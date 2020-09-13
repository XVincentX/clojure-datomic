(ns app.depth-seq (:require [clojure.spec.alpha :as s]))

(s/def ::value int?)
(s/def ::node (s/keys :req [::value ::children]))
(s/def ::children (s/coll-of ::node))

(defn average [numbers] (/ (apply + numbers) (count numbers)))

(defn depth-seq
  [tree]
  (when (seq tree)
    (cons (map ::node tree)
          (depth-seq (mapcat ::children tree)))))
