(ns clj.multimethod)

(defmulti fill
  "Fill a xml/html node with the provided value."
  (fn [node value] (:tag node)))        ; => #'clj.multimethod/fill

(defmethod fill :div
  [node value]
  (assoc node :content [(str value)])) ; => #<MultiFn clojure.lang.MultiFn@5dcd9abe>
(defmethod fill :input
  [node value]
  (assoc-in node [:attr :value] (str value))) ; => #<MultiFn clojure.lang.MultiFn@5dcd9abe>

(fill {:tag :div} "hello")            ; => {:content ["hello"], :tag :div}
(fill {:tag :input} "hello")          ; => {:attr {:value "hello"}, :tag :input}
#_(fill {:span :input} "hello")         ; "No method in multimethod 'fill' for dispatch value: null"

;; provide default
(defmethod fill :default
  [node value]
  (assoc node :content [(str value)])) ; => #<MultiFn clojure.lang.MultiFn@5dcd9abe>
(fill {:span :input} "hello")          ; => {:content ["hello"], :span :input}
(fill {:span :input} "hello")          ; => {:content ["hello"], :span :input}

;; a better one
(defmulti fill
  "Fill a xml/html node (as per clojure.xml)
with the provided value."
  (fn [node value] (:tag node))
  :default nil)                         ; => nil
(defmethod fill nil
  [node value]
  (assoc node :content [(str value)])) ; => #<MultiFn clojure.lang.MultiFn@5dcd9abe>
(defmethod fill :input
  [node value]
  (assoc-in node [:attrs :value] (str value))) ; => #<MultiFn clojure.lang.MultiFn@5dcd9abe>
(defmethod fill :default
  [node value]
  (assoc-in node [:attrs :name] (str value))) ; => #<MultiFn clojure.lang.MultiFn@5dcd9abe>

;;; Hierarchies

;; unmap fill, because redefining a multimethod does not update dispatch function
(ns-unmap *ns* 'fill)                   ; => nil
(defn- fill-dispatch [node value]
  (if (= :input (:tag node))
    [(:tag node) (-> node :attrs :type)]
    (:tag node)))                       ; => #'clj.multimethod/fill-dispatch
(defmulti fill
  ;; using a function here, so that we can change it easily(without ns-unmap)
  #'fill-dispatch
  :default nil)                         ; => #'clj.multimethod/fill
(defmethod fill nil
  [node value]
  (assoc node :content [(str value)])) ; => #<MultiFn clojure.lang.MultiFn@762068d6>
(defmethod fill [:input nil]
  [node value]
  (assoc-in node [:attrs :value] (str value))) ; => #<MultiFn clojure.lang.MultiFn@762068d6>;
(defmethod fill [:input "hidden"]
  [node value]
  (assoc-in node [:attrs :value] (str value))) ; => #<MultiFn clojure.lang.MultiFn@762068d6>
(defmethod fill [:input "text"]
  [node value]
  (assoc-in node [:attrs :value] (str value))) ; => #<MultiFn clojure.lang.MultiFn@762068d6>
(defmethod fill [:input "radio"]
  [node value]
  (if (= value (-> node :attrs :value))
    (assoc-in node [:attrs :checked] "checked")
    (update-in node [:attrs] dissoc :checked))) ; => #<MultiFn clojure.lang.MultiFn@762068d6>
(defmethod fill [:input "checkbox"]
  [node value]
  (if (= value (-> node :attrs :value))
    (assoc-in node [:attrs :checked] "checked")
    (update-in node [:attrs] dissoc :checked))) ; => #<MultiFn clojure.lang.MultiFn@762068d6>
(defmethod fill :default
  [node value]
  (assoc-in node [:attrs :value] (str value))) ; => #<MultiFn clojure.lang.MultiFn@762068d6>

;; try
(fill {:tag :input
       :attrs {:value "first choice"
               :type "checkbox"}}
      "first choice") ; => {:attrs {:checked "checked", :value "first choice", :type "checkbox"}, :tag :input}
(fill *1 "off") ; => {:attrs {:value "first choice", :type "checkbox"}, :tag :input}

;; Hierarchies
(macroexpand '::test)                   ; => :clj.multimethod/test
(derive ::checkbox ::checkable)         ; => nil
(derive ::radio ::checkable)            ; => nil
(derive ::checkable ::input)            ; => nil
(derive ::text ::input)                 ; => nil

(isa? ::radio ::input)                  ; => true
(isa? ::radio ::text)                   ; => false
(isa? java.util.ArrayList Object)       ; => true
(isa? java.util.ArrayList java.util.List) ; => true
(isa? java.util.ArrayList java.util.Map)  ; => false

;; derive Map and Collection
(derive java.util.Map ::collection)        ; => nil
(derive java.util.Collection ::collection) ; => nil
(isa? java.util.ArrayList ::collection)    ; => true
(isa? java.util.HashMap ::collection)      ; => true

(isa? (make-hierarchy) java.util.ArrayList java.util.Collection) ; => true


;; derive default mutates the global hierarchy, but you can use a custom hierarchy
(ns-unmap *ns* 'fill)                   ; => nil
(def fill-hierarchy (-> (make-hierarchy)
                        (derive :input.radio ::checkable)
                        (derive :input.checkbox ::checkable)
                        (derive ::checkable :input)
                        (derive :input.text :input)
                        (derive :input.hidden :input))) ; => #'clj.multimethod/fill-hierarchy

(defn- fill-dispatch [node value]
  (if-let [type (and (= :input (:tag node))
                     (-> node :attrs :type))]
    (keyword (str "input." type))
    (:tag node)))                       ; => #'clj.multimethod/fill-dispatch
(defmulti fill
  #'fill-dispatch
  :default nil
  :hierarchy #'fill-hierarchy)          ; => nil
(defmethod fill nil [node value]
  (assoc node :content [(str value)])) ; => #<MultiFn clojure.lang.MultiFn@264ff350>
(defmethod fill :input [node value]
  (assoc-in node [:attrs :value] (str value))) ; => #<MultiFn clojure.lang.MultiFn@264ff350>
(defmethod fill ::checkable [node value]
  (if (= value (-> node :attrs :value))
    (assoc-in node [:attrs :checked] "checked")
    (update-in node [:attrs] dissoc :checked))) ; => #<MultiFn clojure.lang.MultiFn@264ff350>

;; unknown input types are processed by default(nil) fill
(fill {:tag :input
       :attrs {:type "date"}}
      "20110820") ; => {:content ["20110820"], :attrs {:type "date"}, :tag :input}

;; to solve this, let's dynamically update the hierarchy
(defmethod fill nil [node value]
  (if (= :input (:tag node))
    (do
      ;; derive dynamically
      (alter-var-root #'fill-hierarchy
                      derive (fill-dispatch node value) :input)
      (fill node value))
    (assoc node :content [(str value)]))) ; => #<MultiFn clojure.lang.MultiFn@264ff350>

(fill {:tag :input
       :attrs {:type "date"}}
      "20110820")   ; => {:attrs {:value "20110820", :type "date"}, :tag :input}

;; considering value of dispatch function
(ns-unmap *ns* 'fill)                               ; => nil
(def fill-hierarchy (-> (make-hierarchy)
                        (derive :input.radio ::checkable)
                        (derive :input.checkbox ::checkable))) ; => #'clj.multimethod/fill-hierarchy
(defn- fill-dispatch [node value]
  (if-let [type (and (= :input (:tag node))
                     (-> node :attrs :type))]
    [(keyword (str "input." type)) (class value)]
    [(:tag node) (class value)]))       ; => #'clj.multimethod/fill-dispatch
(defmulti fill
  "Fill a xml/html node (as per clojure.xml)
with the provided value."
  #'fill-dispatch
  :default nil
  :hierarchy #'fill-hierarchy)          ; => #'clj.multimethod/fill
(defmethod fill nil
  [node value]
  (if (= :input (:tag node))
    (do
      (alter-var-root #'fill-hierarchy
                      derive (first (fill-dispatch node value)) :input)
      (fill node value))
    (assoc node :content [(str value)]))) ; => #<MultiFn clojure.lang.MultiFn@76947fa1>
(defmethod fill
  [:input Object] [node value]
  (assoc-in node [:attrs :value] (str value))) ; => #<MultiFn clojure.lang.MultiFn@76947fa1>
(defmethod fill [::checkable clojure.lang.IPersistentSet]
  [node value]
  (if (contains? value (-> node :attrs :value))
    (assoc-in node [:attrs :checked] "checked")
    (update-in node [:attrs] dissoc :checked))) ; => #<MultiFn clojure.lang.MultiFn@76947fa1>

(fill {:tag :input
       :attrs {:value "yes"
               :type "checkbox"}}
      #{"yes" "y"}) ; => {:attrs {:checked "checked", :value "yes", :type "checkbox"}, :tag :input}
(fill *1 #{"no" "n"}) ; => {:attrs {:value "yes", :type "checkbox"}, :tag :input}
(fill {:tag :input
       :attrs {:type "text"}}
      "some text") ; => {:attrs {:value "some text", :type "text"}, :tag :input}
(fill {:tag :h1} "Big Title!")          ; => {:content ["Big Title!"], :tag :h1}


;;; Prefer
(defmulti run "Executes the computation." class) ; => #'clj.multimethod/run
(defmethod run Runnable
  [x]
  (.run x))                        ; => #<MultiFn clojure.lang.MultiFn@7765ca4c>
(defmethod run java.util.concurrent.Callable
  [x]
  (.call x))                       ; => #<MultiFn clojure.lang.MultiFn@7765ca4c>
;; below will throw Exception
;; because Clojure functions implements both Runnable and Callable
#_(run #(println "hello!"))        ; Exception

(prefer-method run java.util.concurrent.Callable Runnable) ; => #<MultiFn clojure.lang.MultiFn@7765ca4c>
(run #(println "hello!"))                                  ; => nil


;; type vs class
(class {})                              ; => clojure.lang.PersistentArrayMap
(type {})                               ; => clojure.lang.PersistentArrayMap
(class ^{:type :a-tag} {})              ; => clojure.lang.PersistentArrayMap
(type ^{:type :a-tag} {})               ; => :a-tag


(ns-unmap *ns* 'run)                            ; => nil
(defmulti run "Executes the computation." type) ; => #'clj.multimethod/run
(defmethod run Runnable
  [x]
  (.run x))                        ; => #<MultiFn clojure.lang.MultiFn@21f4974e>
(defmethod run java.util.concurrent.Callable
  [x]
  (.call x))                       ; => #<MultiFn clojure.lang.MultiFn@21f4974e>
(prefer-method run java.util.concurrent.Callable Runnable) ; => #<MultiFn clojure.lang.MultiFn@21f4974e>
(defmethod run :runnable-map
  [m]
  (run (:run m)))                  ; => #<MultiFn clojure.lang.MultiFn@21f4974e>
(run #(println "hello!"))          ; => nil
(run (reify Runnable
       (run [this] (println "hello!")))) ; => nil
(run ^{:type :runnable-map}
  {:run #(println "hello!") :other :data}) ; => nil
