(ns clj.practice)

;; Numeric
(+ 0.1 0.1 0.1)                         ; => 0.30000000000000004
(+ 1/10 1/10 1/10)                      ; => 3/10

(defn with-type
  [r]
  (list r (class r)))              ; => #'clj.practice/with-type
(with-type (+ 1 1))                ; => (2 java.lang.Long)
(with-type (+ 1 1.5))              ; => (2.5 java.lang.Double)
(with-type (+ 1 1N))               ; => (2N clojure.lang.BigInt)
(with-type (+ 1.1M 1N))            ; => (2.1M java.math.BigDecimal)

;; Math
(def max-long Long/MAX_VALUE)           ; => #'clj.practice/max-long
max-long                                ; => 9223372036854775807
#_(inc max-long)                        ; Exception
(inc (bigint max-long))                 ; => 9223372036854775808N
;; inc' supports arbitrary precision
(inc' max-long)                         ; => 9223372036854775808N
(inc' 1)                                ; => 2

;; unchecked-*
(unchecked-dec Long/MIN_VALUE)          ; => 9223372036854775807

;; precision
#_(/ 22M 7)                             ; Exception, "Non-terminating decimal expansion; no exact representable decimal result"
(with-precision 10 (/ 22M 7))           ; => 3.142857143M
(with-precision 10 :rounding FLOOR
  (/ 22M 7))                            ; => 3.142857142M



;;; Equality and Equivalence
;; identical? just `==' in java
(identical? "foot" (str "fo" "ot"))     ; => false
(= "foot" (str "fo" "ot"))              ; => true
;; numbers are objects, so not identical
(identical? 2600 2600)                  ; => false
;; below is true, because Java caches [-128, 127]
(identical? 127 127)                    ; => true
(identical? 128 128)                    ; => false

;; `=' is equals in Java
(= {:a 1 :b ["hi"]}
   (into (sorted-map) [[:b ["hi"]] [:a 1]])
   (doto (java.util.HashMap.)
     (.put :a 1)
     (.put :b ["hi"])))                 ; => true
;; different type of numbers not equal
(= 1 1.0)                               ; => false
(= 1N 1M)                               ; => false
(= 1.25 5/4)                            ; => false

;; Numeric Equivalence
(== 0.125 0.125M 1/8)                   ; => true
(== 4 4N 4.0 4.0M)                      ; => true


;;; Arrays
(Class/forName "[[Z")                   ; => [[Z
(.getComponentType *1)                  ; => [Z
(.getComponentType *1)                  ; => boolean
;; • Z — boolean
;; • B — byte
;; • C — char
;; • J — long
;; • I — int
;; • S — short
;; • D — double
;; • F — float

;; Array-specific type hints
;; ^objects
;; ^booleans
;; ^bytes
;; ^chars
;; ^longs
;; ^ints
;; ^shorts
;; ^doubles
;; ^floats
