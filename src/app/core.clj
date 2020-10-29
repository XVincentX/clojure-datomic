(ns app.core (:gen-class) (:require [io.pedestal.http :as http]
                                    [io.pedestal.http.body-params :refer [body-params]]
                                    [io.pedestal.http.route :as route]
                                    [environ.core :refer [env]]
                                    [app.interceptors :as interceptors]
                                    [datomic.client.api :as d]
                                    [app.data :as data]))

(defn create-server "Creates a new server with the route map" [routes]
  (-> {::http/routes routes
       ::http/type :jetty
       ::http/host "0.0.0.0"
       ::http/join? false
       ::http/port (Integer. (or (env :port) 5000))}
      (http/default-interceptors)
      (update ::http/interceptors conj interceptors/with-db)
      (update ::http/interceptors conj interceptors/early-304)
      (update ::http/interceptors conj interceptors/tx-304)
      (update ::http/interceptors conj interceptors/caching-headers)
      (update ::http/interceptors conj http/json-body)
      http/create-server))

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

(defn add-user [conn data]
  (let [id (java.util.UUID/randomUUID)]
    (d/transact conn {:tx-data [(assoc data :person/id id)]})
    id))

(def routes
  (route/expand-routes
   #{["/people/:id" :get
      [(interceptors/validate-payload-shape :path-params :person/id)
       #(let [db (:db %)
              id (-> % :path-params :id java.util.UUID/fromString)
              result (get-user-by-id db id)]
          {:status 200 :body result})]
      :route-name :get-person]

     ["/people" :get
      [#(let [result (get-all-users (:db %))]
          {:status 200 :body result})]
      :route-name :get-people]

     ["/people" :post
      [(body-params)
       (interceptors/validate-payload-shape :json-params :app.data/person)
       #(let [id (add-user (:conn %) (:parsed %))]
          {:status 201 :body (str id)})]
      :route-name :add-people]}))

(defonce server (atom nil))

(defn start "Starts the server" []
  (data/init-db! data/db-name)
  (reset! server (-> routes create-server http/start)))

(defn -main [] (start))

(defn restart "Restart the server when required" []
  (http/stop @server)
  (start))
