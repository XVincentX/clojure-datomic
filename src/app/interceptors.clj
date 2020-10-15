(ns app.interceptors (:require
                      [clojure.spec.alpha :as s]
                      [datomic.client.api :as d]
                      [io.pedestal.interceptor.helpers :as interceptor]
                      [app.data :as data]))

(defn validate-payload-shape [source spec]
  (interceptor/before
   ::validate-query-string-shape
   #(let [param (get-in % [:request source])
          parsed-param (s/conform spec param)]
      (if (= parsed-param :clojure.spec.alpha/invalid)
        (assoc % :response {:status 412
                            :headers {"Content-Type" "text/plain"}
                            :body (s/explain-str spec param)})
        (assoc-in % [:request :parsed] parsed-param)))))

(def caching-headers "Sets an immutable and public cache header if the request had the T query parameter"
  (interceptor/after
   ::caching-headers
   #(if  (nil? (:asOfT (get-in % [:request :db])))
      %
      (assoc-in % [:response :headers] {"Cache-Control" "public, max-age=604800, immutable"}))))

(def with-db
  (interceptor/on-request
   ::with-db
   #(let [conn (d/connect data/client {:db-name data/db-name})
          db (d/db conn)
          t (get-in % [:query-params :t])]
      (-> %
          (assoc :db (if (and (not (nil? t)) (< (Integer. t) (:t db))) (d/as-of db (Integer. t)) db))
          (assoc :conn conn)))))
