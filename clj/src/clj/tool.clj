(ns clj.tool)

;;; Namespaces
*ns*                                    ; => #<Namespace clj.tool>
;; in-ns
(do (in-ns 'clj.main)
    (def ^:const planck 6.62606957e-34)) ; => #'clj.main/planck
#'clj.main/planck                       ; => #'clj.main/planck

;; refer
(refer 'clj.main)                       ; => nil
#'planck                                ; => #'clj.main/planck

;; require and use
;; use = require + refer
;; better use ns, and use :use with alias
#_(ns com.examples.ns
    (:refer-clojure :exclude [next replace remove])
    (:use (clojure [string :as string :only (split)]
                   [set :as set]))
    (:import java.util.Date
             java.text.SimpleDateFormat
             (java.util.concurrent Executors
                                   LinkedBlockingQueue)))

;; classpath
;; java -cp '.:src:clojure.jar:lib/*' clojure.main


;;; AOT, ahead-of-time compilation


;; Clojure strings are Java Strings.
;; • Clojure nil is Java’s null.
;; • Clojure numbers are Java numbers.3
;; • Clojure regular expressions are instances of java.util.regex.Pattern.
;; • Clojure data structures implement the read-only portions of the appropriate java.io.* collection interfaces; so, Clojure maps implement java.util.Map, vec- tors and sequences and lists implement java.util.List, and sets implement java.util.Set.
;; • Clojurefunctionsimplementjava.lang.Runnableandjava.util.concurrent.Call able, making them trivial to integrate into existing libraries and frameworks that expect these core Java interfaces.
;; • Behinditssyntaxandabstractions,Clojurefunctioninvocationsaremethodin- vocations in Java; thus, Clojure functions and function calls carry no special run- time overhead.
;; • Clojureisneverinterpreted;rather,itisalwayscompileddowntoefficientJVM bytecode prior to being run, even in interactive settings like the REPL.
;; • CallingJavaAPIsfromClojureissemanticallyandmechanicallythesameopera- tion as calling such APIs from Java.
;; — Clojure functions compile down to classes.
;; — Clojure’s defrecord and deftype forms compile down to Java classes contain- ing regular Java fields.
;; — Protocols defined by defprotocols generate corresponding Java interfaces.
