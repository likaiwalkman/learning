(ns sicp.core
  (:require [clojure.pprint :as pp]))

;;;; -------------------------------------------
;;;; Chapter 1, Building Abstractions with Procedures
;;;; -------------------------------------------

(defn- square
  [n]
  (* n n))
(defn- cube
  [n]
  (* n n n))
(defn- average
  [a b]
  (/ (+ a b) 2))

(defn sqrt-newton
  "Newton method to calculate sqrt"
  [x]
  (let [good-enough? (fn [guess]
                       (<= (Math/abs (- (square guess) x))
                           0.001))
        improve (fn [guess]
                  (/ (+ guess (/ x guess)) 2))
        sqrt-iter (fn sqrt-iter [guess]
                    (if (good-enough? guess)
                      guess
                      (sqrt-iter (improve guess))))]
    (sqrt-iter 1.0)))

(defn fib
  "Fibonacci implementation as a linear recursive process"
  ([n] (fib 0 1 n))
  ([pree pre n]
   (if (= n 2)
     (+ pree pre)
     (fib pre (+ pree pre) (- n 1)))))


;; counting change
;; coins: 1, 5, 10, 25, 50
(defn count-change
  "Calculate how many ways to change money"
  ([amount] (count-change amount [50 25 10 5 1]))
  ([amount coins]
   (cond
     ;; 0 => 1, so that 5 has 2 kinds of change
     (= amount 0) 1
     (< amount 0) 0
     (= 1 (count coins)) (if (zero? (mod amount (first coins)))
                           1
                           0)
     :else (+
            ;; without first coin
            (count-change amount (rest coins))
            ;; with first coin
            (count-change (- amount (first coins)) coins)))))

;; pascal's triangle
(defn pascal-triangle
  "Generate pascal triangle"
  [level]
  (if (= level 1)
    [[1]]
    (let [pre (pascal-triangle (- level 1))
          pre-last (last pre)]
      (conj pre (vec (map #(+ (or (get pre-last (dec %)) 0)
                              (or (get pre-last %) 0))
                          (range (inc (count pre-last)))))))))

(defn gcd
  "Calculate greatest common divisor"
  [a b]
  (if (= 0 b)
    a
    (gcd b (mod a b))))


;; check prime
(defn prime?
  "Test if a number is prime"
  [n]
  (let [divides? (fn [a b]
                   (= (mod b a) 0))
        find-divisor (fn find-divisor [n test-divisor]
                       ;; test-divisor is the smallest atom,
                       ;; a * a > n => n is prime
                       (cond (> (square test-divisor) n) n
                             (divides? test-divisor n) test-divisor
                             :else (find-divisor n (inc test-divisor))))
        smallest-divisor (fn [n]
                           (find-divisor n 2))]
    (= n (smallest-divisor n))))

;; rewrite prime?, using 费马小定理
(defn prime?
  "A probabilistic algorithm to test prime number, using Fermat's Little Theorem(a == a**n mod n)"
  ([n] (prime? n 10))
  ([n times]
   (let [expmod (fn expmod [base exp m]
                  "calculate: base**exp mod m"
                  (cond (zero? exp) 1
                        (even? exp) (mod (square (expmod base (/ exp 2) m)) m)
                        :else (mod (* base (expmod base (dec exp) m)) m)))
         fermat-test (fn [a n]
                       (== (expmod a n n)
                           a))
         rand-from-1 (fn [n]
                 "rand int from 1 (inclusive) to n (exclusive)"
                 (inc (rand-int (dec n))))]
     (if (= 0 times)
       true
       (and (fermat-test (rand-from-1 n) n)
            (prime? n (dec times)))))))

;;; 1.3.1 Procedures as Arguments
(defn sum-integers
  [a b]
  (if (> a b)
    0
    (+ a (sum-integers (inc a) b))))
(defn sum-cubes
  [a b]
  (if (> a b)
    0
    (+ (* a a a) (sum-cubes (inc a) b))))
(defn sum-pi
  [a b]
  (if (> a b)
    0
    (+ (/ 1 (* a (+ a 2))) (sum-pi (+ a 4) b))))
;; high-order procedure
(defn sum
  [term next]
  (fn local
    [a b]
    (if (> a b)
      0
      (+ (term a) (local (next a) b)))))
(def sum-integers (sum identity inc))
(def sum-cubes (sum #(* % % %) inc))
(def sum-pi (sum #(/ 1 (* % (+ % 2))) #(+ % 4)))

;; half-inteval method
(defn search-zero
  "Find x of `f(x) = 0'"
  [f neg pos]
  (let [mid (average neg pos)
        close-enough? (fn [x y]
                        (< (Math/abs (- x y)) 0.001))]
    (if (close-enough? neg pos)
      mid
      (let [test-value (f mid)]
        (cond
          (pos? test-value) (search-zero f neg mid)
          (neg? test-value) (search-zero f mid pos)
          :else mid
          )))))
(defn half-interval-method
  [f a b]
  (let [a-value (f a)
        b-value (f b)]
    (cond
      (and (pos? a-value) (neg? b-value)) (search-zero f b a)
      (and (neg? a-value) (pos? b-value)) (search-zero f a b)
      :else (throw (Exception. (str "Values are not of opposite sign" a b)))
      )))

;; finding fix point of function
(defn fixed-point
  "Fix point of function. return x of `f(x) = x'"
  [f first-guess]
  (let [tolerance 0.00001
        close-enough? (fn [v1 v2]
                        (< (Math/abs (- v1 v2)) tolerance))
        try-it (fn try-it [guess]
                 (let [next (f guess)]
                   (if (close-enough? guess next)
                     next
                     (try-it next))))]
    (try-it first-guess)))

;; Newton's method
(defn deriv
  "导数. `(g(x + dx) - g())/dx'"
  [g]
  (let [dx 0.00001]
    (fn [x]
      (/ (- (g (+ x dx))
            (g x))
         dx))))
(defn newton-transform
  "next = x - g(x)/g'(x), g(next)会比g(x)更接近0"
  [g]
  (fn [x]
    (- x
       (/ (g x)
          ((deriv g) x)))))
(defn newton-method
  "`fixed-point'(找x of `f(x) = x')
配合 `newton-transform'(`f(x) = x - g(x)/g'(x)'), 找出 x of `g(x) = 0'"
  [g guess]
  (fixed-point (newton-transform g) guess))

(defn sqrt [x]
  (newton-method #(- (square %) x) 1.0))
(defn cubic
  "x^3 + ax^2 + bx + c = 0"
  [a b c]
  (newton-method #(+ (cube %)
                     (* a (square %))
                     (* b %)
                     c) 1.0))



;;;; -------------------------------------------
;;;; Chapter 2, Building Abstractions with Data
;;;; -------------------------------------------

;;; Arithmetic Operations for Rational Numbers
(declare make-rat numer denom)
(defn add-rat [x y]
  (make-rat (+ (* (numer x) (denom y))
               (* (numer y) (denom x)))
            (* (denom x) (denom y))))
(defn sub-rat [x y]
  (make-rat (- (* (numer x) (denom y))
               (* (numer y) (denom x)))
            (* (denom x) (denom y))))
(defn mul-rat [x y]
  (make-rat (* (numer x) (numer y))
            (* (denom x) (denom y))))
(defn div-rat [x y]
  (make-rat (* (numer x) (denom y))
            (* (denom x) (numer y))))
(defn equal-rat? [x y]
  (= (* (numer x) (denom y))
     (* (denom x) (numer y))))
(defn make-rat [x y]
  (let [g (gcd x y)]
    (list (/ x g) (/ y g))))
(defn numer [x] (first x))
(defn denom [x] (second x))
(defn print-rat [x]
  (let [is-neg? (neg? (bit-xor (numer x) (denom x)))]
    (str (if is-neg? "-" nil) (Math/abs (numer x)) "/" (Math/abs (denom x)))))

;;; Interval Arithmetic
(declare make-interval lower-bound upper-bound)
(defn add-interval [x y]
  (make-interval (+ (lower-bound x) (lower-bound y))
                 (+ (upper-bound x) (upper-bound y))))
(defn sub-interval [x y]
  (make-interval (- (lower-bound x) (lower-bound y))
                 (- (upper-bound x) (upper-bound y))))
(defn mul-interval [x y]
  (let [p1 (* (lower-bound x) (lower-bound y))
        p2 (* (lower-bound x) (upper-bound y))
        p3 (* (upper-bound x) (lower-bound y))
        p4 (* (upper-bound x) (upper-bound y))]
    (make-interval (min p1 p2 p3 p4)
                   (max p1 p2 p3 p4))))
(defn div-interval [x y]
  (if (or (zero? (upper-bound y)) (zero? (lower-bound y)))
    (throw (Exception. "divisor can't be zero"))
    (mul-interval x
                  (make-interval (/ 1.0 (upper-bound y))
                                 (/ 1.0 (lower-bound y))))))
(defn make-interval [x y] (list x y))
(defn lower-bound [x] (first x))
(defn upper-bound [x] (second x))
