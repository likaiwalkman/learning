(ns sicp.exercise.ch1
  (:require [clojure.pprint :as pp]))

(defn exercise-1_1
  "Below is a sequence of expressions. What is the result printed by the interpreter in response to each expression? Assume that the sequence is to be evaluated in the order in which it is presented."
  []
  (->>
   '(10                                   ; => 10
     (+ 5 3 4)                            ; => 12
     (- 9 1)                              ; => 8
     (/ 6 2)                              ; => 3
     (+ (* 2 4) (- 4 6))                  ; => 6
     (def a 3)                            ; => #'sicp.exercise.ch1/a
     (def b (+ a 1))                      ; => #'sicp.exercise.ch1/b
     (+ a b (* a b))                      ; => 19
     (= a b)                              ; => false
     (if (and (> b a) (< b (* a b)))
       b a)                               ; => 4
     (cond (= a 4) 6
           (= b 4) (+ 6 7 a)
           :else 25)                      ; => 16
     (+ 2 (if (> b a) b a))               ; => 6
     (* (cond (> a b) a
              (< a b) b
              :else -1)
        (+ a 1))                          ; => 16
     )
   (map #(str % " => " (eval %) "\n"))
   (map print)
   ))

(defn exercise-1_2
  "Translate the following expression into prefix form"
  []
  '(/ (+ 5 4 (- 2 (- 3 (+ 6 (/ 4 3)))))
      (* 3 (- 6 2) (- 2 7))))
