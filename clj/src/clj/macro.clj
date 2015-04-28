(ns clj.macro)
(require '(clojure [string :as str]
                   [walk :as walk]))    ; => nil

(defmacro foreach [[sym coll] & body]
  `(loop [coll# ~coll]
     (when-let [[~sym & xs#] (seq coll#)]
       ~@body
       (recur xs#))))                   ; => #'clj.macro/foreach

(macroexpand-1 '(foreach [x [1 2 3]]
                         (println x))) ; => (clojure.core/loop [coll__9929__auto__ [1 2 3]] (clojure.core/when-let [[x & xs__9930__auto__] (clojure.core/seq coll__9929__auto__)] (println x) (recur xs__9930__auto__)))

(defmacro reverse-it
  [form]
  (walk/postwalk #(if (symbol? %)
                    (symbol (str/reverse (name %)))
                    %)
                 form))                 ; => #'clj.macro/reverse-it
(macroexpand-1 '(reverse-it
                 (qesod [gra (egnar 5)]
                        (nltnirp (cni gra))))) ; => (doseq [arg (range 5)] (println (inc arg)))

;; macroexpand-1 only expand macro once
;; macroexpand will expand until the `top level form' is no longer a macro
;; clojure.walk.macroexpand-all can expand all of the macros
(macroexpand-1 '(reverse-it (dnoc
                             a b
                             c d)))     ; => (cond a b c d)
(macroexpand '(reverse-it (dnoc
                           a b
                           c d)))       ; => (if a b (clojure.core/cond c d))
(walk/macroexpand-all '(reverse-it (dnoc
                                    a b
                                    c d))) ; => (if a b (if c d nil))

;;; Syntax
;; syntax-quote(`), unquote(~), splicing-unquote(~@)
(def foo '(123))                        ; => #'clj.macro/foo
[foo (quote foo) 'foo `(foo) `(~foo) `(~@foo)] ; => [(123) foo foo (clj.macro/foo) ((123)) (123)]

;;; Note macros operate at compile time
(defn fn-hello [x]
  (str "Hello, " x "!"))                ; => #'clj.macro/fn-hello
(defmacro macro-hello [x]
  `(str "Hello, " ~x "!"))              ; => #'clj.macro/macro-hello
(fn-hello "Martin")                     ; => "Hello, Martin!"
(macro-hello "Martin")                  ; => "Hello, Martin!"

(map fn-hello ["Martin" "Daisy"])       ; => ("Hello, Martin!" "Hello, Daisy!")
#_(map macro-hello ["Martin" "Daisy"])  ; Will throw RuntimeException
;; below will works, because fn will be compiled
(map #(macro-hello %) ["Martin" "Daisy"]) ; => ("Hello, Martin!" "Hello, Daisy!")

;;; Hygiene
(gensym)                                ; => G__7169
(gensym)                                ; => G__7172
(defmacro hygienic
  [& body]
  (let [sym (gensym)]
    `(let [~sym :macro-value]
       ~@body)))                        ; => #'clj.macro/hygienic
(let [x :important-value]
  (hygienic (println "x:" x)))          ; => nil

;; using # ended
(defmacro hygienic
  [& body]
  `(let [x# :macro-value]
     ~@body))                           ; => #'clj.macro/hygienic
(macroexpand-1 '(hygienic
                 (println "x:" x))) ; => (clojure.core/let [x__7183__auto__ :macro-value] (println "x:" x))
;; inside a syntax-quoted form, `x#' is the same symbol
`(x# x#)                          ; => (x__7207__auto__ x__7207__auto__)
;; different syntax-quoted form will different
[`x# `x#]                               ; => [x__7227__auto__ x__7228__auto__]
(defmacro our-doto [expr & forms]
  ;; explicit bind gensym, because there're 2 syntax-quoted forms
  (let [obj (gensym "obj")]
    `(let [~obj ~expr]
       ~@(map (fn [[f & args]]
                `(~f ~obj ~@args)) forms)
       ~obj)))                          ; => #'clj.macro/our-doto
(our-doto "It works"
          (println "I can't believe it")
          (println "I still can't believe it")) ; => "It works"

;;; Common Macro Idioms and Patterns
;; Require that new local bindings be specified in a vector.
'(let [x (range 10)]
   x)
;; Don't be clever when defining vars
;; Macros that define a var should have a name that starts with def
;; Accept the name of the var as the first argument
;; Define `one var per macro' form

;;; Implicit arguments: &env and &form
(defmacro spy-env []
  (let [ks (keys &env)]
    `(prn (zipmap '~ks [~@ks]))))       ; => #'clj.macro/spy-env
(let [x 1 y 2]
  (spy-env)
  (+ x y))                              ; => 3

;; evaluate expression at compile time if it does not use locals
(defmacro simplify
  [expr]
  (let [locals (set (keys &env))]
    (if (some locals (flatten expr))
      expr
      (do
        (println "Precomputing: " expr)
        (list `quote (eval expr))))))   ; => #'clj.macro/simplify
(defn f
  [a b c]
  (+ a b c (simplify (apply + (range 5e7))))) ; => #'clj.macro/f
(time (f 1 2 3))                              ; "Elapsed time: 0.043 msecs"
(defn f'
  [a b c]
  (simplify (apply + a b c (range 5e7)))) ; => #'clj.macro/f'
(time (f' 1 2 3))                         ; "Elapsed time: 2957.543 msecs"

;; reaching into macro's var, grabbing its implementing function and using it directly, useful for testing
(@#'simplify nil {} '(inc 1))             ; => (quote 2)
(@#'simplify nil {'x nil} '(inc x))       ; => (inc x)

;; &form
(defmacro ontology
  [& triples]
  (println &form)
  (println (meta &form))
  (every? #(or (== 3 (count %))
               (throw (IllegalArgumentException.
                       (format "`%s' provided to `%s' on line %s has < 3 elements"
                               %
                               (first &form)
                               (-> &form meta :line)))))
          triples)
  ;; build and emit pre-processed ontology here...
  )                                     ; => #'clj.macro/ontology

;; Most macros discard the metadata attached to the forms within their context, including type hints
(set! *warn-on-reflection* true)        ; => true
(defn first-char-of-either
  [a b]
  (.substring ^String (or a b) 0 1))    ; => #'clj.macro/first-char-of-either
(binding [*print-meta* true] (prn '^String (or a b))) ; ^{:tag String, :line 1, :column 36} (or a b)

;; when macroexpand, metadata is gone
(binding [*print-meta* true]
  (prn (macroexpand '^String (or a b)))) ; (let* [or__4219__auto__ a] (if or__4219__auto__ or__4219__auto__ (clojure.core/or b)))

;; preserve-metadata
(defn preserve-metadata
  "Ensures that the body containing `expr' will carry the metadata from `&form'."
  [&form expr]
  (let [res (with-meta (gensym "res") (meta &form))]
    `(let [~res ~expr]
       ~res)))                          ; => #'clj.macro/preserve-metadata
(defmacro OR
  "Same as `clojure.core/or', but preserves user-supplied metadata"
  ([] nil)
  ([x] (preserve-metadata &form x))
  ([x & next]
   (preserve-metadata &form `(let [or# ~x]
                               (if or# or# (or ~@next)))))) ; => #'clj.macro/OR
(binding [*print-meta* true]
  (prn (macroexpand '^String (OR a b)))) ; => nil

;; Testing Contextual Macros
(defmacro if-all-let
  [bindings then else]
  (reduce (fn [subform binding]
            `(if-let [~@binding] ~subform ~else))
          then (reverse (partition 2 bindings)))) ; => #'clj.macro/if-all-let
(defn macroexpand1-env
  [env form]
  (if-all-let [[x & xs] (and (seq? form) (seq form))
               v (and (symbol? x) (resolve x))
               ;; check if it is macro
               _ (-> v meta :macro)]
              ;; macro accepts &form, &env as first/second parameter
              (apply @v form env xs)
              form))                    ; => #'clj.macro/macroexpand1-env
(macroexpand1-env '{} '(simplify (range 10))) ; => (quote (0 1 2 3 4 5 6 7 8 9))
(macroexpand1-env '{range nil} '(simplify (range 10))) ; => (range 10)

;; -> and ->>
(macroexpand '(-> foo bar baz))         ; => (baz (bar foo))
(list* 1 2 '(3))                        ; => (1 2 3)
(defn ensure-seq
  [x]
  (if (list? x)
    x
    (list x)))                          ; => #'clj.macro/ensure-seq
(defn insert-second
  "Insert x as the second item in seq y."
  [x ys]
  (let [ys (ensure-seq ys)]
    (list* (first ys) x (rest ys))))    ; => #'clj.macro/insert-second
(insert-second 'a '(b c))               ; => (b a c)
