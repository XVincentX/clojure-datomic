(ns app.core (:gen-class) (:require [io.pedestal.http :as http]
                                    [io.pedestal.http.route :as route]
                                    [clojure.data.json :as json]
                                    [environ.core :refer [env]]
                                    [app.depth-seq :as ds]
                                    [clojure.spec.alpha :as s]))

(defn create-server "Creates a new server with the route map" [routes]
  (http/create-server
   {::http/routes routes
    ::http/type   :jetty
    ::http/host "0.0.0.0"
    ::http/port   (Integer. (or (env :port) 5000))}))

(def routes
  (route/expand-routes
   #{["/depth-seq" :get (fn [request] (let [params (get request :query-params)]
                                        (if (s/valid? app.depth-seq/children params)
                                          {:status 200 :body (json/write-str (app.depth-seq/depth-seq params))}
                                          {:status 412})))
      :route-name :depth-seq]}))

(defn start []
  (http/start (create-server routes)))

(defn -main [] (start))
