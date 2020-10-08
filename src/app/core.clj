(ns app.core (:gen-class) (:require [io.pedestal.http :as http]
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



(def routes
  (route/expand-routes
   #{["/people/:name" :get
      [(interceptors/with-db)
       (fn [request]
         (let [result (d/q '[:find ?surname ?notes
                             :in $ ?name
                             :where
                             [?e :person/notes ?notes]
                             [?e :person/surname ?surname]]
                           (:db request) (get-in request [:path-params :name]))]
           {:status 200 :body (json/write-str result)}))]
      :route-name :get-people]
     ["/people/" :post
      [(interceptors/validate-payload-shape :json-params :app.data/person)
       (interceptors/with-db)
       (constantly {:status 200 :body (json/write-str {:a 1 :b 2})})]
      :route-name :add-people]}))

(defonce server (atom nil))

(defn start []
  (reset! server (http/start (create-server routes))))

(defn -main [] (start))

(defn restart []
  (http/stop @server)
  (start))

(restart)
