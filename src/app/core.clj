(ns app.core (:gen-class) (:require [io.pedestal.http :as http]
                                    [io.pedestal.http.route :as route]
                                    [clojure.data.json :as json]
                                    [environ.core :refer [env]]))

(defn create-server [routes]
  (http/create-server
   {::http/routes routes
    ::http/type   :jetty
    ::http/port   (Integer. (or (env :port) 5000))}))

(def routes
  (route/expand-routes
   #{["/depth-seq" :get (fn respond [req] {:status 200 :body (json/write-str {:a 1 :b 2})}) :route-name :depth-seq]}))

(defn start []
  (http/start (create-server routes)))

(defn -main [] (start))
