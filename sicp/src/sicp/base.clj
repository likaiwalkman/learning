(ns sicp.base)

(defn square [n] (* n n))
(defn cube [n] (* n n n))
(defn average [a b] (/ (+ a b) 2))

(defn error [& args]
  (throw (Exception. (clojure.string/join " " args))))
