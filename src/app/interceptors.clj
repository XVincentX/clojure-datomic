(ns app.interceptors (:require
                      [clojure.spec.alpha :as s]
                      [datomic.client.api :as d]
                      [datomic.api :refer [tx->t]]
                      [lambdaisland.uri :refer [assoc-query]]
                      [io.pedestal.interceptor.helpers :as interceptor]
                      [app.utils :refer [normalize-payload response-304 response-412 response-307]]
                      [app.data :as data]))

(defn validate-payload-shape "Validates a :source request part with the provided spec"
  [source spec]
  (interceptor/before
   ::validate-payload-shape
   #(let [param (-> % :request source)
          parsed-param (s/conform spec param)]
      (cond-> %
        (= parsed-param :clojure.spec.alpha/invalid) (partial response-412 (s/explain-str spec param))))))

(def caching-headers "Sets an immutable and public cache header if the request had a valid T query parameter.
                      Alternatively, it will send an ETag header to the client"
  (interceptor/after
   ::caching-headers
   #(if (nil? (-> % :request :db :asOfT))
      (assoc-in % [:response :headers "ETag"] (-> % :request :db :t str))
      (assoc-in % [:response :headers "Cache-Control"] "public, max-age=604800, immutable"))))

(def with-db "Attaches a :conn and :db to the request map with the relative Datomic objects"
  (interceptor/on-request
   ::with-db
   #(let [conn (d/connect data/client {:db-name data/db-name})
          db (d/db conn)
          t-string (-> % :query-params :t)
          t (when t-string (Integer. t-string))]
      (-> %
          (assoc :db (if (and (some? t) (<= t (:t db))) (d/as-of db t) db))
          (assoc :conn conn)))))

(def early-304 "Returns a 304 response when the request has the If-None-Match header and its value is the same
                of the current Datomic's t value"
  (interceptor/before
   ::early-304
   #(let [if-none-match (get-in % [:request :headers "if-none-match"])
          t (-> % :request :db :t)]
      (cond-> %
        (and (some? if-none-match) (= t (Integer. if-none-match))) response-304))))

(def tx-304 "Returns a 304 response when the response's body has a tx value that's the same of the request's
             If-None-Match header"
  (interceptor/after
   ::tx-304
   (fn [context]
     (let [if-none-match (get-in context [:request :headers "if-none-match"])
           body          (-> context :response :body)
           t             (when if-none-match (Integer. if-none-match))
           max-t         (when body (apply max (map #(-> % last tx->t) body)))]
       (cond-> max-t
         (and (some? max-t) (= t max-t)) (response-304 context)
         (some? max-t) (update-in context [:response :body] normalize-payload))))))

(defn prefer-caching "Redirects the current request to a know T value if possible"
  [keyword]
  (interceptor/before
   ::prefer-caching
   #(cond-> %
      (nil? (-> % :request :query-params :t))
      (fn [ctx]
        (let [db (-> ctx :request :db)
              t  (-> (d/q '[:find ?t
                            :in $ ?keyword
                            :where [_ ?keyword _ ?tx] [(datomic.api/tx->t ?tx) ?t]]
                          db keyword)
                     sort
                     reverse
                     ffirst)
              path (-> ctx :request :uri)]
          (cond-> ctx
            (some? t) (partial response-307 (str (assoc-query path :t t)))))))))
