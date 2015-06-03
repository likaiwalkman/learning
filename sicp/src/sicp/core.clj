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
  "Newton method to calculate sqrt, `(guess + x/guess)/2'"
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
                        ;; (base**(exp/2) mod m)**2 mod m = base**exp mod m
                        (even? exp) (mod (square (expmod base (/ exp 2) m)) m)
                        ;; (base * (base**(exp-1) % m)) mod m = base**exp mod m
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
  "导数. `(g(x + dx) - g(x))/dx'"
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


;;; Hierarchy data and closure property
;; mapping over lists
(defn -map [f coll]
  (if (empty? coll)
    coll
    (cons (f (first coll))
          (-map f (rest coll)))))

(defn length [coll]
  "calculate the length of coll"
  (if (empty? coll)
    0
    (inc (length (rest coll)))))
(defn count-leaves [coll]
  "calculate how many atomic leaves in a coll"
  (cond
    (nil? coll) 0
    (not (seq? coll)) 1
    (empty? coll) 0
    :else (+ (count-leaves (first coll))
             (count-leaves (rest coll)))
    ))

(defn deep-reverse [coll]
  (cond
    (not (seq? coll)) coll
    (empty? coll) coll
    :else (concat (deep-reverse (rest coll))
                  (list (deep-reverse (first coll))))))

;; sequence operations
(defn -filter [pred seq]
  (cond
    (empty? seq) '()
    (pred (first seq)) (cons (first seq)
                             (-filter pred (rest seq)))
    :else (-filter pred (rest seq))))

(defn -reduce [op initial seq]
  (if (empty? seq)
    initial
    (op (-reduce op initial (rest seq))
        (first seq)
        )))

;; redefine functions by -reduce
(defn -map [f seq]
  (-reduce #(cons (f %2) %1) '() seq))
(defn -each [f seq]
  (-reduce #(f %2) nil seq))
(-each #(println %) '(1 2 3))
(defn -concat [seq1 seq2]
  (-reduce #(cons %2 %1) seq2 seq1))

(defn length [seq]
  (-reduce (fn [r n] (+ r 1)) 0 seq))
(defn count-leaves [tree]
  (-reduce + 0 (-map #(if (seq? %)
                        (count-leaves %)
                        1)
                     tree)))
(def fold-right -reduce)
(defn fold-left [op initial seq]
  (let [iter (fn iter [ret seq]
               (if (empty? seq)
                 ret
                 (iter (op ret (first seq))
                       (rest seq))))]
    (iter initial seq)))

;; nested mapping
(defn flatmap [proc seq]
  (-reduce -concat
           '()
           (map proc seq)))
(defn prime-sum? [pair]
  (prime? (+ (first pair) (second pair))))
(defn make-pair-sum [pair]
  (list (first pair)
        (second pair)
        (+ (first pair) (second pair))))
(defn prime-sum-pairs [n]
  (-map make-pair-sum
        (-filter prime-sum?
                 (flatmap
                  (fn [i]
                    (map #(list i %)
                         (range 1 i)))
                  (range 1 (inc n))))))

(defn -remove [x s]
  (-filter #(not (= x %)) s))

(defn permutations
  "排列。permutation of {1,2} are {1,2}, {2,1}"
  [s]
  (if (empty? s)
    '(())
    (flatmap
     (fn [x]
       (map #(cons x %)
            (permutations (-remove x s))))
     s)))

;; eight-queens puzzle
(declare empty-board safe? adjoin-position)
(defn queens
  "假设前k-1列都ok了, 第k列尝试每一个可能的row, filter掉不ok的"
  [board-size]
  (let [queen-cols
        ;; (col row) position of first k cols
        (fn queen-cols [k]
          (if (zero? k)
            (list empty-board)          ; '(())
            (-filter
             #(safe? k %)
             (flatmap (fn [rest-of-queens]
                        ;; 列出第k列所有的可能性
                        (map #(adjoin-position % k rest-of-queens)
                             ;; [1, board-size] rows
                             (range 1 (inc board-size))))
                      (queen-cols (dec k))))))]
    (queen-cols board-size)))

(def empty-board '())
(defn safe?
  [k cols]
  (let [position (first cols)]
    (-reduce #(and %1
                   (not
                    (or (= (first %2) (first position))
                        (= (second %2) (second position))
                        ;; 同一斜线
                        (= (Math/abs (- (first position)
                                        (first %2)))
                           (Math/abs (- (second position)
                                        (second %2)))))))
             true
             (rest cols))))
(defn adjoin-position
  [new-row k rest-of-queens]
  (cons (list k new-row) rest-of-queens))


;;; A picture language
(declare beside below wave rogers flip-vert flip-horiz)
(defn rotate180
  [painter]
  (flip-horiz (flip-vert painter)))
(defn flipped-pairs
  [painter]
  (let [painter2 (beside painter (flip-vert painter))]
    (below painter2 painter2)))

(defn right-split
  [painter n]
  (if (zero? n)
    painter
    (let [smaller (right-split painter (dec n))]
      (beside painter (below smaller smaller)))))
(defn up-split
  [painter n]
  (if (zero? n)
    painter
    (let [smaller (up-split painter (dec n))]
      (below painter (beside smaller smaller)))))
(defn corner-split
  [painter n]
  (if (zero? n)
    painter
    (let [up (up-split painter (dec n))
          right (right-split painter (dec n))]
      (let [top-left (beside up up)
            bottom-right (below right right)
            corner (corner-split painter (dec n))]
        (beside (below painter top-left)
                (below bottom-right corner))))))

(defn square-limit
  [painter n]
  (let [quarter (corner-split painter n)]
    (let [half (beside (flip-horiz quarter) quarter)]
      (below (flip-vert half) half))))

;; extract abstraction
(defn square-of-four
  "正方形的四角"
  [tl tr bl br]
  (fn [painter]
    (let [top (beside (tl painter) (tr painter))
          bottom (beside (bl painter) (br painter))]
      (below bottom top))))
;; rewrite flipped-pairs
(defn flipped-pairs
  [painter]
  (let [combine4 (square-of-four identity flip-vert
                                 identity flip-vert)]))
;; rewrite square-limit
(defn square-limit
  [painter n]
  (let [combine4 (square-of-four flip-horiz identity
                                 rotate180 flip-vert)]
    (combine4 (corner-split painter n))))

;; extract split abstraction
(defn split
  [op1 op2]
  (fn do-split [painter n]
    (let [smaller (do-split (dec n))]
      (op1 smaller (op2 smaller)))))
(def right-split (split beside below))
(def up-split (split below beside))

;;; frames
(declare xcor-vect ycor-vect add-vect sub-vect scale-vect origin-frame edge1-frame edge2-frame)
(defn frame-coord-map
  [frame]
  (fn [v]
    (add-vect
     (origin-frame frame)
     (add-vect (scale-vect (xcor-vect v)
                           (edge1-frame frame))
               (scale-vect (ycor-vect v)
                           (edge2-frame frame))))))
(defn make-vect [x y]
  (list x y))
(defn xcor-vect [v] (first v))
(defn ycor-vect [v] (second v))
(defn add-vect [v1 v2]
  (list (+ (first v1) (first v2))
        (+ (second v1) (second v2))))
(defn sub-vect [v1 v2]
  (list (- (first v1) (first v2))
        (- (second v1) (second v2))))
(defn scale-vect [scalar v]
  (list (* scalar (first v))
        (* scalar (second v))))
(defn make-frame
  [origin edge1 edge2]
  (list origin edge1 edge2))
(defn origin-frame [frame] (first frame))
(defn edge1-frame [frame] (second frame))
(defn edge2-frame [frame] (nth frame 2))

;;; painters
(declare make-segment start-segment end-segment draw-line)
(defn segments->painter
  [segment-list]
  (fn [frame]
    (-each
     (fn [segment]
       (draw-line
        ((frame-coord-map frame) (start-segment segment))
        ((frame-coord-map frame) (end-segment segment))))
     segment-list)))
