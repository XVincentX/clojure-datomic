(ns app.utils)

(defn response-304 "Returns an empty 304 response"
  [context]
  (assoc context :response {:status 304 :headers {}}))

(defn response-412 "Returns a 412 response with the specified message"
  [message context]
  (assoc context :response {:status 412
                            :headers {"Content-Type" "text/plain"}
                            :body message}))

(defn normalize-payload "Takes a typical Datomic query result and returns the first item,
                         assuming the last one is the entity ID used for caching purposes"
  [payload]
  (map first payload))
