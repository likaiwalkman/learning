(ns clj.core)

(defn avarage
  [numbers]
  (/ (apply + numbers) (count numbers)))
(avarage [1 2 3 4])                     ; => 5/2

;; serialize and deserialize
(pr-str [1 2 3])                        ; => "[1 2 3]"
(read-string "[1 2 3]")                 ; => [1 2 3]

;;; Scalar Literals
;;-----------------------------------------------------------
;; string
"hello there"                           ; => "hello there"
"multiple line
  string"                               ; => "multiple line\n  string"

;; boolean
true                                    ; => true

;; nil, just like null in java
nil                                     ; => nil

;; character
(class \a)                              ; => java.lang.Character
;; unicode
\u00ff                                  ; => \ÿ
;; octal
\o41                                    ; => \!
;; special characters
\tab                                    ; => \tab
\space                                  ; => \space

;; keywords
;; map
(def pizza {:name "test's"
            :location "sh"
            ::location "martin"})
;; get value by keyword
(:location pizza)                       ; => "sh"
;; with namespace
(:clj.core/location pizza)              ; => "martin"

;; symbols, must start with non-numeric, can contain *, +, -, !, _, ?
;; avarage is a symbol
(avarage [1 2])

;; numbers
42                                      ; => 42
(class 42)                              ; => java.lang.Long
;; 16
0xff                                    ; => 255
;; 2
2r111                                   ; => 7
;; 8
040                                     ; => 32
;; any
5r11                                    ; => 6
;; double
3.14                                    ; => 3.14
;; BigInt
42N                                     ; => 42N
;; BigDecimal
0.01M                                   ; => 0.01M
;; Ratio
3/2                                     ; => 3/2
(* 0.033 100)                           ; => 3.3000000000000003
(* 0.033M 100)                          ; => 3.300M

;; Regular expressions
(re-seq #"\d" "ad13e2a")                ; => ("1" "3" "2")
(re-find #"\d{2}" "ad3e21ab13c")        ; => "21"
(re-matches #".*\.(\w*)\.\w*" "www.baidu.com") ; => ["www.baidu.com" "baidu"]

;; use #_ as form-level comment
(+ 1 #_[a 2] 2)                         ; => 3

;; `,' is treat as whitespace
(= '(1 2 3) '(1,2,3))                   ; => true

;;; Collections
;;-----------------------------------------------------------
;; list
'(a b :name 3.5)                        ; => (a b :name 3.5)
;; vector
['a 'b :name 3.5]                       ; => [a b :name 3.5]
;; map
{:name "martin" :age "25"}              ; => {:age "25", :name "martin"}
;; set
#{1 2 :name}                            ; => #{1 :name 2}

;;; Namespaces
;; define vars(in current namespace), vars are not variables
(def x 1)                               ; => #'clj.core/x
;; current namespace
*ns*                                    ; => #<Namespace clj.core>
java.util.List                          ; => java.util.List
;; all the vars in clojure.core can be accessed without namespace-qualifying
filter                          ; => #<core$filter clojure.core$filter@5bf3255f>

;;; Special forms
;;-----------------------------------------------------------
;; quote
''x                                     ; => (quote x)
(= ''x '(quote x))                      ; => true
'@x                                     ; => (clojure.core/deref x)

;;; Code Blocks
;;-----------------------------------------------------------
;; do
(do
  (println "test")
  (println "hello")
  (apply * [2 3 4]))                    ; => 24

;; def, define vars
(def p "foo")
p                       ; => "foo"

;; let, local bindings
(let
    [a (inc (rand-int 6))
     b (inc (rand-int 6))]
  (println (format "You rolled a %s and b %s" a b))
  (+ a b))
;; _ is used for ignore name binding
(defn hypot
  [x y]
  (let [x2 (* x x)
        _ (println "x2 is" x2)
        y2 (* y y)]
    (Math/sqrt (+ x2 y2))))
(hypot 3 4)                             ; => 5.0

;;; Destructuring
;;-----------------------------------------------------------
(def v [3 'a 5 [4 6]])                  ; => #'clj.core/v
(nth v 2)                               ; => 5
(v 2)                                   ; => 5
;; All of Clojure’s sequential collections implement the java.util.List
(.get v 2)                              ; => 5
;; Sequence destructuring
(let [
      [x y _ [zx zy]] v
      ]
  (println "x is" x)                   ; x is 3
  (println "y is" y)                   ; y is a
  (println "zx is" zx)                 ; zx is 4
  (println "zy is" zy)                 ; zy is 6
  )
;; bind rest of the vector, return a sequence
(let [[x & r] v]
  r)                                    ; => (a 5 [4 6])
;; use :as to bind original vector
(let [[x _ z :as origin] v]
  (conj origin (+ x z)))                ; => [3 a 5 [4 6] 8]

;; Map destructuring
(def m {:a 1 :b 2
        :c [3 4 5]
        :d {:e 6 :f 7}
        "foo" 88
        42 false})                      ; => #'clj.core/m
(let [{a :a b :b c "foo"} m]
  (println "a is" a)                    ; a is 1
  (println "b is" b)                    ; b is 2
  (println "c is" c)                    ; c is 88
  )
;; map destructuring with vector by index
(let [{a 3 b 5} [1 2 3 4 5 6]]
  (println "a is" a)                    ; a is 4
  (println "b is" b))                   ; b is 6
                                        ; nested
(let [{{a :e} :d} m]
  (println "a is" a))                   ; a is 6

;; Retaining the destructured value
(let [{r1 :x r2 :y :as randoms}
      (zipmap [:x :y :z] (repeatedly (partial rand-int 10)))]
  (assoc randoms :sum (+ r1 r2)))       ; => {:sum 9, :z 1, :y 0, :x 9}

;; Default value, when not find is specific source then find in default value
(let [{k :unkwn x :a
       :or {k 50}} m]
  k)                                    ; => 50
;; Binding values by keys' name
(def user {:name "Martin" "age" 26 'location "Shanghai"})
(let [{:keys [name]
       :strs [age]
       :syms [location]} user]
  [name age location])                  ; => ["Martin" 26 "Shanghai"]
;; rest
(def user-info ["Martin" 1989 :city "Shanghai" :age 26])
(let [[name birth & {:keys [city age]}] user-info]
  [city age])                           ; => ["Shanghai" 26]


;;; Functions
;;-----------------------------------------------------------
((fn [x]
   (+ 1 x)) 8)                          ; => 9
(def strange-adder
  (fn ([x] (+ 1 x))
    ([x y] (+ x y))
    ))
(strange-adder 5)                       ; => 6
(strange-adder 3 5)                     ; => 8
(defn concat-rest
  [x & rest]
  (apply str (butlast rest)))
(concat-rest 0 1 2 3 4)                 ; => "123"

(defn make-user
  ;; use vector because the rest args is a list
  [& [user-id]]
  {:user-id (or user-id
                (str (java.util.UUID/randomUUID)))})
(make-user)               ; => {:user-id "ea56a233-deb7-4d8a-93a1-efbc10b2c7ea"}
(make-user "test")        ; => {:user-id "test"}

;; keyword arguments
(defn make-user
  [username & {:keys [email join-date]
               :or {join-date (java.util.Date.)}}]
  {:username username
   :email email
   :join-date join-date
   ;; 2.592e9 -> one month in ms
   :exp-date (java.util.Date. (long (+ 2.592e9 (.getTime join-date))))})
(make-user "martin") ; => {:username "martin", :email nil, :join-date #inst "2015-03-16T14:40:51.991-00:00", :exp-date #inst "2015-04-15T14:40:51.991-00:00"}
(make-user "martin" :email "test@gmail.com" :join-date (java.util.Date. 115 2 17)) ; => {:username "martin", :email "test@gmail.com", :join-date #inst "2015-03-16T16:00:00.000-00:00", :exp-date #inst "2015-04-15T16:00:00.000-00:00"}

;; Note, strings or numbers or even collections can be the key
(defn foo
  [& {k ["m" 7]}]                       ; ["m" 7] is treated as key
  (inc k))
(foo ["m" 7] 9)                         ; => 10

;; function literial, anonymous function sugar
(#(Math/pow %1 %2) 2 3)                 ; => 8.0
'#(Math/pow %1 %2) ; => (fn* [p1__7171# p2__7172#] (Math/pow p1__7171# p2__7172#))
;; need to use `do' explicitly when multi statements
(#(Do (println %1)
      (println %2)) "hello" "world")
;; first argument can just use %
'#(Math/pow % %2) ; => (fn* [p1__7207# p2__7208#] (Math/pow p1__7207# p2__7208#))
;; rest syntax
(#(apply + %&) 1 2 3 4)                 ; => 10
;; Function literals cannot be nested, so #(#()) will raises error

;;; Conditionals
;;-----------------------------------------------------------
;; conditionals determine logical truth to be anything other than `nil' and `false'
(if "hi" \t)                            ; => \t
(if 2 \t)                               ; => \t
(if nil \t \f)                          ; => \f
(if false \t \f)                        ; => \f
(if (not true) \t)                      ; => nil
;; when
(when true \t)                          ; => \t
(when false \t)                         ; => nil
;; cond
(#(cond (> %1 0) 1
        (= %1 0) 0
        (< %1 0) -1) 3)                 ; => 1
;; if-let, when-let
(if-let [c true] \t \f)                 ; => \t
(if-let [c false] \t \f)                ; => \f
(when-let [c true] \t \f)               ; => \f
;; true? and false? are only used for boolean
(true? true)                            ; => true
(true? 1)                               ; => false

;;; Looping
;;-----------------------------------------------------------
;; recur is used for tail call optimization, it do not consume stack space
(loop [x 6]
  (println "x is" x)
  (if (neg? x)
    x
    (recur (dec x))))                   ; => -1
(defn countdown
  [x]
  (if (zero? x)
    :zero!
    (do (println x)
        (recur (dec x)))))
(countdown 2)

(defn feb
  [n]
  (cond
    (zero? n) 0
    (= 1 n) 1
    true (loop [ppre 0N
                pre 1N
                num 2]
           (if (= num n)
             (+ ppre pre)
             (recur pre (+ ppre pre) (inc num))))))
(feb 10)                                ; => 55N

;;; var to get the reference itself, rather than the value
(def x 5)                               ; => #'clj.core/x
(var x)                                 ; => #'clj.core/x
#'x                                     ; => #'clj.core/x

;;; Java Interop: `.' and `new'
(java.util.ArrayList. 100)              ; => []
(new java.util.ArrayList 100)           ; => []
(Math/pow 2 10)                         ; => 1024.0
(. Math pow 2 10)                       ; => 1024.0
(.substring "hello" 1 3)                ; => "el"
(. "hello" substring 1 3)               ; => "el"
(Integer/MAX_VALUE)                     ; => 2147483647
(. Integer MAX_VALUE)                   ; => 2147483647

;;; eval
(eval x)
(defn embedded-repl
  "A naive Clojure REPL implementation. Enter `:quit`
  to exit."
  []
  (print (str (ns-name *ns*) ">>> "))
  (flush)
  (let [expr (read)
        value (eval expr)]
    (when (not= :quit value)
      (println value)
      (recur))))
;;(embedded-repl)

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




;;;; Thinking
;; 1. Pure Function, 函数不依赖外部的状态，不改变外部的状态(side effect)，同样的输入对应固定的输出。这样的函数严谨，可靠，可测。
;; 对于有状态依赖的函数，我们一般需要mock data来测试，但是你永远无法保证能cover所有的state。而pure函数没有这个问题。
;; 但是关键是，真实的场景中，交互永远是状态依赖的。我们无法避免状态，从这个角度来讲，我们要做的，就是尽量保证Pure Function，然后把状态依赖收集起来，集中处理
;; Pure Function的好处：easy to reason about(易于推导), 便于测试, 可缓存化, 可并行化

;; 2. Memoize可能导致cache不被GC回收，产生memory leak
