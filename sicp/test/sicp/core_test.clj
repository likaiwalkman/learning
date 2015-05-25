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
      (count-change 3) => 1
      (count-change 4) => 1
      (count-change 5) => 2
      (count-change 6) => 2
      (count-change 7) => 2
      (count-change 10) => 4
      (count-change 100) => 292
      )

(fact "about greatest common divisor"
      (gcd 4 2) => 2
      (gcd 3 5) => 1
      (gcd 3 5) => 1
      (gcd 10 5) => 5
      (gcd 10 4) => 2
      )

(fact "about `prime?'"
      (prime? 2) => true
      (prime? 3) => true
      (prime? 9) => false
      )
