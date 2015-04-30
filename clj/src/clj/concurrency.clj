(ns clj.concurrency
  (:use (clojure [pprint :only (pprint)]
                 [repl :only (doc)])))
;;-----------------------------------------------------------
;;; Chapter 4: Concurrency and Parallelism
;;-----------------------------------------------------------

;; delay, when forced (with force or deref/@), will evaluate once then cache the result
(def d (delay (println "Running...")
              :done!))
(deref d)                               ; => :done!
@d                                      ; => :done!
(macroexpand '@d)                       ; => (clojure.core/deref d)

(defn get-document
  [id]
                                        ; ... do some work to retrieve the identified document's metadata ...
  {:url "http://www.mozilla.org/about/manifesto.en.html"
   :title "The Mozilla Manifesto"
   :mime "text/html"
   :content (delay (slurp "http://baidu.com"))})

(def d (get-document "some-id"))
(realized? (:content d))                ; => false
@(:content d)
(realized? (:content d))                ; => true

;; Future, future will run in another thread
(def long-calculation (future (apply + (range 1e8))))
@long-calculation
(deref (future (Thread/sleep 5000) :done!)
       ;; timeout
       1000
       ;; timeout value
       :impatient!)                     ; => :impatient!

;; Promises, can be set a value *once* by `diliver'
(def p (promise))                       ; => #'clj.core/p
(realized? p)                           ; => false
(deliver p 42)                     ; => #<core$promise$reify__6996@4cf9a739: 42>
(realized? p)                      ; => true
@p                                 ; => 42

(def a (promise))                       ; => #'clj.core/a
(def b (promise))                       ; => #'clj.core/b
(def c (promise))                       ; => #'clj.core/c
(future
  (deliver c (+ @a @b))
  (println "Delivery complete!!")) ; => #<core$future_call$reify__6953@6ecaf2fc: :pending>
(deliver a 2)                      ; => #<core$promise$reify__6996@23166ffa: 2>
(deliver b 8)                      ; => #<core$promise$reify__6996@dc4e3b8: 8>
@c                                 ; => 10

;; Promise don't detect cyclic dependencies
;; below code will block indefinitely
#_(deliver p @p)

;; Promise are very useful for dealing with async operations
(defn call-service
  [arg1 arg2 callback-fn]
  ;; ...async
  (future (callback-fn (+ arg1 arg2) (- arg1 arg2)))) ; => #'clj.core/call-service
(defn sync-fn
  [async-fn]
  (fn [& args]
    (let [result (promise)]
      ;; synchronously return the args of callback-fn
      (apply async-fn (conj (vec args) #(deliver result %&)))
      @result)))                        ; => #'clj.core/sync-fn
((sync-fn call-service) 8 7)            ; => (15 1)

(defn phone-number
  [string]
  (re-seq #"(\d{3})[\.-]?(\d{3})[\.-]?(\d{4})" string))
(phone-number "123-333.2222")         ; => (["123-333.2222" "123" "333" "2222"])

(def files (repeat 100
                   (apply str
                          (concat (repeat 100000 \space)
                                  "Sunil: 232.035.5777, Betty: 233.547.7456"))))
(time (dorun (map phone-number files)))
(time (dorun (pmap phone-number files)))

(def files (repeat 100000
                   (apply str
                          (concat (repeat 1000 \space)
                                  "Sunil: 617.555.2937, Betty: 508.555.2218"))))
(time (dorun (map phone-number files)))  ; Elapsed time: 1516.586777 msecs
;; 过多的小task并行，提升的效率不足以抵销并行本身的开销
(time (dorun (pmap phone-number files))) ; Elapsed time: 1606.872801 msecs

;; use chunk to combine small tasks
(time (->> files
           (partition-all 250)
           (pmap (fn [chunk] (doall (map phone-number chunk))))
           (apply concat)
           dorun))                      ; Elapsed time: 894.438165 msecs


;;; Clojure Reference Types
;;-----------------------------------------------------------
;; 4 reference types: var, ref, agent, atom
@(atom 12)                              ; => 12
@(agent {:c 42})
(map deref [(agent {:c 42}) (atom 12)
            (ref "http://clojure.org") (var +)]) ; => ({:c 42} 12 "http://clojure.org" #<core$_PLUS_ clojure.core$_PLUS_@3d5a6899>)
;; deref will return a *snapshot* of a reference, and deref will never block

(defmacro futures
  [n & exprs]
  (vec (for [_ (range n)
             expr exprs]
         `(future ~expr))))
(defmacro wait-futures
  [& args]
  `(doseq [f# (futures ~@args)]
     @f#))

;; Atoms, synchro- nous, uncoordinated, atomic compare-and-set modification
(def sarah (atom {:name "Sarah" :age 25 :wears-glasses? false}))
;; use swap! for atom
(swap! sarah update-in [:age] + 3) ; => {:age 28, :name "Sarah", :wears-glasses? false}
;; if the atom's value changes, swap! will retry with the newer value
(def xs (atom #{1 2 3}))                ; => #'clj.core/xs
(wait-futures 1
              (swap! xs (fn [v]
                          (Thread/sleep 250)
                          (println "trying 4")
                          (conj v 4)))
              (swap! xs (fn [v]
                          (Thread/sleep 500)
                          (println "trying 5")
                          (conj v 5))))
@xs                                     ; => #{1 4 3 2 5}

;; bare compare-and-set!
(compare-and-set! xs :wrong "new value") ; => false
(compare-and-set! xs @xs "new value")    ; => true
;; reset! to force reset
(reset! xs :y)                          ; => :y

;;; Notifications and Constraints
;; Watches
(defn echo-watch
  [key identity old new]
  (println key old "=>" new))
(def martin (atom {:name "martin" :age 25}))
(add-watch martin :echo echo-watch)
(add-watch martin :echo2 echo-watch)
(swap! martin update-in [:age] inc)     ; => {:age 26, :name "martin"}
(remove-watch martin :echo2)     ; => #<Atom@b5c8bf8: {:age 30, :name "martin"}>
;; watch not guarantee the state is different
(reset! martin @martin)

(def history (atom ()))                 ; => #'clj.core/history
(defn log->list
  [dest-atom key source old new]
  (when (not= old new)
    (swap! dest-atom conj new)))
(def martin (atom {:name "martin" :age 25}))

(add-watch martin :record (partial log->list history))
(swap! martin update-in [:age] inc)     ; => {:age 26, :name "martin"}
(swap! martin assoc :wears-glasses? true)
(clojure.pprint/pprint @history)

;; Validator, return true or false
(def n (atom 1 :validator pos?))        ; => #'clj.core/n
(swap! n + 500)                         ; => 501
#_(swap! n - 1000)                      ; java.lang.IllegalStateException: "Invalid reference state"
;; set-validator!
(def sarah (atom {:name "Sarah" :age 25})) ; => #'clj.core/sarah
(set-validator! sarah :age)                ; => nil
#_(swap! sarah dissoc :age) ; java.lang.IllegalStateException: "Invalid reference state"
(set-validator! sarah #(or (:age %)
                           (throw (IllegalStateException. "People must have `:age's!")))) ; => nil
#_(swap! sarah dissoc :age)

;;; Refs, coordinated reference type, using STM
;; A multiplayer game
(defn character
  [name & {:as opts}]
  (ref (merge {:name name :items #{} :health 500}
              opts)))                   ; => #'clj.core/character
(def smaug (character "Smaug" :health 500 :strength 400 :items (set (range 50))))
(def bilbo (character "Bilbo" :health 100 :strength 100))
(def gandalf (character "Gandalf" :health 75 :mana 750))
;; dosync and alter, All modifications of refs must occur within a transaction, when conflict, there will be retrying with new refs
(defn loot
  [from to]
  (dosync
   (when-let [item (first (:items @from))]
     (alter to update-in [:items] conj item)
     (alter from update-in [:items] disj item)))) ; => #'clj.core/loot
(wait-futures 1
              (while (loot smaug bilbo))
              (while (loot smaug gandalf))) ; => nil
@smaug              ; => {:strength 400, :name "Smaug", :items #{}, :health 500}
@bilbo ; => {:strength 100, :name "Bilbo", :items #{0 7 20 39 46 4 21 31 32 40 33 13 22 36 41 43 29 44 6 28 25 17 3 12 2 23 47 35 19 11 9 5 45 26 16 38 30 10 18 42 37 8}, :health 100}
@gandalf ; => {:mana 750, :name "Gandalf", :items #{27 1 24 15 48 34 14 49}, :health 75}
(map (comp count :items deref) [bilbo gandalf]) ; => (42 8)
(filter (:items @bilbo) (:items @gandalf))      ; => ()

;; `alter' will fail when the order isn't right
;; `commute' can be used when reorderability is acceptable
;; commute will never cause conflict, and the in-transaction value is not guaranteed to be eventual committed value
(def x (ref 0))                         ; => #'clj.core/x
(time (wait-futures 5
                    (dotimes [_ 1000]
                      (dosync (alter x + (apply + (range 1000)))))
                    (dotimes [_ 1000]
                      (dosync (alter x - (apply + (range 1000))))))) ; "Elapsed time: 1027.664 msecs"
(time (wait-futures 5
                    (dotimes [_ 1000]
                      (dosync (commute x + (apply + (range 1000)))))
                    (dotimes [_ 1000]
                      (dosync (commute x - (apply + (range 1000))))))) ; "Elapsed time: 204.655 msecs"

;; rewrite loot
(defn fixed-loot
  [from to]
  (dosync
   (when-let [item (first (:items @from))]
     ;; commute here because `to' will not conflict
     (commute to update-in [:items] conj item)
     ;; alter here because from will be updated by different threads
     (alter from update-in [:items] disj item))))
(def smaug (character "Smaug" :health 500 :strength 400 :items (set (range 50))))
(def bilbo (character "Bilbo" :health 100 :strength 100))
(def gandalf (character "Gandalf" :health 75 :mana 750))
(wait-futures 1
              (while (fixed-loot smaug bilbo))
              (while (fixed-loot smaug gandalf)))
(map (comp count :items deref) [bilbo gandalf]) ; => (41 9)
(filter (:items @bilbo) (:items @gandalf))      ; => ()

;; attack, heal
(defn attack
  [aggressor target]
  (dosync
   (let [damage (* (rand 0.1) (:strength @aggressor))]
     ;; commute can be used because decrementing a number will not conflict
     (commute target update-in [:health] #(max 0 (- % damage))))))
(defn heal
  [healer target]
  (dosync
   (let [aid (* (rand 0.1) (:mana @healer))]
     (when (pos? aid)
       (commute healer update-in [:mana] - (max 5 (/ aid 5)))
       (commute target update-in [:health] + aid)))))

(def alive? (comp pos? :health))        ; => #'clj.core/alive?
(defn play
  [character action other]
  (while (and (alive? @character)
              (alive? @other)
              (action character other))
    (Thread/sleep (rand-int 50))))
(wait-futures 1
              (play bilbo attack smaug)
              (play smaug attack bilbo))
(map (comp :health deref) [smaug bilbo]) ; => (458.60940890768944 0)

;; reset health
(dosync
 (alter smaug assoc :health 500)
 (alter bilbo assoc :health 100))

(wait-futures 1
              (play bilbo attack smaug)
              (play smaug attack bilbo)
              (play gandalf heal bilbo))
(map (comp #(select-keys % [:name :health :mana]) deref)
     [smaug bilbo gandalf]) ; => ({:health 0, :name "Smaug"} {:health 94.29314753431505, :name "Bilbo"} {:mana -0.9306288523772537, :health 75, :name "Gandalf"})

;; `ref-set'
(dosync (ref-set bilbo {:name "Bilbo"})) ; => {:name "Bilbo"}
;; above is the same as
(dosync (alter bilbo (constantly {:name "Bilbo"}))) ; => {:name "Bilbo"}

;; Rewrite by adding validators
(defn- enforce-max-health
  [name health]
  (fn [character-data]
    (or (<= (:health character-data) health)
        (throw (IllegalStateException. (str name " is already at max health!"))))))
(defn character
  [name & {:as opts}]
  (let [cdata (merge {:name name :items #{} :health 500}
                     opts)
        cdata (assoc cdata :max-health (:health cdata))
        validators (list* (enforce-max-health name (:health cdata))
                          (:validators cdata))]
    (ref (dissoc cdata :validators)
         :validator #(every? (fn [v] (v %)) validators))))
(def bilbo (character "Bilbo" :health 100 :streanth 100))
(def gandalf (character "Gandalf" :health 75 :mana 750))

#_(heal gandalf bilbo)                  ; IllegalStateException

(dosync (alter bilbo assoc-in [:health] 95)) ; => {:max-health 100, :streanth 100, :name "Bilbo", :items #{}, :health 95}
;; below will throw exception, because heal will make the health bigger than 100
#_(heal gandalf bilbo)
;; rewrite heal to cover this case
(defn heal
  [healer target]
  (dosync
   (let [aid (min (* (rand 0.1) (:mana @healer))
                  (- (:max-health @target) (:health @target)))]
     (when (pos? aid)
       (commute healer update-in [:mana] - (max 5 (/ aid 5)))
       (alter target update-in [:health] + aid)))))
(heal gandalf bilbo) ; => {:max-health 100, :streanth 100, :name "Bilbo", :items #{}, :health 100}

;;; STM's sharp corners
;; use `io!' to prevent wrong usage of side-effecting functions in transaction
(defn unsafe
  []
  (io! (println "writing to database...")))
#_(dosync (unsafe))                     ; will throw IllegalStateException

;; live lock, for deadlock, STM have some fallbacks
(def x (ref 0))                         ; => #'clj.core/x
;; below will throw exception: "Transaction failed after reaching retry limit"
#_(dosync
   @(future (dosync (ref-set x 1)))
   (ref-set x 2))

;; reader may cause transaction retry, because there may be some writers commited during read transactoin
;; ref have history to cover this case. for example, we read a, but a is updating, then we read a's history
(def a (ref 0))
(future (dotimes [_ 500]
          (dosync
           (Thread/sleep 200)
           (alter a inc)))) ; => #<core$future_call$reify__6953@120d15a2: :pending>
@(future (dosync (Thread/sleep 1000) @a)) ; => 39
(ref-max-history a)                       ; => 10
(ref-history-count a)                     ; => 5

;; Write skew, when an read ref changed out of the transaction, then the result will inconsistent
;; we can use `ensure' to prevent other transaction change the ref
(def daylight (ref 1))                  ; => #'clj.core/daylight
(defn attack
  [aggressor target]
  (dosync
   ;; if daylight changes outside, we don't know it
   (let [damage (* (rand 0.1) (:strength @aggressor) @daylight)]
     (commute target update-in [:health] #(max 0 (- % damage))))))

;;; Vars
map                                   ; => #<core$map clojure.core$map@455e7ae6>
#'map                                 ; => #'clojure.core/map
@#'map                                ; => #<core$map clojure.core$map@455e7ae6>
(macroexpand '#'map)                  ; => (var map)
(macroexpand '@#'map)                 ; => (clojure.core/deref (var map))

;; private vars
;; Can only be referred to using its fully qualified name when in another namespace.
;; Its value can only be accessed by manually deferencing the var.
(def ^:private everything 42)           ; => #'clj.core/everything

;; docstring
(def a
  "a sample doc"
  3)                                    ; => #'clj.core/a
(clojure.repl/doc a)
(meta #'a) ; => {:ns #<Namespace clj.core>, :name a, :file "/tmp/form-init2348055155997652946.clj", :column 1, :line 1, :doc "a sample doc"}

;; Constants
(def ^:const max-value 42)             ; => #'clj.core/everything
(defn valid-value?
  [v]
  ;; max-value is ^:const, then it will be captured here at compile-time
  ;; so change max-value will never impact this function
  (<= v max-value))                     ; => #'clj.core/valid-value?
(def max-value 50)                      ; => #'clj.core/max-value
(valid-value? 45)                       ; => false

;;; Dynamic Scope
(def ^:dynamic *max-value* 255)         ; => #'clj.core/*max-value*
(defn valid-value?
  [v]
  (<= v *max-value*))                   ; => #'clj.core/valid-value?
(binding [*max-value* 500]
  (valid-value? 299))                   ; => true
;; binding in only thread-local change
(binding [*max-value* 500]
  (println (valid-value? 299))
  (doto (Thread. #(println "in other thread:" (valid-value? 299)))
    .start
    .join))

;; define a http get function
(defn http-get
  [url-string]
  (let [conn (-> url-string java.net.URL. .openConnection)
        response-code (.getResponseCode conn)]
    (if (== 404 response-code)
      [response-code]
      [response-code (-> conn .getInputStream slurp)])))
(http-get "http://baidu.com/")

;; using dynamic
(def ^:dynamic *response-code* nil)     ; => #'clj.core/*response-code*
(defn http-get
  [url-string]
  (let [conn (-> url-string java.net.URL. .openConnection)
        response-code (.getResponseCode conn)]
    (when (thread-bound? #'*response-code*)
      ;; set! is for thread local set
      (set! *response-code* response-code))
    (when (not= 404 response-code)
      (-> conn .getInputStream slurp)))) ; => #'clj.core/http-get
(http-get "http://baidu.com") ; => "<html>\n<meta http-equiv=\"refresh\" content=\"0;url=http://www.baidu.com/\">\n</html>\n"
*response-code*               ; => nil
(binding [*response-code* nil]
  (let [content (http-get "http://baidu.com")]
    (println "Response code was:" *response-code*)
    (println content)))

;; Dynamic scope propagates through clojure-native concurrency forms
(binding [*max-value* 500]
  (println (valid-value? 299))
  ;; future propagate the scope to other thread
  @(future (valid-value? 299)))         ; => true
(binding [*max-value* 500]
  ;; map not propagate dynamic scope
  (map valid-value? [299]))             ; => (false)
;; use this instead
(map #(binding [*max-value* 500]
        (valid-value? %))
     [299])                             ; => (true)

;; var is not variables
;; def always defines top level vars in the nampspace
;; but you still can changing a var's Root Binding
(def x 0)                               ; => #'clj.core/x
(alter-var-root #'x inc)                ; => 1

;; Forward Declarations
(def j)                                 ; => #'clj.core/j
j                                       ; => #<Unbound Unbound: #'clj.core/j>
;; better using declare
(declare complex-helper-fn other-helper-fn)
(defn public-api-funciotn
  [arg1 arg2]
  ;; use helper functions
  (other-helper-fn arg1 arg2 (complex-helper-fn arg1 arg2)))
;; define helper functions later
(defn- complex-helper-fn
  [arg1 arg2]
  '...)
(defn- other-helper-fn
  [arg1 arg2]
  '...)

;;; Agents, uncoordinated, asynchronous. Agents will queue actions, and it's safe for tranaction retry(actions will hold until commited)
;; send, using a fixed-size thread pool, so it should not be used for blocking operations(e.g. IO)
;; send-off, using an unbounded thread pool(the same one used by futures)
(def a (agent 500))                     ; => #'clj.core/a
(send a range 1000)
@a

;; `await'
(def a (agent 500))                    ; => #'clj.core/a
(def b (agent 1000))                   ; => #'clj.core/b
(send-off a #(Thread/sleep %))          ; => #<Agent@436efd24: 5000>
(send-off b #(Thread/sleep %))          ; => #<Agent@16b72f4c: 10000>
@a                                      ; => 5000
;; await will block to wait actions done, wait-for has a timeout
(await-for 600 a b)
@a                                      ; => nil
@b                                      ; => 10000

;; Error handling
(def a (agent nil))                     ; => #'clj.core/a
(send a (fn [_] (throw (Exception. "something is wrong")))) ; => #<Agent@6fc9a4c8 FAILED: nil>
a                                       ; => #<Agent@6fc9a4c8 FAILED: nil>
;; below will throw Exception
#_(send a identity)
;; restart agent, optional :clear-actions flag
(restart-agent a 42)                    ; => 42
(send a inc)                            ; => #<Agent@6fc9a4c8: 43>
(reduce send a (for [x (range 3)]
                 (fn [_] (throw (Exception. (str "error #" x)))))) ; => #<Agent@6fc9a4c8: 43>
(agent-error a)                  ; => #<Exception java.lang.Exception: error #0>
(restart-agent a 42)             ; => 42
(agent-error a)                  ; => #<Exception java.lang.Exception: error #1>
(restart-agent a 42 :clear-actions true) ; => 42
(agent-error a)                          ; => nil

;; error handlers and modes, :fail(the default) or :continue
;; use :continue mode, it will ignore errors
(def a (agent nil :error-mode :continue)) ; => #'clj.core/a
(send a (fn [_] (throw (Exception. "something is wrong"))))
(send a identity)                       ; => #<Agent@4994a307: nil>
;; use error handlers
(def a (agent nil
              :error-mode :continue
              :error-handler (fn [the-agent exception]
                               (.println *out* (.getMessage exception))))) ; => #'clj.core/a
(send a (fn [_] (throw (Exception. "something is wrong"))))
(send a identity)                       ; => #<Agent@207c05c3: nil>

;; change error mode
(set-error-handler! a (fn [the-agent exception]
                        (when (= "FATAL" (.getMessage exception))
                          (set-error-mode! the-agent :fail))))
(send a (fn [_] (throw (Exception. "FATAL"))))
#_(send a identity)                     ; will throw Exception

;; Persisting reference states with an agent-based write-behind log
(require '[clojure.java.io :as io])     ; => nil
(def console (agent *out*))             ; => #'clj.core/console
(def character-log (agent (io/writer "/tmp/character-states.log" :append true))) ; => #'clj.core/character-log

(defn write
  [^java.io.Writer w & content]
  (doseq [x (interpose " " content)]
    (.write w (str x)))
  (doto w
    (.write "\n")
    .flush))                            ; => #'clj.core/write

(defn log-reference
  [reference & writer-agents]
  (add-watch reference :log
             (fn [_ reference old new]
               (doseq [writer-agent writer-agents]
                 (send-off writer-agent write new))))) ; => #'clj.core/log-reference

(def smaug (character "Smaug" :health 500 :strength 400)) ; => #'clj.core/smaug
(def bilbo (character "Bilbo" :health 100 :strength 100)) ; => #'clj.core/bilbo
(def gandalf (character "Gandalf" :health 75 :mana 1000)) ; => #'clj.core/gandalf
(log-reference bilbo console character-log) ; => #<Ref@14442b61: {:max-health 100, :strength 100, :name "Bilbo", :items #{}, :health 100}>
(log-reference smaug console character-log) ; => #<Ref@577cbcb1: {:max-health 500, :strength 400, :name "Smaug", :items #{}, :health 500}>
(wait-futures 1
              (play bilbo attack smaug)
              (play smaug attack bilbo)
              (play gandalf heal bilbo))

;; using log
(defn attack
  [aggressor target]
  (dosync
   (let [damage (* (rand 0.1) (:strength @aggressor) (ensure daylight))]
     (send-off console write
               (:name @aggressor) "hits" (:name @target) "for" damage)
     (commute target update-in [:health] #(max 0 (- % damage))))))
(defn heal
  [healer target]
  (dosync
   (let [aid (min (* (rand 0.1) (:mana @healer))
                  (- (:max-health @target) (:health @target)))]
     (when (pos? aid)
       (send-off console write
                 (:name @healer) "heals" (:name @target) "for" aid)
       (commute healer update-in [:mana] - (max 5 (/ aid 5)))
       (alter target update-in [:health] + aid)))))
(dosync
 (alter smaug assoc :health 500)
 (alter bilbo assoc :health 100)) ; => {:max-health 100, :strength 100, :name "Bilbo", :items #{}, :health 100}
(wait-futures 1
              (play bilbo attack smaug)
              (play smaug attack bilbo)
              (play gandalf heal bilbo))


;;; Using agents to parallelize workloads
(require '[net.cgrand.enlive-html :as enlive])
(use '[clojure.string :only (lower-case)])
(import '(java.net URL MalformedURLException))
(defn- links-from
  [base-url html]
  (remove nil? (for [link (enlive/select html [:a])]
                 (when-let [href (-> link :attrs :href)]
                   (try
                     (URL. base-url href)
                     ;; ignore bad URLs
                     (catch MalformedURLException e))))))
(defn- words-from
  [html]
  (let [chunks (-> html
                   (enlive/at [:script] nil)
                   (enlive/select [:body enlive/text-node]))]
    (->> chunks
         (mapcat (partial re-seq #"\w+"))
         (remove (partial re-matches #"\d+"))
         (map lower-case))))

(def url-queue (java.util.concurrent.LinkedBlockingQueue.)) ; => #'clj.core/url-queue
(def crawled-urls (atom #{}))           ; => #'clj.core/crawled-urls
(def word-freqs (atom {}))              ; => #'clj.core/word-freqs

(declare get-url)                       ; => #'clj.core/get-url
(def agents (set (repeatedly 25 #(agent {::t #'get-url :queue url-queue})))) ; => #'clj.core/agents

(declare run process handle-results)    ; => #'clj.core/handle-results
(defn ^::blocking get-url
  [{:keys [^java.util.concurrent.BlockingQueue queue] :as state}]
  (let [url (io/as-url (.take queue))]
    (try
      (if (@crawled-urls url)
        state
        {:url url
         :content (slurp url)
         ::t #'process})
      (catch Exception e
        ;; skip any URL we failed to load
        state)
      (finally (run *agent*)))))        ; => #'clj.core/get-url
(defn process
  [{:keys [url content]}]
  (try
    (let [html (enlive/html-resource (java.io.StringReader. content))]
      {::t #'handle-results
       :url url
       :links (links-from url html)
       :words (reduce (fn [m word]
                        (update-in m [word] (fnil inc 0)))
                      {}
                      (words-from html))})
    (finally (run *agent*))))           ; => #'clj.core/process
(defn ^::blocking handle-results
  [{:keys [url links words]}]
  (try
    (swap! crawled-urls conj url)
    (doseq [url links]
      (.put url-queue url))
    (swap! word-freqs (partial merge-with +) words)
    {::t #'get-url :queue url-queue}
    (finally (run *agent*))))

(defn paused? [agent]
  (::paused (meta agent)))              ; => #'clj.core/paused?
(defn run
  ([] (doseq [a agents] (run a)))
  ([a]
   (when (agents a)
     (send a (fn [{transition ::t :as state}]
               (when-not (paused? *agent*)
                 (let [dispatch-fn (if (-> transition
                                           meta
                                           ::blocking)
                                     send-off
                                     send)]
                   (dispatch-fn *agent* transition)))
               state)))))               ; => #'clj.core/run
(defn pause
  ([] (doseq [a agents] (pause a)))
  ([a] (alter-meta! a assoc ::paused true))) ; => #'clj.core/pause
(defn restart
  ([] (doseq [a agents] (restart a)))
  ([a]
   (alter-meta! a dissoc ::paused)
   (run a)))                            ; => #'clj.core/restart

(defn test-crawler
  "Resets all state associated with the crawler, adds the given URL to the url-queque,
   and runs the crawler for 60 seconds, returning a vector containing the number of URLs crawled,
   and the number of URLs accumulated through crawling that have yet to be visited."
  [agent-count starting-url]
  (def agents (set (repeatedly agent-count
                               #(agent {::t #'get-url :queue url-queue}))))
  (.clear url-queue)
  (swap! crawled-urls empty)
  (swap! word-freqs empty)
  (.add url-queue starting-url)
  (run)
  (Thread/sleep 60000)
  (pause)
  [(count @crawled-urls) (count url-queue)]) ; => #'clj.core/test-crawler

(test-crawler 25 "https://news.ycombinator.com/") ; => [82, 6086]
