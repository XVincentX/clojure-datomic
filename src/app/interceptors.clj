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

(def caching-headers "Sets an immutable and public cache header if the request had a valid T query parameter.
                      Alternatively, it will send an ETag header to the"
  (interceptor/after
   ::caching-headers
   #(if (-> % :request :db :asOfT nil?)
      (assoc-in % [:response :headers] {"ETag" (-> % :request :db :t str)})
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

(def early-304 "Returns a 304 response when the request has the If-None-Match header and its value is the same
                         of the current Datomic's t value"
  (interceptor/before
   ::early-304
   #(if-let [if-none-match (get-in % [:request :headers "if-none-match"])]
      (let [t (-> % :request :db :t)]
        (if (= t (Integer. if-none-match))
          (assoc % :response {:status 304 :headers {}})
          %))
      %)))


(defn prefer-caching "Redirects the current request to a know T value if possible"
  [keyword]
  (interceptor/before
   ::prefer-caching
   #(if-not (-> % :request :query-params :t nil?)
      %
      (let [db (-> % :request :db)
            t (-> (d/q '[:find ?t
                         :in $ ?keyword
                         :where [_ ?keyword _ ?tx] [(datomic.api/tx->t ?tx) ?t]]
                       db keyword)
                  sort
                  reverse
                  ffirst)
            path (-> % :request :uri)]
        (if (nil? t)
          %
          (assoc % :response {:status 307
                              :headers {"Location" (str (assoc-query path :t t))}}))))))
