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
                                        ; unicode
\u00ff                                  ; => \ÿ
                                        ; octal
\o41                                    ; => \!
                                        ; special characters
\tab                                    ; => \tab
\space                                  ; => \space

;; keywords
                                        ; map
(def pizza {:name "test's"
            :location "sh"
            ::location "martin"})
                                        ; get value by keyword
(:location pizza)                       ; => "sh"
                                        ; with namespace
(:clj.core/location pizza)              ; => "martin"

;; symbols, must start with non-numeric, can contain *, +, -, !, _, ?
                                        ; avarage is a symbol
(avarage [1 2])

;; numbers
42                                      ; => 42
(class 42)                              ; => java.lang.Long
                                        ; 16
0xff                                    ; => 255
                                        ; 2
2r111                                   ; => 7
                                        ; 8
040                                     ; => 32
                                        ; any
5r11                                    ; => 6
                                        ; double
3.14                                    ; => 3.14
                                        ; BigInt
42N                                     ; => 42N
                                        ; BigDecimal
0.01M                                   ; => 0.01M
                                        ; Ratio
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
                                        ; list
'(a b :name 3.5)                        ; => (a b :name 3.5)
                                        ; vector
['a 'b :name 3.5]                       ; => [a b :name 3.5]
                                        ; map
{:name "martin" :age "25"}              ; => {:age "25", :name "martin"}
                                        ; set
#{1 2 :name}                            ; => #{1 :name 2}

;;; Namespaces
                                        ; define vars(in current namespace), vars are not variables
(def x 1)                               ; => #'clj.core/x
                                        ; current namespace
*ns*                                    ; => #<Namespace clj.core>
java.util.List                          ; => java.util.List
                                        ; all the vars in clojure.core can be accessed without namespace-qualifying
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
p                                       ; "foo"

;; let, local bindings
(let
    [a (inc (rand-int 6))
     b (inc (rand-int 6))]
  (println (format "You rolled a %s and b %s" a b))
  (+ a b))
; _ is used for ignore name binding
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
; bind rest of the vector, return a sequence
(let [[x & r] v]
  r)                                    ; => (a 5 [4 6])
; use :as to bind original vector
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
; map destructuring with vector by index
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
; rest
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
  ; use vector because the rest args is a list
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
; need to use `do' explicitly when multi statements
(#(Do (println %1)
      (println %2)) "hello" "world")
; first argument can just use %
'#(Math/pow % %2) ; => (fn* [p1__7207# p2__7208#] (Math/pow p1__7207# p2__7208#))
; rest syntax
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
; when
(when true \t)                          ; => \t
(when false \t)                         ; => nil
; cond
(#(cond (> %1 0) 1
        (= %1 0) 0
        (< %1 0) -1) 3)                 ; => 1
; if-let, when-let
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
  ([n] (feb n 0))
  ([n sum]
   (cond
     (zero? x) sum
     (= 1 x) (+ sum 1)
     true (do ()
              (recur (dec x))))))
