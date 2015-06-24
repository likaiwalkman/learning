(ns sicp.ch2
  (:require [sicp.ch1 :refer :all]))

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

(defn -concat [seq1 & seqs]
  (if (empty? seqs)
    seq1
    (let [new-seq (-reduce #(cons %2 %1) (first seqs) seq1)]
      (apply -concat new-seq (rest seqs)))))

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

(defn make-segment [start end] (list start end))
(defn start-segment [segment] (first segment))
(defn end-segment [segment] (second segment))

;; The painter that draws the outline of the designated frame.
(segments->painter '((make-segment (make-vect 0 0) (make-vect 0 1))
                     (make-segment (make-vect 0 0) (make-vect 1 0))
                     (make-segment (make-vect 0 1) (make-vect 1 1))
                     (make-segment (make-vect 1 0) (make-vect 1 1))))

;; The painter that draws an ``X'' by connecting opposite corners of the frame.
(segments->painter '((make-segment (make-vect 0 0) (make-vect 1 1))
                     (make-segment (make-vect 0 1) (make-vect 1 0))))

;; The painter that draws a diamond shape by connecting the midpoints of the sides of the frame.
(segments->painter '((make-segment (make-vect 0 0.5) (make-vect 0.5 0))
                     (make-segment (make-vect 0 0.5) (make-vect 0.5 1))
                     (make-segment (make-vect 0.5 0) (make-vect 1 0.5))
                     (make-segment (make-vect 0.5 1) (make-vect 1 0.5))))

;;; painter transform
(defn transform-painter
  [painter origin corner1 corner2]
  (fn [frame]
    (let [m (frame-coord-map frame)
          new-origin (m origin)]
      (painter
       (make-frame new-origin
                   (sub-vect (m corner1) new-origin)
                   (sub-vect (m corner2) new-origin))))))
(defn flip-vert
  [painter]
  (transform-painter painter
                     (make-vect 0 1)
                     (make-vect 1 1)
                     (make-vect 0 0)))
(defn shrink-to-upper-right
  [painter]
  (transform-painter painter
                     (make-vect 0.5 0.5)
                     (make-vect 1 0.5)
                     (make-vect 0.5 1)))
(defn rotate90
  [painter]
  (transform-painter painter
                     (make-vect 1 0)
                     (make-vect 1 1)
                     (make-vect 0 0)))
(defn squash-inwards
  [painter]
  (transform-painter painter
                     (make-vect 0.0 0.0)
                     (make-vect 0.65 0.35)
                     (make-vect 0.35 0.65)))

;; beside
(defn beside
  [painter1 painter2]
  (let [split-point (make-vect 0.5 0)
        paint-left (transform-painter painter1
                                      (make-vect 0 0)
                                      split-point
                                      (make-vect 0 1))
        paint-right (transform-painter painter2
                                       split-point
                                       (make-vect 1 0)
                                       (make-vect 0.5 1))]
    (fn [frame]
      (paint-left frame)
      (paint-right frame))))

(defn flip-horiz
  [painter]
  (transform-painter painter
                     (make-vect 1 0)
                     (make-vect 0 0)
                     (make-vect 1 1)))

(defn below [painter1 painter2]
  (let [split-point (make-vect 0 0.5)
        paint-down (transform-painter painter1
                                      (make-vect 0 0)
                                      (make-vect 1 0)
                                      split-point)
        paint-up (transform-painter painter2
                                    split-point
                                    (make-vect 1 0)
                                    (make-vect 0 1))]
    (fn [frame]
      (paint-down frame)
      (paint-up frame))))


;;;; 2.3 Symbolic Data
;;; differentiation
(declare variable? same-variable? sum? product? make-sum make-product addend augend multiplier multiplicand)
(defn deriv [exp var]
  (cond
    (number? exp) 0
    (variable? exp) (if (same-variable? exp var) 1 0)
    (sum? exp) (make-sum (deriv (addend exp) var)
                         (deriv (augend exp) var))
    (product? exp) (make-sum (make-product (multiplier exp)
                                           (deriv (multiplicand exp) var))
                             (make-product (deriv (multiplier exp) var)
                                           (multiplicand exp)))
    :else (throw (Exception. "unknown expression type -- DERIV"))))

(defn variable? [v] (symbol? v))
(defn same-variable? [v1 v2]
  (and (variable? v1)
       (variable? v2)
       (= v1 v2)))
(defn sum? [exp]
  (and (seq? exp)
       (= '+ (first exp))))
(defn product? [exp]
  (and (seq? exp)
       (= '* (first exp))))
(defn make-sum [e1 e2] (list '+ e1 e2))
(defn make-product [e1 e2] (list '* e1 e2))
(defn addend [sum] (second sum))
(defn augend [sum]
  (let [aug (rest (rest sum))]
    (if (== 1 (count aug))
      (first aug)
      (cons '+ aug))))
(defn multiplier [product] (second product))
(defn multiplicand [product]
  (let [cand (rest (rest product))]
    (if (== 1 (count cand))
      (first cand)
      (cons '* cand))))


;; rewrite for simplification
(defn make-sum [e1 e2]
  (cond
    (and (number? e1) (zero? e1)) e2
    (and (number? e2) (zero? e2)) e1
    (and (number? e1)
         (number? e2)) (+ e1 e2)
         :else (list '+ e1 e2)))
(defn make-product [e1 e2]
  (cond
    (and (number? e1) (number? e2)) (* e1 e2)
    (and (number? e1) (== 1 e1)) e2
    (and (number? e2) (== 1 e2)) e1
    (and (number? e1) (zero? e1)) 0
    (and (number? e2) (zero? e2)) 0
    :else (list '* e1 e2)))

;;;; 2.3.3 Representing Sets

;;; unordered lists
(defn element-of-set?
  "O(n). Check if a set contains an element"
  [x set]
  (cond
    (empty? set) false
    (= x (first set)) true
    :else (element-of-set? x (rest set))))

(defn adjoin-set
  [x set]
  (if (element-of-set? x set)
    set
    (cons x set)))

;; O(n^2), for calling element-of-set? n times
(defn intersection-set
  [set1 set2]
  (cond
    (or (empty? set1) (empty? set2))
    '()

    (element-of-set? (first set1) set2)
    (cons (first set1)
          (intersection-set (rest set1) set2))

    :else
    (intersection-set (rest set1) set2)))

;; O(n^2)
(defn union-set
  [set1 set2]
  (cond
    (empty? set1)
    set2

    (empty? set2)
    set1

    (element-of-set? (first set1) set2)
    (union-set (rest set1) set2)

    :else
    (cons (first set1)
          (union-set (rest set1) set2))))

;;; ordered set
(defn element-of-set?
  [x set]
  (cond
    (empty? set) false
    (= x (first set)) true
    ;; add this check to increase performance
    (< x (first set)) false
    :else (element-of-set? x (rest set))))

;; O(n) now, don't need to check element-of-set?
(defn intersection-set
  [set1 set2]
  (if (or (empty? set1) (empty? set2))
    '()
    (let [x1 (first set1)
          x2 (first set2)]
      (cond
        (= x1 x2)
        (cons x1 (intersection-set (rest set1) (rest set2)))

        (< x1 x2)
        (intersection-set (rest set1) set2)

        (> x1 x2)
        (intersection-set x1 (rest set2))))
    ))

;; better adjoin-set
(defn adjoin-set
  [x set]
  (cond
    (empty? set)
    (cons x set)

    (= x (first set))
    set

    (< x (first set))
    (cons x set)

    ;; x > (first set)
    :else
    (cons (first set) (adjoin-set x (rest set)))))

;; O(n) union
(defn union-set
  [set1 set2]
  (cond
    (empty? set1)
    set2

    (empty? set2)
    set1

    :else
    (let [x1 (first set1)
          x2 (first set2)]
      (cond
        (= x1 x2)
        (cons x1 (union-set (rest set1) (rest set2)))

        (< x1 x2)
        (cons x1 (union-set (rest set1) set2))

        (> x1 x2)
        (cons x2 (union-set x1 (rest set2)))))))


;;; Sets as binary trees
;; use (entry left right) to represent tree
(defn entry [tree] (first tree))
(defn left-branch [tree] (second tree))
(defn right-branch [tree] (nth tree 2))
(defn make-tree
  ([entry] (list entry '() '()))
  ([entry left right]
   (list entry left right)))

;; O(log n)
(defn element-of-set?
  [x set]
  (cond
    (empty? set)
    false

    (= x (entry set))
    true

    (< x (entry set))
    (element-of-set? x (left-branch set))

    (> x (entry set))
    (element-of-set? x (right-branch set))))

;; O(log n) only if the tree is balanced, this is not guaranteed
(defn adjoin-set
  [x set]
  (cond
    (empty? set)
    (make-tree x)

    (= x (entry set))
    set

    (< x (entry set))
    (make-tree
     (entry set)
     (adjoin-set x (left-branch set))
     (right-branch set))

    (> x (entry set))
    (make-tree
     (entry set)
     (left-branch set)
     (adjoin-set x (right-branch set)))))

(defn tree->list-1
  [tree]
  (if (empty? tree)
    '()
    (-concat (tree->list-1 (left-branch tree))
             (cons (entry tree)
                   (tree->list-1 (right-branch tree))))))
(defn tree->list-2
  [tree]
  (let [copy-to-list
        (fn copy-to-list [tree result-list]
          (if (empty? tree)
            result-list
            (copy-to-list (left-branch tree)
                          (cons (entry tree)
                                (copy-to-list (right-branch tree)
                                              result-list)))))]
    (copy-to-list tree '())))

(defn partial-tree [elts n]
  (if (= n 0)
    (cons '() elts)
    (let [left-size (int (/ (- n 1) 2))
          left-result (partial-tree elts left-size)
          left-tree (first left-result)
          non-left-elts (rest left-result)
          right-size (- n (+ left-size 1))
          this-entry (first non-left-elts)
          right-result (partial-tree (rest non-left-elts)
                                     right-size)
          right-tree (first right-result)
          remaining-elts (rest right-result)]
      (cons (make-tree this-entry left-tree right-tree)
            remaining-elts))))
(defn list->tree
  [elements]
  (first (partial-tree elements (count elements))))

;; balanced binary tree
;; O(n)
(defn union-set
  [set1 set2]
  (let [-union-set
        (fn -union-set
          [set1 set2]
          (cond
            (empty? set1)
            set2

            (empty? set2)
            set1

            :else
            (let [x (entry set1)
                  left (left-branch set1)
                  right (right-branch set1)
                  set (adjoin-set x set2)]
              (-union-set left (-union-set right set)))))
        new-set (-union-set set1 set2)]
    (list->tree (tree->list-1 new-set))))

(defn intersection-set
  [set1 set2]
  (let [-intersection-set
        (fn -intersection-set
          [set1 set2]
          (if (or (empty? set1)
                  (empty? set2))
            '()

            ;; else
            (let [x1 (entry set1)
                  x2 (entry set2)
                  left1 (left-branch set1)
                  left2 (left-branch set2)
                  right1 (right-branch set1)
                  right2 (right-branch set2)
                  ]
              (cond
                (= x1 x2)
                (-concat (list x1)
                         (-intersection-set left1 left2)
                         (-intersection-set right1 right2))

                (< x1 x2)
                (-concat (-intersection-set set1 left2)
                         (-intersection-set right1 right2))

                (> x1 x2)
                (-concat (-intersection-set left1 set2)
                         (-intersection-set right1 right2))))))]
    (list->tree (-intersection-set set1 set2))))

;; Sets and information retrieval
(def -key identity)
(defn lookup [k set]
  (cond
    (empty? set)
    false

    (= k (-key (entry set)))
    (entry set)

    (< k (-key (entry set)))
    (lookup k (left-branch set))

    (> k (-key (entry set)))
    (lookup k (right-branch set))))
