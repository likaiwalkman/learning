;;; 2.4 Multiple Representations for Abstract Data
(ns sicp.complex)

(defn square [n] (* n n))
(defn error [& args]
  (throw (Exception. (clojure.string/join " " args))))

(declare real-part imag-part magnitude angle make-from-real-imag make-from-mag-ang)

(defn add-complex [z1 z2]
  (make-from-real-imag (+ (real-part z1) (real-part z2))
                       (+ (imag-part z1) (imag-part z2))))
(defn sub-complex [z1 z2]
  (make-from-real-imag (- (real-part z1) (real-part z2))
                       (- (imag-part z1) (imag-part z2))))
(defn mul-complex [z1 z2]
  (make-from-mag-ang (* (magnitude z1) (magnitude z2))
                     (+ (angle z1) (angle z2))))
(defn div-complex [z1 z2]
  (make-from-real-imag (/ (magnitude z1) (magnitude z2))
                       (- (angle z1) (angle z2))))


(defn attach-tag [type-tag contents]
  (cons type-tag contents))
(defn type-tag [datum]
  (if (seq? datum)
    (first datum)
    (error "Bad tagged datum -- TYPE-TAG" datum)))
(defn contents [datum]
  (if (seq? datum)
    (rest datum)
    (error "Bad tagged datum -- CONTENTS" datum)))

(defn rectangular? [z]
  (= (type-tag z) 'rectangular))
(defn polar? [z]
  (= (type-tag z) 'polar))

;; Rectangular form
(defn real-part-rectangular [z] (first z))
(defn imag-part-rectangular [z] (rest z))
(defn magnitude-rectangular [z]
  (Math/sqrt (+ (square (real-part-rectangular z))
                (square (imag-part-rectangular z)))))
(defn angle-rectangular [z]
  (Math/atan (/ (imag-part-rectangular z)
                (real-part-rectangular z))))
(defn make-from-real-imag-rectangular [x y]
  (attach-tag 'rectangular (cons x y)))
(defn make-from-mag-ang-rectangular [r a]
  (attach-tag 'rectangular
              (cons (* r (Math/cos a)) (* r (Math/sin a)))))
;; Polar form
(defn magnitude-polar [z] (first z))
(defn angle-polar [z] (rest z))
(defn real-part-polar [z]
  (* (magnitude-polar z) (Math/cos (angle-polar z))))
(defn imag-part-polar [z]
  (* (magnitude-polar z) (Math/sin (angle-polar z))))
(defn make-from-real-imag-polar [x y]
  (attach-tag 'polar
              (cons (Math/sqrt (+ (square x) (square y)))
                    (Math/atan (/ y x)))))
(defn make-from-mag-ang-polar [r a]
  (attach-tag 'polar (cons r a)))


(defn real-part [z]
  (cond (rectangular? z)
        (real-part-rectangular (contents z))

        (polar? z)
        (real-part-polar (contents z))

        :else
        (error "Unknown type -- REAL-PART" z)))

(defn imag-part [z]
  (cond (rectangular? z)
        (imag-part-rectangular (contents z))

        (polar? z)
        (imag-part-polar (contents z))

        :else
        (error "Unknown type -- IMAG-PART" z)))

(defn magnitude [z]
  (cond (rectangular? z)
        (magnitude-rectangular (contents z))

        (polar? z)
        (magnitude-polar (contents z))

        :else
        (error "Unknown type -- MAGNITUDE" z)))

(defn angle [z]
  (cond (rectangular? z)
        (angle-rectangular (contents z))

        (polar? z)
        (angle-polar (contents z))

        :else
        (error "Unknown type -- ANGLE" z)))

(defn make-from-real-imag [x y]
  (make-from-real-imag-rectangular x y))
(defn make-from-mag-ang [r a]
  (make-from-mag-ang-polar r a))
