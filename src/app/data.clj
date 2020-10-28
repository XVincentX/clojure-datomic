(ns app.data (:require [datomic.client.api :as d]
                       [clojure.spec.alpha :as s]))


(def db-schema [{:db/ident :person/id
                 :db/valueType :db.type/uuid
                 :db/unique :db.unique/identity
                 :db/cardinality :db.cardinality/one}

                {:db/ident :person/name
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

(def seed-data [{:person/id (java.util.UUID/randomUUID)
                 :person/name "Vincenzo"
                 :person/surname "Chianese"
                 :person/nodes [{::note "Nobody wants me"}]}

                {:person/id (java.util.UUID/randomUUID)
                 :person/name "Elio"
                 :person/surname "Bencini"
                 :person/nodes [{::note "I am ugly"}]}])

(def client (d/client {:server-type :dev-local
                       :system "dev"
                       :storage-dir "/Users/vncz/dev/app/src/data/"}))

(def db-name "db")
(defn init-db! "If the Datomic instance has no database, it will create one"
  [db-name]
  (when (zero? (count (d/list-databases client {})))
    (d/create-database client {:db-name db-name})
    (let [conn (d/connect client {:db-name db-name})]
      (d/transact conn {:tx-data db-schema})
      (d/transact conn {:tx-data seed-data}))))

(defn reset-db! [db-name] (d/delete-database client {:db-name db-name}))
(defn get-current-db [] (d/db (d/connect client {:db-name db-name})))

(s/def :person/id uuid?)
(s/def :person/name string?)
(s/def :person/surname string?)
(s/def ::note string?)
(s/def :person/notes (s/coll-of :note :kind vector?))
(s/def ::person (s/keys :req [:person/name :person/surname] :opt [:person/notes]))
