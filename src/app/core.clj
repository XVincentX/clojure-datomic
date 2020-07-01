(ns app.core (:gen-class) (:require [datomic.client.api :as d]))

(def db-cfg {:server-type :peer-server
             :access-key "myaccesskey"
             :secret "mysecret"
             :endpoint "localhost:8998"
             :validate-hostnames false})


(def schema [{:db/ident ::title
              :db/unique :db.unique/identity
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/one}

             {:db/ident ::text
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/one}

             {:db/ident ::author
              :db/valueType :db.type/string
              :db/cardinality :db.cardinality/many}

             {:db/ident ::followup
              :db/valueType :db.type/ref
              :db/cardinality :db.cardinality/one}])

(def initial-data [{::title "Grocery" ::text "Pasta" ::author "Vincenzo Chianese" ::followup {::title "Cucina"}}
                   {::title "Cooking" ::text "Pan" ::author "Vincenzo Chianese"}])

(def client (d/client db-cfg))
(def connection (d/connect client {:db-name "hello"}))

(d/transact connection {:tx-data schema})
(d/transact connection {:tx-data initial-data})

(def db (d/db connection))

(def all-titles '[:find ?title
                  :where [?id ::title ?title] [?id ::followup]])


(defn -main
  []
  (println (d/q all-titles db)))
