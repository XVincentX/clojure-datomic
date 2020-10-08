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
    ::http/allowed-origins (constantly true)
    ::http/join? false
    ::http/port   (Integer. (or (env :port) 5000))}))

(def routes
  (route/expand-routes
   #{["/people/:id" :get
      [(interceptors/with-db)
       (fn [context] (let [db (get-in context [:request :db])
                           result (d/q '[:find ?name
                                         :in $
                                         :where
                                         [?note-id ::note "Nota 2"]
                                         [?e :person/notes ?note-id]
                                         [?e :person/name ?name]] db)]
                       (json/write-str result)))]
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
