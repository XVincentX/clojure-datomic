(ns app.core (:gen-class) (:require [io.pedestal.http :as http]
                                    [io.pedestal.http.body-params :refer [body-params]]
                                    [io.pedestal.http.route :as route]
                                    [environ.core :refer [env]]
                                    [app.interceptors :as interceptors]
                                    [datomic.client.api :as d]))

(defn create-server "Creates a new server with the route map" [routes]
  (http/create-server
   {::http/routes routes
    ::http/type   :jetty
    ::http/host "0.0.0.0"
    ::http/join? false
    ::http/port   (Integer. (or (env :port) 5000))}))

(defn get-user-by-id [db id]
  (d/pull db '[:person/name :person/surname] id))

(defn get-users-id [db]
  (d/q '[:find ?e :in $ :where [?e :person/name _]] db))

(defn add-user [conn data] (d/transact conn {:tx-data [data]}))

(def routes
  (route/expand-routes
   #{["/people/:id" :get
      [(interceptors/with-db)
       #(let [result (get-user-by-id (:db %) (Long. (get-in % [:path-params :id])))]
          {:status 200 :body result})]
      :route-name :get-person]

     ["/people" :get
      [(interceptors/with-db)
       #(let [result (get-users-id (:db %))]
          {:status 200 :body result})]
      :route-name :get-people]

     ["/people" :post
      [(body-params)
       (interceptors/validate-payload-shape :json-params :app.data/person)
       (interceptors/with-db)
       #(let [_ (add-user (:conn %) (:parsed %))] {:status 201})]
      :route-name :add-people]}))

(defonce server (atom nil))

(defn start "Starts the server" []
  (reset! server (http/start (create-server routes))))

(defn -main [] (start))

(defn restart "Restart the server when required" []
  (http/stop @server)
  (start))
