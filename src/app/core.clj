(ns app.core (:gen-class) (:require [app.depth-seq :as ds]
                                    [io.pedestal.http :as http]
                                    [io.pedestal.http.route :as route]
                                    [clojure.data.json :as json]))

(defn create-server [routes]
  (http/create-server
   {::http/routes routes
    ::http/type   :jetty
    ::http/port   8890}))

(def routes
  (route/expand-routes
   #{["/depth-seq" :get (fn nasino [] (json/write-str {:a 1 :b 2})) :route-name :depth-seq]}))

(defn start []
  (http/start (create-server routes)))

(defn -main [] (start))
