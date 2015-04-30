(ns clj.interop
  (:import (java.io
            File
            FileInputStream FileOutputStream)))

;; with-open, auto release resources
(defn copy-files
  [from to]
  (with-open [in (FileInputStream. from)
              out (FileOutputStream. to)]
    (loop [buf (make-array Byte/TYPE 1024)]
      (let [len (.read in buf)]
        (when (pos? len)
          (.write out buf 0 len)
          (recur buf))))))              ; => #'clj.main/copy-files
#_(copy-files (File. "src/clj/main.clj")
              (File. "src/clj/main_copy.clj")) ; => nil

;; Type Hinting for Performance
;; ^String = ^{:tag String}
;; it's used by compiler to avoid `reflective interop' calls
(set! *warn-on-reflection* true)        ;= true
(defn capitalize
  [s]
  (-> s
      (.charAt 0)
      Character/toUpperCase
      (str (.substring s 1)))) ; =>
(defn fast-capitalize
  [^String s]
  (-> s
      (.charAt 0)
      Character/toUpperCase
      (str (.substring s 1))))

(time (doseq
          [s (repeat 100000 "foo")]
        (capitalize s)))
(time (doseq
          [s (repeat 100000 "foo")]
        (fast-capitalize s)))

;;; Array
;; Create an array from a collection
(into-array ["a" "b" "c"])        ; => #<String[] [Ljava.lang.String;@975d601>
;; Create an empty array
(make-array Integer 10 100)   ; => #<Integer[][] [[Ljava.lang.Integer;@113b23d5>
;; Create an empty array of primitive longs
(long-array 10)                         ; => #<long[] [J@47f9464b>
(make-array Long/TYPE 10)               ; => #<long[] [J@7de672fa>
;; Access an array value
#_(aget some-array 0) ; =>
;; Set an array valuea
#_(aset some-array 4 "foo")
#_(aset ^ints int-array 4 5)
