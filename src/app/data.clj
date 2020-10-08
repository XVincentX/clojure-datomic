(ns app.data (:require [datomdic.client.api :as d]
                       [clojure.spec.alpha :as s]))

(def client (d/client {:server-type :dev-local
                       :system "dev"
                       :storage-dir "/Users/vncz/dev/gops/src/gops/data/"}))

(d/create-database client {:db-name "db"})
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

(s/def :person/name string?)
(s/def :person/surname string?)
(s/def ::note string?)
(s/def :person/note (s/keys :req [::note]))
(s/def :person/notes (s/coll-of :person/note :kind vector?))
(s/def ::person (s/keys :req [:person/name :person/surname] :opt [:person/notes]))

(def example-person
  {:person/name "Vincenzo"
   :person/surname "Chianese"
   :person/notes [{::note "Nasino pariosino"}]})

(s/conform ::person example-person)


(def db (d/db conn))

(d/q '[:find ?name
       :in $
       :where
       [?note-id ::note "Nota 2"]
       [?e :person/notes ?note-id]
       [?e :person/name ?name]] db)

(d/transact conn {:tx-data people-schema})
(d/transact conn {:tx-data [example-person]})
