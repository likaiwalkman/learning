(ns clj.core)

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))
(defn average
  [x]
  (/ (apply + x) (count x)))
(average [1 2 3])
