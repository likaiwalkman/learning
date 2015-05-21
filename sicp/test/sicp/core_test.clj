(ns sicp.core-test
  (:use midje.sweet)
  (:require [clojure.test :refer :all]
            [sicp.core :refer :all]))

(fact "Sqrt with Newton's Method"
      (format "%.4f" (sqrt-newton 2.0)) => "1.4142")
(fact "fib could be work"
      (fib 6) => 8)
