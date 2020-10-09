(ns app.core (:gen-class) (:require [io.pedestal.http :as http]
                                    [io.pedestal.http.body-params :as body-parsers]
                                    [io.pedestal.http.route :as route]
                                    [clojure.data.json :as json]
                                    [environ.core :refer [env]]
                                    [drawbridge.core]
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

(defn handle-post [conn data] (d/transact conn {:tx-data data}))

(def routes
  (route/expand-routes
   #{["/people/:name" :get
      [(interceptors/with-db)
       (fn [request]
         (let [result (#'handle-get (:db request) (get-in request [:path-params :name]))]
           {:status 200 :body (json/write-str result)}))]
      :route-name :get-people]

     ["/people" :post
      [(body-parsers/body-params)
       (interceptors/validate-payload-shape :json-params :app.data/person)
       (interceptors/with-db)
       (fn [request]
         (let [_ (#'handle-post (:conn request) (:parsed request))]
           {:status 201}))]
      :route-name :add-people]}))

(defonce server (atom nil))

(defn start []
  (reset! server (http/start (create-server routes))))

(defn -main [] (start))

(defn restart []
  (http/stop @server)
  (start))

(restart)
