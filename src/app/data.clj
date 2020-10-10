(ns app.data (:require [datomic.client.api :as d]
                       [clojure.spec.alpha :as s]))

(def client (d/client {:server-type :dev-local
                       :system "dev"
                       :storage-dir "/Users/vncz/dev/app/src/data/"}))

(d/create-database client {:db-name "db"})
(comment (d/delete-database client {:db-name "db"}))
(def conn (d/connect client {:db-name "db"}))

(def people-schema [{:db/ident :person/name
                     :db/valueType :db.type/string
                     :db/cardinality :db.cardinality/one}

                    {:db/ident :person/surname
                     :db/valueType :db.type/string
                     :db/cardinality :db.cardinality/one}

                    {:db/ident :person/notes
                     :db/valueType :db.type/ref
                     :db/isComponent true
                     :db/cardinality :db.cardinality/many}

                    {:db/ident ::note
                     :db/valueType :db.type/string
                     :db/cardinality :db.cardinality/one}])

(d/transact conn {:tx-data people-schema})

(s/def :person/name string?)
(s/def :person/surname string?)
(s/def ::note string?)
(s/def :person/note (s/keys :req [::note]))
(s/def :person/notes (s/coll-of :person/note :kind vector?))
(s/def ::person (s/keys :req [:person/name :person/surname] :opt [:person/notes]))
