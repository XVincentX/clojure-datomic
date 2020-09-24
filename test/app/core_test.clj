(ns app.core-test
  (:require [clojure.test :as t]
            [app.depth-seq :as ds]))

(t/deftest depth-seq
  (t/testing "Returns nil when empty collection"
    (t/is (= nil (ds/depth-seq []))))
  (t/testing "Returns the value when the collection has one item"
    (t/is (= (list (list 10)) (ds/depth-seq [{:value 10}]))))
  (t/testing "Returns the sum when the collection has more than one item"
    (t/is (= (list (list 10) (list 20)) (ds/depth-seq [{:value 10 :children [{:value 20}]}])))))
