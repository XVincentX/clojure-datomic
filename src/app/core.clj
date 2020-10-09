(ns app.core (:gen-class) (:require [io.pedestal.http :as http]
                                    [io.pedestal.http.body-params :refer [body-params]]
                                    [io.pedestal.http.route :as route]
                                    [clojure.data.json :as json]
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

(defn handle-get [db name]
  (let [result
        (d/q '[:find (pull ?e [:person/name :person/surname])
               :in $ ?name
               :where
               [?e :person/name ?name]]
             db name)]
    (json/write-str result)))

(defn handle-post [conn data]
  (d/transact conn {:tx-data
                    [{:person/name (get-in data [:person :name])
                      :person/surname (get-in data [:person :surname])}]}))

(def routes
  (route/expand-routes
   #{["/people/:name" :get
      [(interceptors/with-db)
       #(let [result (handle-get (:db %) (get-in % [:path-params :name]))]
          {:status 200 :body (json/write-str result)})]
      :route-name :get-people]

     ["/people" :post
      [(body-params)
       (interceptors/validate-payload-shape :json-params :app.data/person)
       (interceptors/with-db)
       #(let [_ (#'handle-post (:conn %) (:parsed %))]
          {:status 201})]
      :route-name :add-people]}))

(defonce server (atom nil))

(defn start "Starts the server" []
  (reset! server (http/start (create-server routes))))

(defn -main [] (start))

(defn restart "Restart the server when required" []
  (http/stop @server)
  (start))
