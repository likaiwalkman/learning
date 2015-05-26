(ns sicp.core-test
  (:use midje.sweet)
  (:require [clojure.test :refer :all]
            [sicp.core :refer :all]))

(facts "Chapter 1, Building Abstractions with Procedures"
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
             (prime? 561) => true
             )

       (fact "about `sum-*'"
             (sum-integers 1 10) => 55
             (sum-cubes 1 2) => 9
             (sum-pi 1 2) => 1/3
             )

       (fact "about `half-inteval-method'"
             (format "%.2f" (half-interval-method #(Math/sin %) 2.0 4.0)) => "3.14")

       (fact "about `fixed-point'"
             (fixed-point #(Math/cos %) 1.0) => 0.7390822985224024
             (fixed-point (fn [y] (+ (Math/sin y) (Math/cos y)))
                          1.0) => 1.2587315962971173
                          )

       (fact "about `deriv'"
             ((deriv #(* % % %)) 5) => (roughly 75))

       (fact "about `newton-method'"
             (sqrt 4) => (roughly 2)
             (cubic 0 0 -8) => (roughly 2)
             ))


(facts "Chapter 2, Building Abstractions with Data"
       (fact "about Rational Numbers"
             (numer (make-rat 1 3)) => 1
             (denom (make-rat 1 3)) => 3

             (add-rat (make-rat 1 3)
                      (make-rat 1 3))
             => (partial equal-rat? (make-rat 2 3))

             (mul-rat (make-rat 1 3)
                      (make-rat 2 3))
             => (partial equal-rat? (make-rat 2 9))

             (add-rat (make-rat 1 3)
                      (make-rat -2 3))
             => (partial equal-rat? (make-rat -1 3))

             (mul-rat (make-rat -1 3)
                      (make-rat -2 -3))
             => (partial equal-rat? (make-rat -2 9))
             )

       (fact "about hierachy data and closure"
             (-map inc '()) => '()
             (-map inc '(1 2 3)) => '(2 3 4)

             (length '((1 2) 3 4)) => 3
             (count-leaves '((1 2) 3 4)) => 4

             (deep-reverse '((4 3) (2 1))) => '((1 2) (3 4))
             )
       )
