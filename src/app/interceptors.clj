(ns app.interceptors (:require
                      [clojure.spec.alpha :as s]
                      [datomic.client.api :as d]
                      [clojure.data.json :as json]
                      [io.pedestal.interceptor.helpers :as interceptor]
                      [app.data :as data]))

(defn validate-payload-shape [source spec]
  (interceptor/on-request
   ::validate-query-string-shape
   #(let [param (get-in % [:request source])
          parsed-param (s/conform spec param)]
      (if (= parsed-param :clojure.spec.alpha/invalid)
        (assoc % :response {:status 412
                            :headers {}
                            :body (json/write-str (s/explain-str spec param))})
        (assoc-in % [:request :parsed] parsed-param)))))

(defn with-db [] (interceptor/on-request
                  ::with-db
                  #(let [conn (d/connect data/client {:db-name "db"})
                         db (d/db conn)]
                     (-> %
                         (assoc-in [:request :db] db)
                         (assoc-in [:request :conn] conn)))))
