(ns app.core (:gen-class) (:require [io.pedestal.http :as http]
                                    [io.pedestal.http.body-params :refer [body-params]]
                                    [io.pedestal.http.route :as route]
                                    [environ.core :refer [env]]
                                    [app.interceptors :as interceptors]
                                    [app.data :as data]
                                    [app.query :as queries]))

(defn create-server "Creates a new server with the route map" [routes]
  (-> {::http/routes routes
       ::http/type   :immutant
       ::http/host   "0.0.0.0"
       ::http/join?  false
       ::http/port   (Integer. (or (env :port) 5000))}
      (http/default-interceptors)
      (update ::http/interceptors into [interceptors/with-db
                                        interceptors/early-304
                                        interceptors/caching-headers
                                        http/json-body
                                        interceptors/tx-304])
      http/create-server))

(def routes
  (route/expand-routes
   #{["/people/:id" :get
      [(interceptors/validate-payload-shape :path-params :person/id)
       #(let [db     (:db %)
              id     (-> % :path-params :id java.util.UUID/fromString)
              result (queries/get-user-by-id db id)]
          {:status 200 :body result})]
      :route-name :get-person]

     ["/people" :get
      [#(let [result (queries/get-all-users (:db %))]
          {:status 200 :body result})]
      :route-name :get-people]

     ["/people" :post
      [(body-params)
       (interceptors/validate-payload-shape :json-params :app.data/person)
       #(let [id (queries/add-user! (:conn %) (:json-params %))]
          {:status 201 :body (str id)})]
      :route-name :add-people]}))

(defonce server (atom nil))

(defn start "Starts the server" []
  (data/init-db! data/db-name)
  (reset! server (-> routes create-server http/start)))

(defn -main [] (start))

(defn restart "Restart the server when required" []
  (http/stop @server)
  (start))

(comment (restart))
(comment (-main))
