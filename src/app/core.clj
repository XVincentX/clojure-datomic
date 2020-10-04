(ns app.core (:gen-class) (:require [io.pedestal.http :as http]
                                    [io.pedestal.http.route :as route]
                                    [clojure.data.json :as json]
                                    [environ.core :refer [env]]
                                    [app.depth-seq :as ds]
                                    [clojure.spec.alpha :as s]
                                    [clojure.edn :as edn]
                                    [drawbridge.core]))

(defn create-server "Creates a new server with the route map" [routes]
  (http/create-server
   {::http/routes routes
    ::http/type   :jetty
    ::http/host "0.0.0.0"
    ::http/allowed-origins (constantly true)
    ::http/join? false
    ::http/port   (Integer. (or (env :port) 5000))}))

(defn validate-query-string-shape [qstring spec]
  {:name ::validate-query-string-shape
   :enter (fn [context] (let [request (:request context)
                              q-param (get-in request [:query-params qstring])
                              parsed-param (edn/read-string q-param)]
                          (if (s/valid? spec parsed-param)
                            (assoc-in context [:request :parsed] parsed-param)
                            (assoc context :response {:status 412 :headers {}}))))})

(def routes
  (route/expand-routes
   #{["/depth-seq" :get
      [(validate-query-string-shape :q :app.depth-seq/children)
       (fn [request] {:status 200 :body (json/write-str (ds/depth-seq (:parsed request)))})]
      :route-name :depth-seq]
     ["/repl" :any
      [(fn [request] ((drawbridge.core/ring-handler) request))]
      :route-name :repl]}))

(defonce server (atom nil))

(defn start []
  (reset! server
          (http/start (create-server routes))))

(defn -main [] (start))

(defn restart []
  (http/stop @server)
  (start))
