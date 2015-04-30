(ns clj.interop
  (:import (java.io
            File
            FileInputStream FileOutputStream)
           (javax.swing JList JFrame JScrollPane JButton)
           java.util.Vector))

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


;;; Proxy
(defn lru-cache
  [max-size]
  ;; proxy [superclass & interfaces] [args] functions
  (proxy [java.util.LinkedHashMap] [16 0.75 true]
    (removeEldestEntry [entry]
      (> (count this) max-size))))      ; => #'clj.interop/lru-cache

(def cache (doto (lru-cache 5)
             (.put :a :b)))             ; => #'clj.interop/cache
cache                                   ; => {:a :b}
(doseq [[k v] (partition 2 (range 500))]
  ;; visit :a to make it hot, so that will not be removed
  (get cache :a)
  (.put cache k v))                     ; => nil
cache                           ; => {492 493, 494 495, 496 497, :a :b, 498 499}


;;; Annotations, just use meta
#_(gen-class
   :name com.clojurebook.annotations.JUnitTest
   :methods [[^{org.junit.Test true} simpleTest [] void]
             [^{org.junit.Test {:timeout 2000}} timeoutTest [] void]
             [^{org.junit.Test {:expected NullPointerException}}
              badException [] void]])

;;; Call clojure from Java

;; RT.var("clojure.core", "require").invoke(
;;  Symbol.intern("com.clojurebook.protocol"));
;; IFn speakFn = RT.var("com.clojurebook.protocol", "speak").fn();


;;; defonce will not reevaluate
(defonce fn-names "name")
