(ns clj.design-pattern)

;;; Listener, Observer
;; Clojure watches

;;; Abstract Factory, Strategy, Command
;; Clojure functions

;;; Iterator
;; Clojure sequences. And by the way, map is much better than Iterator

;;; Adapter, Wrapper, Delegate
;; Clojure protocols

;;; Memento
;; Immutable collections and records

;;; Template Method
;; High-order function


;;; Dependency Injection
(defprotocol Bark
  (bark [this]))                        ; => Bark
(defrecord Chihuahua []
  Bark
  (bark [this] "Yip!"))                 ; => clj.design_pattern.Chihuahua
(defrecord Mastiff []
  Bark
  (bark [this] "Woof!"))                ; => clj.design_pattern.Mastiff

(defrecord PetStore [dog])              ; => clj.design_pattern.PetStore
(defn main
  [dog]
  (let [store (PetStore. dog)]
    (bark (:dog store))))               ; => #'clj.design-pattern/main

(main (Chihuahua.))                     ; => "Yip!"
(main (Mastiff.))                       ; => "Woof!"

(-> "src/clj/main.clj"
    slurp
    read-string
    println)


;; Chain of Responsibility
(defn foo [data]
  (println "FOO passes")
  true)                                 ; => #'clj.design-pattern/foo
(defn bar [data]
  (println "BAR" data "and let's stop here")
  false)                                ; => #'clj.design-pattern/bar
(defn baz [data]
  (println "BAZ?")
  true)                                 ; => #'clj.design-pattern/baz
(defn wrap [f1 f2]
  (fn [data]
    (when (f1 data)
      (f2 data))))                      ; => #'clj.design-pattern/wrap
(def chain (reduce wrap [foo bar baz])) ; => #'clj.design-pattern/chain
(chain "test-data")                     ; => nil


;; AOP
(defn time-it [f & args]
  (let [start (System/currentTimeMillis)]
    (try
      (apply f args)
      (finally
        (println "Run time: "
                 (-
                  (System/currentTimeMillis)
                  start)
                 "ms")))))              ; => #'clj.design-pattern/time-it

(require 'robert.hooke)
(defn foo [x y]
  (Thread/sleep (rand-int 1000))
  (+ x y))
(robert.hooke/add-hook #'foo time-it)
(foo 1 2)

;; temporarily suspend hook
(robert.hooke/with-hooks-disabled foo (foo 1 2)) ; => 3
(robert.hooke/remove-hook #'foo time-it)         ; => #<design_pattern$foo clj.design_pattern$foo@55d7a774>
(foo 1 2)                                        ; => 3
