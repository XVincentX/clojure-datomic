(ns app.core (:require [clojure.spec.alpha :as spec]
                       [io.pedestal.http :as http]
                       [io.pedestal.http.route :as route]) (:gen-class))

(spec/def ::age integer?)
(spec/def ::name string?)
(spec/def ::surname string?)

(spec/def ::user (spec/keys :req [::name ::surname ::age]))

(spec/valid? ::user {::name "Vincenzo" ::surname "Chianese" ::age 31})

(defn theSum [a b c d] (+ a b c d))

(defn respond-hello [request]
  {:status 200 :body "Hello, world!"})

(def routes
  (route/expand-routes
   #{["/greet" :get respond-hello :route-name :greet]}))

(defn create-server []
  (http/create-server
   {::http/routes routes
    ::http/type   :jetty
    ::http/port   8890}))

(defn start []
  (http/start (create-server)))

(defn -main
  []
  (start))
