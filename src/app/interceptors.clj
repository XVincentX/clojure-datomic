(ns app.interceptors (:require
                      [clojure.spec.alpha :as s]
                      [datomic.client.api :as d]
                      [app.data :as data]))

(defn validate-payload-shape [source spec]
  {:name ::validate-query-string-shape
   :enter (fn [context] (let [request (:request context)
                              param (get request source)]
                          (if-let [parsed-param (s/conform spec param)]
                            (assoc-in context [:request :parsed] parsed-param)
                            (assoc context :response {:status 412 :headers {}}))))})

(defn with-db []
  {:name ::with-db
   :enter (fn [context] (let [conn (d/connect data/client {:db-name "db"})
                              db (d/db conn)]
                          (-> context
                              (assoc-in [:request :db] db)
                              (assoc-in [:request :conn] conn))))})
