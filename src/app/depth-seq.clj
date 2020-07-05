(ns app.depth-seq (:gen-class))

(defn average [numbers] (/ (apply + numbers) (count numbers)))

(defn depth-seq
  [tree]
  (when (seq tree)
    (cons (map :node tree)
          (depth-seq (mapcat :children tree)))))
