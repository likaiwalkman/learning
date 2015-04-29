(ns clj.tool)

;;; Namespaces
*ns*                                    ; => #<Namespace clj.tool>
(defn a [] 42)                          ; => #'clj.tool/a
;; in-ns
(do (in-ns 'clj.main)
    (def ^:const planck 6.62606957e-34)
    *ns*)                               ; => #<Namespace clj.main>
#'clj.main/planck                       ; => #'clj.main/planck

;; refer
(refer 'clj.main)                       ; => nil
#'planck                                ; => #'clj.main/planck

;; require and use
