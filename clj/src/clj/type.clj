(ns clj.type)

;;; Protocol
;; protocol will compiled to Java interface/class, so it don't support dynamic parameters
(defprotocol Matrix
  "Protocol for working with 2d data structures"
  (lookup [matrix i j])
  (update [matrix i j value])
  (rows [matrix])
  (cols [matrix])
  (dims [matrix]))                      ; => Matrix

;; extending to existing types
(class [])                              ; => clojure.lang.PersistentVector
(extend-protocol Matrix
  clojure.lang.IPersistentVector
  (lookup [vov i j]
    (get-in vov [i j]))
  (update [vov i j value]
    (assoc-in vov [i j] value))
  (rows [vov]
    (seq vov))
  (cols [vov]
    (apply map vector vov)))            ; => nil

(update [[nil nil][nil nil][nil nil]] 1 0 'x) ; => [[nil nil] [x nil] [nil nil]]

;; can be extended to nil
(extend-protocol Matrix
  nil
  (lookup [x i j])
  (update [x i j value])
  (rows [x] [])
  (cols [x] [])
  (dims [x] [0 0]))                     ; => nil
(lookup nil 3 3)                        ; => nil
(dims nil)                              ; => [0 0]


(defn vov
  "Create a vector of h w-item vectors."
  [h w]
  (vec (repeat h (vec (repeat w nil))))) ; => #'clj.type/vov
(def matrix (vov 3 4))                   ; => #'clj.type/matrix
matrix              ; => [[nil nil nil nil] [nil nil nil nil] [nil nil nil nil]]
(update matrix 1 2 :x) ; => [[nil nil nil nil] [nil nil :x nil] [nil nil nil nil]]
(lookup *1 1 2)        ; => :x
(rows (update matrix 1 2 :x)) ; => ([nil nil nil nil] [nil nil :x nil] [nil nil nil nil])
(cols (update matrix 1 2 :x)) ; => ([nil nil nil] [nil nil nil] [nil :x nil] [nil nil nil])

;; extend protocol to Java type
(Class/forName "[[D")                   ; => [[D
(extend-protocol Matrix
  (Class/forName "[[D")

  (lookup [matrix i j]
    (aget matrix i j))

  (update [matrix i j value]
    (let [clone (aclone matrix)]
      (aset clone i
            (doto (aclone (aget clone i))
              (aset j value)))
      clone))

  (rows [matrix]
    (map vec matrix))

  (cols [matrix]
    (apply map vector matrix))

  (dims [matrix]
    (let [rs (count matrix)]
      (if (zero? rs)
        [0 0]
        [rs (count (aget matrix 0))])))) ; => nil

(def matrix (make-array Double/TYPE 2 3)) ; => #'clj.type/matrix
matrix                                    ; => #<double[][] [[D@46881f54>
(rows matrix)                             ; => ([0.0 0.0 0.0] [0.0 0.0 0.0])
(rows (update matrix 1 1 3.4))            ; => ([0.0 0.0 0.0] [0.0 3.4 0.0])
(lookup (update matrix 1 1 3.4) 1 1)      ; => 3.4
(cols (update matrix 1 1 3.4))            ; => ([0.0 0.0] [0.0 3.4] [0.0 0.0])
(dims matrix)                             ; => [2 3]


;;; Define your own types
;; `defrecord' or `deftype' will created a new Java class Point(with public final properties), when use in other namespaces, we have to use `import'
(defrecord Point [x y])                 ; => clj.type.Point
(.x (Point. 3 2))                       ; => 3
(class (Point. 3 3))                    ; => clj.type.Point

;; using type hinting
(defrecord NamedPoint [^String name ^long x ^long y]) ; => clj.type.NamedPoint
;; it's not mandatory
(class (.name (NamedPoint. 1 2 3)))
;; get basis
(NamedPoint/getBasis)                   ; => [name x y]
(map meta (NamedPoint/getBasis))    ; => ({:tag String} {:tag long} {:tag long})

;;; Records
;; types defined by `defrecord' are a specialization of those defined by deftype

;; Value semantics. records are *immutable*, and two records whose fields are equal, they are equal
(defrecord Point [x y])                 ; => clj.type.Point
(= (Point. 3 4) (Point. 3 4))           ; => true
(= 3 3N)                                ; => true
(= (Point. 3N 4N) (Point. 3 4))         ; => true

;; Associative collections. records can be used just like maps, and they implements java.util.Map interface
(:x (Point. 3 4))                       ; => 3
(:z (Point. 3 4) 0)                     ; => 0
(map :x [(Point. 3 4)
         (Point. 5 6)
         (Point. 7 8)])                 ; => (3 5 7)
(assoc (Point. 3 4) :z 5)               ; => #clj.type.Point{:x 3, :y 4, :z 5}
(let [p (assoc (Point. 3 4) :z 5)]
  (dissoc p :x))                        ; => {:y 4, :z 5}
(let [p (assoc (Point. 3 4) :z 5)]
  (dissoc p :z))                        ; => #clj.type.Point{:x 3, :y 4}
;; auxiliary slots are not become new fields on the record's underlying Java class
(:z (assoc (Point. 3 4) :z 5))          ; => 5
#_(.z (assoc (Point. 3 4) :z 5))        ; Exception

;; Metadata support
(-> (Point. 3 4)
    (with-meta {:foo :bar})
    meta)                               ; => {:foo :bar}

;; Readable representation
(pr-str (assoc (Point. 3 4) :z [:a :b])) ; => "#clj.type.Point{:x 3, :y 4, :z [:a :b]}"
(= (read-string "#clj.type.Point{:x 3, :y 4, :z [:a :b]}")
   (assoc (Point. 3 4) :z [:a :b]))     ; => true

;; Auxiliary constructor. Can give 2 more arguments: a map for meta, a map for additional slots
(Point. 3 4 {:foo :bar} {:z 5})         ; => #clj.type.Point{:x 3, :y 4, :z 5}
(meta *1)                               ; => {:foo :bar}

;; Constructors and factory functions
;; `deftype' and `defrecord' implicitly create one factory function of the form "->Type"
;; `defrecord' also implicitly create a factory function of the form "map->Type"
(->Point 3 4)                           ; => #clj.type.Point{:x 3, :y 4}
(map->Point {:x 3 :z 5})                ; => #clj.type.Point{:x 3, :y nil, :z 5}

(apply ->Point [5 6])                   ; => #clj.type.Point{:x 5, :y 6}
(map (partial apply ->Point)
     [[5 6] [7 8] [9 10]]) ; => (#clj.type.Point{:x 5, :y 6} #clj.type.Point{:x 7, :y 8} #clj.type.Point{:x 9, :y 10})
(map map->Point [{:x 1} {:y 2 :z 3}]) ; => (#clj.type.Point{:x 1, :y nil} #clj.type.Point{:x nil, :y 2, :z 3})

;; `defrecord' also provides a static method create
(Point/create {:x 3, :y 4, :z 5})       ; => #clj.type.Point{:x 3, :y 4, :z 5}

(defn log-point
  [x]
  ;; assertion
  {:pre [(pos? x)]}
  (Point. x (Math/log x)))              ; => #'clj.type/log-point
(log-point -2)
(log-point Math/E)            ; => #clj.type.Point{:x 2.718281828459045, :y 1.0}

;; When to use maps or records
;; Most of the time, map is okay;
;; use records/types when you need for type-based polymorphism, or performance-sensitive field access
(= (Point. 3 4) (Point. 3 4))           ; => true
(= {:x 3, :y 4} (Point. 3 4))           ; => false
(= (Point. 3 4) {:x 3 :y 4})            ; => false


;;; Types. for low-lever infrastructure types, mostly related mutable fields
(deftype Point [x y])                   ; => clj.type.Point
(.x (Point. 3 4))                       ; => 3
(:x (Point. 3 4))                       ; => nil

;; mutable fields, have to explicitly qualify them with ^:volatile-mutable or ^:unsynchronized-mutable
(deftype MyType [^:volatile-mutable fld]) ; => clj.type.MyType

(deftype SchrödingerCat [^:unsynchronized-mutable state]
  ;; implement interface
  clojure.lang.IDeref
  ;; method of interface
  (deref [sc]
    (locking sc
      (or state
          (set! state (if (zero? (rand-int 2))
                        :dead
                        :alive))))))    ; => clj.type.SchrödingerCat
@(SchrödingerCat. nil)                  ; => :alive

(definterface TT (a []) (b []))         ; => clj.type.TT
(definterface TTT (c []) (d [] 1))      ; => clj.type.TTT
(deftype T [x y]
  TT
  (a [this] 1)
  (b [this] 2)
  TTT
  (c [this] 3)
  )                                     ; => clj.type.T
(.c (T. 3 4))                           ; => 3
#_(.d (T. 3 4))                         ; java.lang.AbstractMethodError


;;; Implementing Protocols
;; inline implementation
(defrecord Point [x y]
  Matrix
  (lookup [pt i j]
    (when (zero? j)
      (case i
        0 x
        1 y)))
  (update [pt i j value]
    (if (zero? j)
      (condp = i
        0 (Point. value y)
        1 (Point. x value))
      pt))
  (rows [pt] [[x] [y]])
  (cols [pt] [[x y]])
  (dims [pt] [2 1]))                    ; => clj.type.Point

;; extend
(defrecord Point [x y])                 ; => clj.type.Point
(extend-protocol Matrix
  Point
  (lookup [pt i j]
    (when (zero? j)
      (case i
        0 (:x pt)
        1 (:y pt))))
  (update [pt i j value]
    (if (zero? j)
      (condp = i
        0 (Point. value (:y pt))
        1 (Point. (:x pt) value))
      pt))
  (rows [pt]
    [[(:x pt)] [(:y pt)]])
  (cols [pt]
    [[(:x pt) (:y pt)]])
  (dims [pt] [2 1]))                    ; => nil

;; inline implementation is fast(low level), but not good to resolve conflicts
(defprotocol ClashWhenInlined
  (size [x]))                           ; => ClashWhenInlined
;; below will throw exception because size method is defined in java.util.Map
#_(defrecord R []
    ClashWhenInlined
    (size [x]))                           ; Exception, "Duplicate method name&signature in class file clj/type/R"
(defrecord R [])                        ; => clj.type.R
(extend-type R
  ClashWhenInlined
  (size [x]))                           ; => nil
(size (R.))                             ; => nil

;; inline implementation can't be changed in runtime unless redefining the whole type
;; inline implementation should be kept as an optimizing step(for performance)
;; For most of the cases, use extend implementation

;; inline implementation is the *only* way to implement Java interfaces and Object class(special case)
#_(deftype MyType [a b c]
    java.lang.Runnable
    (run [this] ...)
    Object
    (equals [this that] ...)
    (hashCode [this] ...)
    Protocol1
    (method1 [this ...] ...)
    Protocol2
    (method2 [this ...] ...)
    (method3 [this ...] ...))


;; Defining anonymous types with `reify'
;; like anonymous inner classes in Java
;; it not support dynamic update(inline); it can only implement methods of Java interface and Object class
(defn listener
  "Creates an AWT/Swing `ActionListener' that delegates to the given function."
  [f]
  (reify
    java.awt.event.ActionListener
    (actionPerformed [this e]
      (f e))))                          ; => #'clj.type/listener
