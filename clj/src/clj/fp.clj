(ns clj.fp
  (:use (clojure [pprint :only (pprint)])))
;;; Functional Programming
;;-----------------------------------------------------------
;; map, accept one func, and one or more collections
(map clojure.string/lower-case ["This" "Is" "a" "TesT"]) ; => ("this" "is" "a" "test")
(map * [1 2 3 4] [5 6 7])                                ; => (5 12 21)
;; reduce
(reduce #(cond
           (and (< %1 0) (< %2 0)) 0
           (< %1 0) %2
           (< %2 0) %1
           true (+ %1 %2)) [1 -2 3 -4]) ; => 4
;; can optionally provide an initial value
(reduce + 30 [1 2 3 4])                 ; => 40
(reduce (fn [map v]
          (assoc map v (* v v)))
        {} [1 2 3 4])                   ; => {4 16, 3 9, 2 4, 1 1}

;; apply, apply will take arguments from an vector
(apply * 2 3 [2 3])                     ; => 36
(#(apply map * %&) [1 2 3] [4 5 6] [7 8 9])
(apply map * [[1 2 3] [2 2 2]])

;; partial
(def only-string (partial filter string?))
(only-string [1 "2" 3 5 "b"])           ; => ("2" "b")

;; comp, like a pipline, from right to left, accepted args is decided by last func
(def negated-sum-str (comp str - +))
(negated-sum-str 1 2 3 4)               ; => "-10"
;; is the same as:
((fn [& rest]
   (str (- (apply + rest)))) 1 2 3 4)   ; => "-10"

;; camel -> keyword
(def camel->keyword
  (comp
   keyword
   clojure.string/join
   (partial interpose "-")
   (partial map clojure.string/lower-case)
   #(clojure.string/split % #"(?<=[a-z])(?=[A-Z])")))
(camel->keyword "TestKeyword")          ; => :test-keyword
(camel->keyword "thisIsTest")           ; => :this-is-test

;; ->> macro
((fn [s]
   (->>
    (clojure.string/split s #"(?<=[a-z])(?=[A-Z])")
    (map clojure.string/lower-case)
    (interpose "-")
    clojure.string/join
    keyword
    )) "thisIsTest")                    ; => :this-is-test
(macroexpand '(->> (op1 arg) (op2 a) op3)) ; => (op3 (op2 a (op1 arg)))
(macroexpand '(-> (op1 arg) (op2 a) op3))  ; => (op3 (op2 (op1 arg) a))

(def camel-pairs->map
  (comp
   (partial apply hash-map)
   (partial map-indexed (fn
                          [i v]
                          (if (odd? i)
                            v
                            (camel->keyword v))))))
(camel-pairs->map ["CamelTest" 1 "AnotherTest" 2]) ; => {:camel-test 1, :another-test 2}

;;; High-Order Functions
;;-----------------------------------------------------------
;; build a log system
(defn print-logger
  [writer]
  ;; after binding exit, *out* will be the original value
  #(binding [*out* writer]
     (println %)))
(def *out*-logger
  (print-logger *out*))
(*out*-logger "test")                   ; "test"

;; log to a string
(def writer (java.io.StringWriter.))
(def retained-logger (print-logger writer))
(retained-logger "hi there, StringWriter") ; => nil
(str writer)                               ; => "hi there, StringWriter\n"

;; log to a file
(defn file-logger
  [file]
  #(with-open [f (clojure.java.io/writer file :append true)]
     ((print-logger f) %)))
(def log->file (file-logger "message.log"))
(log->file "haha")

;; multi loggers
(defn multi-logger
  [& logger-fns]
  #(doseq [f logger-fns]
     (f %)))
(def log (multi-logger
          (print-logger *out*)
          (file-logger "message.log")))
(log "this is a test")

;; timestamp logger
(defn timestamped-logger
  [logger]
  #(logger (format "[%1$tY-%1$tm-%1$te %1$tH:%1$tM:%1$tS] %2$s"
                   (java.util.Date.) %)))
(def log-timestamped
  (timestamped-logger log))
(log-timestamped "now test timestamped log")

;;; Pure Functions, no side effects
;; memoize, puer functions are suit for memoize
(defn prime?
  [n]
  (cond
    (== 1 n) false
    (== 2 n) true
    (even? n) false
    :else (->> (range 3 (inc (Math/sqrt n)) 2)
               (filter #(zero? (rem n %)))
               empty?)))
(time (prime? 1125899906842679))        ; => true
(let [m-prime? (memoize prime?)]
  (time (m-prime? 1125899906842679))    ; "Elapsed time: 842.35 msecs"
  (time (m-prime? 1125899906842679)))   ; "Elapsed time: 0.008 msecs"

;;;; Collections and Data Structures
;;-----------------------------------------------------------
;; Abstractions over implementations
(def v [1 2 3])
(conj v 4 5)                            ; => [1 2 3 4 5]
(seq v)                                 ; => (1 2 3)
(def m {:a 5 :b 6})                     ; => #'clj.core/m
(conj m [:c 7])                         ; => {:c 7, :b 6, :a 5}
(seq m)                                 ; => ([:b 6] [:a 5])
(def s #{1 2 3})                        ; => #'clj.core/s
(conj s 4)                              ; => #{1 4 3 2}
(conj s 3 4)                            ; => #{1 4 3 2}
(seq s)                                 ; => (1 3 2)
(def lst '(1 2 3))                      ; => #'clj.core/lst
(conj lst 0 4)                          ; => (4 0 1 2 3)
(seq lst)                               ; => (1 2 3)
;; into
(into v [4 5])                          ; => [1 2 3 4 5]
(into lst '(4 5))                       ; => (5 4 1 2 3)
(into m [[:c 7] [:d 8]])                ; => {:c 7, :b 6, :d 8, :a 5}
(into #{1 2} [2 3 1])                   ; => #{1 3 2}
(into [1] {:a 1 :b 2})                  ; => [1 [:b 2] [:a 1]]

;;; Collection
;; All data structures in Clojure participate in the common `collection' abstraction
;; they all can use conj, seq, count, empty, etc functions
(conj '(1 2) '(3))                      ; => ((3) 1 2)
(into '(1 2) ['(3)])                    ; => ((3) 1 2)

;; (empty coll), create a empty collection with the same type of coll
(defn swap-pairs
  [sequential]
  (into (empty sequential)
        (interleave
         (take-nth 2 (drop 1 sequential))
         (take-nth 2 sequential))))
(swap-pairs (apply list (range 10)))    ; => (8 9 6 7 4 5 2 3 0 1)
(swap-pairs (apply vector (range 10)))  ; => [1 0 3 2 5 4 7 6 9 8]

(defn map-map
  [f m]
  (into (empty m)
        (for [[k v] m]
          [k (f v)])))
(map-map inc (hash-map :a 1 :b 2 :c 5))   ; => {:c 6, :b 3, :a 2}
(map-map inc (sorted-map :a 1 :b 2 :c 5)) ; => {:a 2, :b 3, :c 6}

;; count
(count [1 2 3])                         ; => 3
(count {:a 1 :b 2 :c 3})                ; => 3
(count #{1 2 3})                        ; => 3
(count '(1 2 3))                        ; => 3

;;; Sequences
;; seq
(seq "clojure")                         ; => (\c \l \o \j \u \r \e)
(seq {:a 3 :b 2})                       ; => ([:b 2] [:a 3])
(seq (java.util.ArrayList. (range 3)))  ; => (0 1 2)
(seq (into-array ["Clojure" "Programming"])) ; => ("Clojure" "Programming")
(seq nil)                                    ; => nil
(seq [])                                     ; => nil

;; some functions will call `seq' implicitly, for example:
(map clojure.string/upper-case "clojure") ; => ("C" "L" "O" "J" "U" "R" "E")

(first "clojure")                       ; => \c
(rest "clojure")                        ; => (\l \o \j \u \r \e)
(next "clojure")                        ; => (\l \o \j \u \r \e)
(rest [])                               ; => ()
(next [])                               ; => nil

;; sequences are not iterators
(doseq [x (range 3)]
  (println x))
(let [r (range 3)
      rst (rest r)]
  (pr (map str rst))
  (pr (map #(+ 100 %) r))
  (pr (conj r -1) (conj rst -42)))
;; sequences are not lists, lists always track their size
(let [s (range 1e6)]
  (time (count s)))                     ; "Elapsed time: 62.184 msecs"
(let [s (apply list (range 1e6))]
  (time (count s)))                     ; "Elapsed time: 0.019 msecs"
;; create sequences
(cons 0 (range 1 5))                    ; => (0 1 2 3 4)
(cons :a [:b :c :d])                    ; => (:a :b :c :d)
(cons 0 (cons 1 (cons 2 [])))           ; => (0 1 2)
(list* 0 1 2 [])                        ; => (0 1 2)

;; lazy seq, it's value will realized when access the value, once realized, it will retain
(defn random-ints
  [limit]
  (lazy-seq
   (println "realizing random number")
   (cons (rand-int limit)
         (random-ints limit))))
(def rands (take 10 (random-ints 50)))
(first rands)                           ; => 5
(nth rands 3)                           ; => 1
(count rands)                           ; => 10
;; same as:
(def rands (repeatedly 10 (fn
                            []
                            (println "realizing...")
                            (rand-int 50))))
;; next will check the head of the seq(to check if empty)
(def x (next (random-ints 50)))         ; realizing random number\n realizing random number
(def x (rest (random-ints 50)))         ; realizing random number
;; Sequential destructuring always use `next'
(let [[x & rest] (random-ints 50)])     ; realizing random number\n realizing random number

;; split-with
(split-with neg? (range -5 5))          ; => [(-5 -4 -3 -2 -1) (0 1 2 3 4)]
;; Head retention, when reference the head of a seq, all of the seq elements will not be GC
(let [[t d] (split-with #(< % 12) (range 1e8))]
  ;; below will cause memory issue, because when counting d(large size),
  ;; t are referencing the head of seq that range generated
  #_[(count d) (count t)]
  ;; below will not have issue, when counting d, t(small size) is finished and released
  #_[(count t) (count d)])                ; => [2 5]

;;; Associate
(def m {:a 1, :b 2, :c 3})              ; => #'clj.core/m
(get m :b)                              ; => 2
(get m :d)                              ; => nil
(get m :d "not-found")                  ; => "not-found"
(assoc m :d 4 :e 5)                     ; => {:e 5, :c 3, :b 2, :d 4, :a 1}
(dissoc m :b :a)                        ; => {:c 3}
;; also works for vector
(def v [1 2 3])
(get v 1)                               ; => 2
(get v 10)                              ; => nil
(get v 10 "not-found")                  ; => "not-found"
(assoc v 1 4 3 5)                       ; => [1 4 3 5]
;; for set
(get #{'a 'b 'c} 'a)                    ; => a
(get #{'a 'b 'c} 't)                    ; => nil
(when (get #{'a 'b 'c} 'b)
  (println "it contains `b'"))          ; it contians `b'

;;; contains? check if contains specific `key'(not checking value)
(contains? [1 2 3] 0)                   ; => true
(contains? {:a 3 :b 4} :b)              ; => true
(contains? #{1 2 3} 0)                  ; => false
(get "clojure" 3)                       ; => \j
(contains? (java.util.HashMap.) "not-there") ; => false
(get (into-array [1 2 3]) 0)                 ; => 1
;; beware of nil value, or false value
(get {:a nil} :a)                       ; => nil
(find {:a nil} :a)                      ; => [:a nil]
;; find is used for destructuring
(if-let [e (find {:a 3 :b 4} :a)]
  (format "found %s=>%s" (key e) (val e))
  "not found")                          ; => "found :a=>3"
(if-let [[k v] (find {:a 3 :b 4} :a)]
  (format "found %s=>%s" k v)
  "not found")                          ; => "found :a=>3"

;;; Indexed are not recommended. nth is used for index, while get is more general
(nth [:a :b :c] 2)                      ; => :c
(get [:a :b :c] 2)                      ; => :c
#_(nth [:a :b :c] 3)                    ; java.lang.IndexOutOfBoundsException
(get [:a :b :c] 3)                      ; => nil
;; nth = get when give default value
(nth [:a :b :c] 3 :not-found)           ; => :not-found
(get [:a :b :c] 3 :not-found)           ; => :not-found

;;; Stack, list or vector can be used as Stack
;; list as stack
(conj '() 1)                            ; => (1)
(conj '(2 1) 3)                         ; => (3 2 1)
(peek '(3 2 1))                         ; => 3
(pop '(3 2 1))                          ; => (2 1)
;; vector as stack
(conj [1 2] 3)                          ; => [1 2 3]
(peek [1 2 3])                          ; => 3
(pop [1 2 3])                           ; => [1 2]

;;; Set
(get #{1 2 3} 2)                        ; => 2
(disj #{1 2 3} 2 1 5)                   ; => #{3}

;;; Sorted
(def sm (sorted-map :z 5 :x 9 :y 0 :b 2 :a 3 :c 4)) ; => #'clj.core/sm
sm                                     ; => {:a 3, :b 2, :c 4, :x 9, :y 0, :z 5}
(rseq sm)                       ; => ([:z 5] [:y 0] [:x 9] [:c 4] [:b 2] [:a 3])
(subseq sm <= :c)               ; => ([:a 3] [:b 2] [:c 4])
(subseq sm > :b <= :y)          ; => ([:c 4] [:x 9] [:y 0])
(rsubseq sm > :b <= :y)         ; => ([:y 0] [:x 9] [:c 4])

;; compare
(compare 1 2)                           ; => -1
(compare "ac" "ab")                     ; => 1
(compare [1 1 4] [1 2 3])               ; => -1
;; sort
(sort < (repeatedly 10 #(rand-int 100))) ; => (0 14 19 23 24 31 44 52 61 89)
(sort-by first > (map-indexed vector "Clojure")) ; => ([6 \e] [5 \r] [4 \u] [3 \j] [2 \o] [1 \l] [0 \C])
(sorted-map-by compare :z 5 :x 7 :y 3 :b 2 :a 3 :c 7) ; => {:a 3, :b 2, :c 7, :x 7, :y 3, :z 5}
(sorted-map-by (comp - compare) :z 5 :x 7 :y 3 :b 2 :a 3 :c 7) ; => {:z 5, :y 3, :x 7, :c 7, :b 2, :a 3}

(defn magnitude
  [x]
  (->> x Math/log10 Math/floor))
(magnitude 100)                         ; => 2.0
(magnitude 11)                          ; => 1.0
(defn compare-magnitude
  [a b]
  (< (magnitude a) (magnitude b)))
((comparator compare-magnitude) 10 100) ; => -1
((comparator compare-magnitude) 100 77) ; => 1
((comparator compare-magnitude) 10 77)  ; => 0
(compare-magnitude 100 10) ; => false

(def s (sorted-set-by compare-magnitude 10 100 30))
(conj s 600)                            ; => #{10 100}
(disj s 750)                            ; => #{10}
(contains? s 135)                       ; => true

;; interpolate
(defn interpolate
  "Takes a collection of points (as [x y] tuples), returning a function
  which is a linear interpolation between those points."
  [points]
  (let [results (into (sorted-map) (map vec points))]
    (fn [x]
      (let [[xa ya] (first (rsubseq results <= x))
            [xb yb] (first (subseq results > x))]
        (pr xa " " xb " " ya " " yb)
        (if (and xa xb)
          (/ (+ (* ya (- xb x)) (* yb (- x xa)))
             (- xb xa))
          (or ya yb))))))
(def f (interpolate [[0 0] [10 10] [15 5]]))
(map f [2 10 12])                       ; => (2 10 8)

;;; Concise Collections access
;; collections are functions
([:a :b :c] 2)                          ; => :c
({:a 1 :b 2} :c 3)                      ; => 3
;; keywords are functions
(:a {:a 2})                             ; => 2
(#(:foo %) nil)                         ; => nil
#_(#(% :foo) nil)                       ; NullPointerException
;; collections and keys are high-order functions
(map :name [{:name "Martin" :age 26}
            {:name "Daisy" :age 26}
            {:name "No" :age 44}])      ; => ("Martin" "Daisy" "No")
(some #{1 2 3} [5 3 4])                 ; => 3
(filter :age [{:age 20} {:name "t"}])   ; => ({:age 20})
(filter (comp (partial >= 30) :age)
        [{:name "Martin" :age 26}
         {:name "Daisy" :age 26}
         {:name "No" :age 44}]) ; => ({:age 26, :name "Martin"} {:age 26, :name "Daisy"})

;; beaware of nil/false
(remove #{false 3 5} (cons false (range 5))) ; => (false 0 1 2 4)
(remove (partial contains? #{false 3 5}) (cons false (range 5))) ; => (0 1 2 4)

;;; Data Structure Types
;; lists are linked list
'(1 2 (+ 1 2))                          ; => (1 2 (+ 1 2))
(list 1 2 (+ 1 2))                      ; => (1 2 3)
;; Vectors, just match the expectations to java.util.ArrayList
(vector 1 2 3)                          ; => [1 2 3]
(vec (range 3))                         ; => [0 1 2]
;; Sets
(hash-set 1 2 3 1 3)                    ; => #{1 3 2}
(set [1 2 3 1 3])                       ; => #{1 3 2}
;; Maps
(keys {:a 1 :b 2})                      ; => (:b :a)
(vals {:a 1 :b 2})                      ; => (2 1)
(group-by #(rem % 3) (range 10))        ; => {0 [0 3 6 9], 1 [1 4 7], 2 [2 5 8]}
(group-by (juxt :name :age) [{:name "Martin" :age 26}
                             {:name "Daisy" :age 26}]) ; => {["Martin" 26] [{:age 26, :name "Martin"}], ["Daisy" 26] [{:age 26, :name "Daisy"}]}
(def orders
  [{:product "Clock", :customer "Wile Coyote", :qty 6, :total 300}
   {:product "Dynamite", :customer "Wile Coyote", :qty 20, :total 5000}
   {:product "Shotgun", :customer "Elmer Fudd", :qty 2, :total 800}
   {:product "Shells", :customer "Elmer Fudd", :qty 4, :total 100}
   {:product "Hole", :customer "Wile Coyote", :qty 1, :total 1000}
   {:product "Anvil", :customer "Elmer Fudd", :qty 2, :total 300}
   {:product "Anvil", :customer "Wile Coyote", :qty 6, :total 900}]) ; => #'clj.core/orders
(defn reduce-by
  [key-fn f init coll]
  (reduce (fn [summaries x]
            (let [k (key-fn x)]
              (assoc summaries k (f (summaries k init) x))))
          {} coll))
(reduce-by :customer #(+ %1 (:total %2)) 0 orders) ; => {"Elmer Fudd" 1200, "Wile Coyote" 7200}
(reduce-by (juxt :customer :product)
           #(+ %1 (:total %2)) 0 orders) ; => {["Wile Coyote" "Anvil"] 900, ["Elmer Fudd" "Anvil"] 300, ["Wile Coyote" "Hole"] 1000, ["Elmer Fudd" "Shells"] 100, ["Elmer Fudd" "Shotgun"] 800, ["Wile Coyote" "Dynamite"] 5000, ["Wile Coyote" "Clock"] 300}

(get-in {:a {:b 2} :c 3} [:a :b])       ; => 2
(assoc-in {} [:a :b :c] 2)              ; => {:a {:b {:c 2}}}
(defn reduce-by-in
  [keys-fn f init coll]
  (reduce (fn [summaries x]
            (let [ks (keys-fn x)]
              (assoc-in summaries ks
                        (f (get-in summaries ks init) x))))
          {} coll))
(reduce-by-in (juxt :customer :product)
              #(+ %1 (:total %2)) 0 orders) ; => {"Elmer Fudd" {"Anvil" 300, "Shells" 100, "Shotgun" 800}, "Wile Coyote" {"Anvil" 900, "Hole" 1000, "Dynamite" 5000, "Clock" 300}}

;;; Immutability and Persistence
;; Most of the operations in clojure do not modify data
(def v (vec (range 1e6)))
(count v)                               ; => 1000000
(def v2 (conj v 1e6))                   ; => #'clj.core/v2
(count v2)                              ; => 1000001
(count v)                               ; => 1000000

;; transient
(def x (transient []))                  ; => #'clj.core/x
(def y (conj! x 1))                     ; => #'clj.core/y
(count y)                               ; => 1
(count x)                               ; => 1

(defn naive-into
  [coll source]
  (reduce conj coll source))
(= (naive-into #{} [1 2 3 5])
   (into #{} [1 2 3 5]))                ; => true
;; use do to prevent print 1e6 numbers
(time (do (into #{} (range 1e6)) nil))  ; "Elapsed time: 422.67 msecs"
(time (do (naive-into #{} (range 1e6)) nil)) ; "Elapsed time: 828.696 msecs"
;; fast into by transient
(defn fast-into
  [coll source]
  (persistent! (reduce conj! (transient coll) source)))
(time (do (fast-into #{} (range 1e6)) nil)) ; "Elapsed time: 374.967 msecs"

;; only vectors and unsorted map/set have transient variants
(defn transient-capable?
  [coll]
  (instance? clojure.lang.IEditableCollection coll))
(map transient-capable? [[] {} #{}])                     ; => (true true true)
(map transient-capable? ['() (sorted-map) (sorted-set)]) ; => (false false false)
;; persistent! a transient will makes the source unusable, and further more, one the thread that created a given transient can access/modify the transient
(def tv (transient [1 2]))              ; => #'clj.core/tv
(get tv 0)                              ; => 1
(persistent! tv)                        ; => [1 2]
#_(get tv 0)                            ; java.lang.IllegalAccessError

;;; Metedata
(def a ^{:created (System/currentTimeMillis)} [1 2 3]) ; => #'clj.core/a
a                                                      ; => [1 2 3]
(meta a)                                ; => {:created 1427448121001}
;; short form for true value
(meta ^:private [1 2 3])                ; => {:private true}
(meta ^:private ^:static [1 2 3])       ; => {:private true, :static true}
;; with-meta(replace meta) and vary-meta(update meta)
(def b (with-meta a (assoc (meta a)
                           :modified (System/currentTimeMillis))))
(meta b)                  ; => {:modified 1427448392190, :created 1427448121001}
(def b (vary-meta a assoc :modified (System/currentTimeMillis))) ; => #'clj.core/b
(meta b)                  ; => {:modified 1427448415589, :created 1427448121001}
;; meta will retain when modify data
(meta (conj a 4))                       ; => {:created 1427448121001}


;;-----------------------------------------------------------
;;; Algorithm: Conway's game of life
;;-----------------------------------------------------------
(defn empty-board
  "Creates a rectangular empty board of the specific width and height"
  [w h]
  (vec (repeat w (vec (repeat h nil)))))
(defn populate
  "Turns :on each of the cells specified as [y, x] coordinates"
  [board living-cells]
  (reduce (fn [board coordinates]
            (assoc-in board coordinates :on))
          board living-cells))
(def glider (populate (empty-board 6 6)
                      #{[2 0] [2 1] [2 2] [1 2] [0 1]}))
(clojure.pprint/pprint glider)

(defn neighbours [[x y]]
  (for [dx [-1 0 1] dy [-1 0 1] :when (not= 0 dx dy)]
    [(+ dx x) (+ dy y)]))
(neighbours [3 3])        ; => ([2 2] [2 3] [2 4] [3 2] [3 4] [4 2] [4 3] [4 4])

(defn count-neighbours
  [board loc]
  (count (filter #(get-in board %)
                 (neighbours loc))))
(defn indexed-step
  "Yields the next state of the board, using indices to determine neighbors,
  liveness, etc."
  [board]
  (let [w (count board)
        h (count (first board))]
    (loop [new-board board x 0 y 0]
      (cond
        (>= x w) new-board
        (>= y h) (recur new-board (inc x) 0)
        :else (let [new-liveness (case (count-neighbours board [x y])
                                   2 (get-in board [x y])
                                   3 :on
                                   nil)]
                (recur (assoc-in new-board [x y] new-liveness) x (inc y)))))))
(-> (iterate indexed-step glider) (nth 8) clojure.pprint/pprint)
;; now refactor by FP style
(defn indexed-step2
  [board]
  (let [w (count board)
        h (count (first board))]
    (reduce (fn [new-board x]
              (reduce (fn [new-board y]
                        (let [new-liveness (case (count-neighbours board [x y])
                                             2 (get-in board [x y])
                                             3 :on
                                             nil)]
                          (assoc-in new-board [x y] new-liveness)))
                      new-board (range h)))
            board (range w))))
(-> (iterate indexed-step2 glider) (nth 8) clojure.pprint/pprint)

(defn indexed-step3
  [board]
  (let [w (count board)
        h (count (first board))]
    (reduce (fn [new-board [x y]]
              (let [new-liveness (case (count-neighbours board [x y])
                                   2 (get-in board [x y])
                                   3 :on
                                   nil)]
                (assoc-in new-board [x y] new-liveness)))
            board
            (for [x (range w) y (range h)]
              [x y]))))
(-> (iterate indexed-step3 glider) (nth 8) clojure.pprint/pprint)

;; partition
(partition 3 1 (range 5))                      ; => ((0 1 2) (1 2 3) (2 3 4))
(partition 3 1 (concat [nil] (range 5) [nil])) ; => ((nil 0 1) (0 1 2) (1 2 3) (2 3 4) (3 4 nil))
(defn window
  "Returns a lazy sequence of 3-item windows centered around each item of coll,
  padded as necessary with pad or nil."
  ([coll] (window nil coll))
  ([pad coll] (partition 3 1 (concat [pad] coll [pad]))))
(window (range 5))            ; => ((nil 0 1) (0 1 2) (1 2 3) (2 3 4) (3 4 nil))

(defn cell-block
  "Creates a sequences of 3x3 windows from a triple of 3 sequences."
  [[left mid right]]
  (window (map vector left mid right)))
(cell-block [[0 1 2] [1 2 3] [2 3 4]])

(defn liveness
  "Returns the liveness (nil or :on) of the center cell for
  the next step."
  [block]
  (let [[_ [_ center _] _] block]
    (case (- (count (filter #{:on} (apply concat block)))
             (if (= :on center) 1 0))
      2 center
      3 :on
      nil)))
(defn- step-row
  "Yields the next state of the center row."
  [rows-triple]
  (vec (map liveness (cell-block rows-triple))))
(defn index-free-step
  "Yields the next state of board"
  [board]
  (vec (map step-row (window (repeat nil) board))))

(= (nth (iterate indexed-step glider) 8)
   (nth (iterate index-free-step glider) 8)) ; => true

;; An elegant implementation of Conway’s Game of Life
(defn step
  "Yields the next state of the world"
  [cells]                               ; eg. #{[2 0] [2 1] [2 2] [1 2] [0 1]}
  (set (for [
             ;; cell is neighbours' neighbour
             ;; 所有:on的cell的neighbours中，某个neighbour出现的次数，等同于该neighbour周围的:on的cell的个数
             [loc n] (frequencies (mapcat neighbours cells))
             :when (or (= n 3)
                       (and (= n 2)
                            (cells loc)))]
         loc)))
(->> (iterate step #{[2 0] [2 1] [2 2] [1 2] [0 1]})
     (drop 8)
     first
     (populate (empty-board 6 6))
     clojure.pprint/pprint)

;; extract a high order function
(defn stepper
  "Returns a step function for Life-like cell automata.
  neighbours takes a location and return a sequential collection of locations.
  survive? and birth? are predicates on the number of living neighbours."
  [neighbours birth? survive?]
  (fn [cells]
    (set (for [
               [loc n] (frequencies (mapcat neighbours cells))
               :when (if (cells loc) (survive? n) (birth? n))
               ]
           loc))))
;; `step' above is equivalent to `(stepper neighbours #{3} #{2 3})'

;; Life-like automaton H.B2/S34 (with a hexagonal grid, birth for 2, survive when 3 or 4)
(defn hex-neighbours
  [[x y]]
  (for [dx [-1 0 1]
        dy (if (zero? dx)
             [-2 2]
             [-1 1])]
    [(+ dx x) (+ dy y)]))
(def hex-step (stepper hex-neighbours #{2} #{3 4}))

(hex-step #{[0 0] [1 1] [1 3] [0 4]})   ; => #{[2 2] [1 5] [1 -1]}
