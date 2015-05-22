(ns sicp.core-test
  (:use midje.sweet)
  (:require [clojure.test :refer :all]
            [sicp.core :refer :all]))

(fact "Sqrt with Newton's Method"
      (format "%.4f" (sqrt-newton 2.0)) => "1.4142")
(fact "fib could be work"
      (fib 6) => 8)

(fact "about `count-change'"
      (count-change 1) => 1
      (count-change 2) => 1
      (count-change 5) => 2
      (count-change 7) => 2
      (count-change 10) => 4
      (count-change 100) => 292
      )
