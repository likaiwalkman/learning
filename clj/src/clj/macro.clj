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
