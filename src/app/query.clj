(ns app.query (:require [datomic.client.api :as d]))

(defn get-user-by-id [db id]
  (d/q '[:find (pull ?e [:person/id :person/name :person/surname]) ?t
         :in $ ?id
         :where
         [?e :person/id ?id ?txId]
         [?e :person/name _ ?txName]
         [?e :person/surname _ ?txSurname]
         [(max ?txId ?txName ?txSurname) ?t]] db id))

(defn get-all-users [db]
  (d/q '[:find (pull ?e [:person/id :person/name :person/surname]) ?t
         :where
         [?e :person/id _ ?txId]
         [?e :person/name _ ?txName]
         [?e :person/surname _ ?txSurname]
         [(max ?txId ?txName ?txSurname) ?t]] db))

(defn add-user! [conn data]
  (let [id (java.util.UUID/randomUUID)]
    (d/transact conn {:tx-data [(assoc data :person/id id)]})
    id))
