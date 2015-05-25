(ns sicp.core
  (:require [clojure.pprint :as pp]))

(defn- square
  [n]
  (* n n))

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
  "Calculate greatest comman divisor"
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
