(ns app.interceptors (:require
                      [clojure.spec.alpha :as s]
                      [datomic.client.api :as d]
                      [lambdaisland.uri :refer [assoc-query]]
                      [io.pedestal.interceptor.helpers :as interceptor]
                      [app.data :as data]))

(defn validate-payload-shape "Validates a :source request part with the provided spec"
  [source spec]
  (interceptor/before
   ::validate-payload-shape
   #(let [param (-> % :request source)
          parsed-param (s/conform spec param)]
      (if (= parsed-param :clojure.spec.alpha/invalid)
        (assoc % :response {:status 412
                            :headers {"Content-Type" "text/plain"}
                            :body (s/explain-str spec param)})
        (assoc-in % [:request :parsed] parsed-param)))))

(def caching-headers "Sets an immutable and public cache header if the request had a valid T query parameter"
  (interceptor/after
   ::caching-headers
   #(if (nil? (:asOfT (-> % :request :db)))
      %
      (assoc-in % [:response :headers] {"Cache-Control" "public, max-age=604800, immutable"}))))

(def with-db "Attaches a :conn and :db to the request map with the relative Datomic objects"
  (interceptor/on-request
   ::with-db
   #(let [conn (d/connect data/client {:db-name data/db-name})
          db (d/db conn)
          t-string (-> % :query-params :t)
          t (when t-string (Integer. t-string))]
      (-> %
          (assoc :db (if (and (not (nil? t)) (< t (:t db))) (d/as-of db t) db))
          (assoc :conn conn)))))

(defn prefer-caching "Redirects the current request to a know T value if possible"
  [keyword]
  (interceptor/before
   ::prefer-caching
   #(if-not (nil? (-> % :request :query-params :t))
      %
      (let [db (-> % :request :db)
            t (ffirst (-> (d/q '[:find ?t
                                 :in $ ?keyword
                                 :where [_ ?keyword _ ?tx] [(datomic.api/tx->t ?tx) ?t]]
                               db keyword)
                          sort
                          reverse))
            path (-> % :request :uri)]
        (assoc % :response {:status 307
                            :headers {"Location" (str (assoc-query path :t t))}})))))
