(ns app.depth_seq (:require [clojure.spec.alpha :as s]))

(s/def ::node int?)
(s/def ::tree (s/coll-of (s/keys :req [::node] :opt [::children])))
(s/def ::children (s/coll-of ::tree))

(defn average [numbers] (/ (apply + numbers) (count numbers)))

(defn depth-seq
  [tree]
  (when (seq tree)
    (cons (map :node tree)
          (depth-seq (mapcat :children tree)))))
