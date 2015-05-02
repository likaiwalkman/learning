(ns clj.test
  (:use (clojure [test]
                 [string :as clojure.string])))
(defprotocol Bark (bark [this]))
(defrecord Chihuahua [weight price]
  Bark
  (bark [this] "Yip!"))
(defrecord PetStore [dog])
(defn configured-petstore []
  (-> "petstore-config.clj" slurp
      read-string map->PetStore))

(def ^:private dummy-petstore (PetStore. (Chihuahua. 12 "$84.50")))
(deftest test-configured-petstore
  (is (= (configured-petstore) dummy-petstore)))

;; Fixture
(defn petstore-config-fixture
  [f]
  (let [file (java.io.File. "petstore-config.clj")]
    (try
      (spit file (with-out-str (pr dummy-petstore)))
      (f)
      (finally
        (.delete file)))))              ; => #'clj.test/petstore-config-fixture
(use-fixtures :once petstore-config-fixture) ; => {:clojure.test/once-fixtures (#<test$petstore_config_fixture clj.test$petstore_config_fixture@52f5b75d>)}


(deftest test-addition
  (are [x y z] (= x (+ y z))
       10 7 3
       20 10 10
       100 89 11))                      ; => #'clj.test/test-addition
(defmacro are* [f & body]
  `(are [x# y#]
     (~'= y# (~f x#))
     ~@body))                           ; => #'clj.test/are*
(are* str
      10 "10"
      :foo ":foo")                      ; => true

;;; HTML generator
(declare html attrs)                    ; => #'clj.test/attrs
(deftest test-html
  (are* html
        [:html]
        "<html></html>"

        [:a [:b]]
        "<a><b></b></a>"

        [:a {:href "/"} "Home"]
        "<a href=\"/\">Home</a>"

        [:div "foo" [:span "bar"] "baz"]
        "<div>foo<span>bar</span>baz</div>")) ; => #'clj.test/test-html
(deftest test-attrs
  (are* (comp clojure.string/trim attrs)
        nil ""

        {:foo "bar"}
        "foo=\"bar\""

        (sorted-map :a "b" :c "d")
        "a=\"b\" c=\"d\""))             ; => #'clj.test/test-attrs

(defn attrs
  [attr-map]
  (->> attr-map
       (mapcat (fn [[k v]] [(name k) "=\"" v "\" "]))
       (apply str)
       clojure.string/trim))                    ; => #'clj.test/attrs

(defn html
  [x]
  (if-not (sequential? x)
    (str x)
    (let [[tag & body] x
          [attr-map body] (if (map? (first body))
                            [(first body) (rest body)]
                            [nil body])]
      (str "<" (name tag)
           (if-not (blank? (attrs attr-map))
             (str " " (attrs attr-map))
             "")
           ">"
           (apply str (map html body))
           "</" (name tag) ">"))))

(run-tests)

(html [:html
       [:head [:title "Propaganda"]]
       [:body [:p "Visit us at "
               [:a {:href "http://clojurebook.com"}
                "our website"] "."]]])

;; Assertions
(defn attrs
  [attr-map]
  (assert (or (map? attr-map)
              (nil? attr-map))
          "attr-map must be nil, or a map")
  (->> attr-map
       (mapcat (fn [[k v]] [\space (name k) "=\"" v "\""]))
       (apply str)))                    ; => #'clj.test/attrs
;; enable assert
(set! *assert* true)
#_(attrs "hi")                            ; Assert failed: attr-map must be nil, or a map (or (map? attr-map) (nil? attr-map))


;; Preconditions and Postconditions
(defn pre-post
  [x]
  {
   ;; assert for args
   :pre [(pos? x)]
   ;; assert for return value
   :post [(neg? %)]
   }
  (- 1 x))                              ; => #'clj.test/pre-post
#_(pre-post -1)                           ; Assert failed: (pos? x)
#_(pre-post 1)                            ; Assert failed: (neg? %)
(pre-post 2)                              ; => -1
