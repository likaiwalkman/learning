(ns sicp.ch2-test
  (:use midje.sweet)
  (:require [clojure.test :refer :all]
            [sicp.ch2 :refer :all]))

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
             (count-leaves '((1 2) (3 (4 5)))) => 5

             (deep-reverse '((4 3) (2 1))) => '((1 2) (3 4))
             )

       (fact "about sequence operations"
             (-filter #(> % 0) '(-1 0 1 2)) => '(1 2)
             (-reduce + 0 '(-1 0 1 2)) => 2
             (-reduce * 1 '(1 2 3 4 5)) => 120

             (fold-right #(cons %2 %1) '() '(1 2)) => '(1 2)
             (fold-left #(cons %2 %1) '() '(1 2)) => '(2 1)

             ;; nested map
             (prime-sum-pairs 6) => '((6 1 7) (6 5 11) (5 2 7) (4 1 5) (4 3 7) (3 2 5) (2 1 3))

             (permutations '(1 2)) => '((2 1) (1 2))
             (permutations '(1 2 3)) => '((3 2 1) (3 1 2) (2 3 1) (2 1 3) (1 3 2) (1 2 3))

             ;; eight queens
             (queens 1) => '(((1 1)))
             (queens 2) => '()
             (count (queens 4)) => 2
             (count (queens 5)) => 10
             (count (queens 6)) => 4
             (count (queens 7)) => 40
             )

       (fact "about differentiation"
             (deriv '(+ x 1) 'x) => 1
             (deriv '(* x y) 'x) => 'y
             (deriv '(* (* x y) (+ x 3)) 'x) => '(+ (* x y) (* y (+ x 3)))
             (deriv '(* x y (+ x 3)) 'x) => '(+ (* x y) (* y (+ x 3)))
             )

       (fact "about set"
             (element-of-set? 1 '(2 (1) (3))) => true
             (element-of-set? 1 '(3 (2) ())) => false

             (element-of-set? 3 (adjoin-set 3 '(2 (1 () ()) ()))) => true

             (intersection-set '(2 (1 () ()) (3 () ())) '(4 (2 () ()) (5 () ()))) => '(2 () ())
             (tree->list-1 (union-set '(2 (1 () ()) (3 () ())) '(4 (2 () ()) (5 () ())))) => '(1 2 3 4 5)
             )
       )
