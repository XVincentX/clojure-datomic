(ns app.interceptors (:require
                      [clojure.spec.alpha :as s]
                      [datomic.client.api :as d]
                      [clojure.data.json :as json]
                      [app.data :as data]))

(defn validate-payload-shape [source spec]
  {:name ::validate-query-string-shape
   :enter (fn [context] (let [param (get-in context [:request source])
                              parsed-param (s/conform spec param)]
                          (if (= parsed-param :clojure.spec.alpha/invalid)
                            (assoc context :response {:status 412 :headers {} :body (json/write-str (s/explain-str spec param))})
                            (assoc-in context [:request :parsed] parsed-param))))})

(defn with-db []
  {:name ::with-db
   :enter (fn [context] (let [conn (d/connect data/client {:db-name "db"})
                              db (d/db conn)]
                          (-> context
                              (assoc-in [:request :db] db)
                              (assoc-in [:request :conn] conn))))})
