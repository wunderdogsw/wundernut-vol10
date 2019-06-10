(ns wunderpahkina-vol10.core-test
  (:require [clojure.test :refer :all]
            [wunderpahkina-vol10.core :refer :all]))

(deftest with-complex-example
  (testing "should produce correct result"
    (is (= {"quark" 13030300 "gromit" 5 "milo" 5}
           (interpret (->actions (preprocess-input "example_with_everything.dog")))))))
