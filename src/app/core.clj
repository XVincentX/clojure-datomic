(ns app.core (:require [clojure.spec.alpha :as spec]) (:gen-class))

(spec/def ::age integer?)
(spec/def ::name string?)
(spec/def ::surname string?)

(spec/def ::user (spec/keys :req [::name ::surname ::age]))

(spec/valid? ::user {::name "Vincenzo" ::surname "Chianese" ::age 31})

(defn theSum [a b c d] (+ a b c d))

(defn -main
  []
  (println (spec/valid? ::user {::name "Vincenzo" ::surname "Chianese" ::age 31})))
