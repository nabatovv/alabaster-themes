;; Full Clojure syntax sample.
;;
;; Coverage note: namespaces (:require / :import / :refer / :refer-clojure),
;; def / defonce / defn (multi-arity, variadic, :pre/:post), defmacro with
;; syntax-quote / unquote / unquote-splicing and gensym, destructuring,
;; let / if-let / when-let / cond / case / condp, threading macros
;; (-> ->> some-> as-> cond->), loop/recur, lazy sequences, transducers,
;; records / types / protocols, multimethods with a hierarchy,
;; atoms / refs / agents (with watches and validators), futures / delays /
;; promises, metadata, dynamic vars, Java interop, exceptions, regexes,
;; reader literals, keywords/symbols/namespaces, strings, chars and the
;; numeric tower.
;;
;; Only clojure.core is used, plus the bundled standard-library namespaces
;; clojure.string and clojure.set (both ship with Clojure, no external deps).
;; Every top-level form is safe to execute; run prints a series of results.

(ns sample
  "Full Clojure syntax sample for theme highlighting tests."
  (:refer-clojure :exclude [update])
  (:require
   [clojure.set :as cset]
   [clojure.string :as str :refer [upper-case]])
  (:import
   (java.util ArrayList Date UUID)))

;; ---------------------------------------------------------------------------
;; Constants, defonce and the numeric tower
;; ---------------------------------------------------------------------------

(def max-items 1024)
(def greeting "hello")
(def ratio 1/3)                    ; a clojure.lang.Ratio
(def a-bigint 12345678901234567890N)
(def a-bigdec 1.5M)
(def hex-value 0xFF)               ; 255
(def octal-value 017)              ; 15
(def radix-value 2r1010)           ; 10

(defonce ^:private state-store (atom {}))

(def ^:dynamic *level* 0)

;; ---------------------------------------------------------------------------
;; Functions: multi-arity, variadic, pre/post conditions
;; ---------------------------------------------------------------------------

(defn greet
  ([] (greet "world"))
  ([name] (str greeting ", " name))
  ([name & more] (str (greet name) " and " (count more) " more")))

(defn ^:private divide
  [dividend divisor]
  {:pre [(number? dividend) (not (zero? divisor))]
   :post [(number? %)]}
  (/ dividend divisor))

(defn sum-squares
  "Sum of the squares of xs (variadic friendly)."
  [& xs]
  (reduce + (map #(* % %) xs)))

;; ---------------------------------------------------------------------------
;; Macros: syntax-quote, unquote-splicing, gensym and auto-gensym
;; ---------------------------------------------------------------------------

(defmacro unless
  "Evaluate body when test is falsey."
  [test & body]
  `(if ~test nil (do ~@body)))

(defmacro twice
  "Evaluate expr twice and return a vector of both results."
  [expr]
  (let [sym (gensym "twice")]
    `(let [~sym ~expr] [~sym ~sym])))

(defmacro with-temp
  "Auto-gensym (#) example."
  [init & body]
  `(let [value# ~init] (do ~@body) value#))

;; ---------------------------------------------------------------------------
;; Destructuring: vector, map, nested, :as, :or, rest
;; ---------------------------------------------------------------------------

(defn describe-point
  [[x y & rest :as all] {:keys [label] :or {label "?"} :as meta-map}]
  (str label " (" x "," y ")"
       (when (seq rest) (str " +" (count rest) " extra"))
       " | keys=" (sort (keys meta-map)) " | n=" (count all)))

;; ---------------------------------------------------------------------------
;; Control flow: let / if-let / when-let / cond / case / condp
;; ---------------------------------------------------------------------------

(defn classify [n]
  (cond
    (neg? n) :negative
    (zero? n) :zero
    (pos? n) :positive))

(defn day-kind [day]
  (case day
    :sat :weekend
    :sun :weekend
    :weekday))

(defn rank [n]
  (condp = n
    1 :gold
    2 :silver
    3 :bronze
    :participant))

;; ---------------------------------------------------------------------------
;; Threading macros
;; ---------------------------------------------------------------------------

(defn pipeline-demo []
  (let [inc-double (-> 5 inc (* 2))                         ; -> 12
        sum-evens  (->> (range 10) (filter even?) (reduce +)) ; 20
        safe       (some-> {:a {:b 1}} :a :b inc)            ; 2
        squared    (as-> 5 x (* x x) (- x 1))                ; 24
        adjusted   (cond-> 1 true inc false (* 9) (= 2 true) (* 10))] ; 20
    [inc-double sum-evens safe squared adjusted]))

;; ---------------------------------------------------------------------------
;; loop / recur
;; ---------------------------------------------------------------------------

(defn factorial [n]
  (loop [i n acc 1]
    (if (<= i 1)
      acc
      (recur (dec i) (* acc i)))))

;; ---------------------------------------------------------------------------
;; Lazy sequences and transducers
;; ---------------------------------------------------------------------------

(def naturals (iterate inc 0))
(def repeating (cycle [:a :b :c]))

(defn seq-demo []
  (let [mapped   (map #(* % %) (range 5))
        filtered (filter odd? (range 10))
        taken    (take 4 naturals)
        rng      (range 2 12 3)]
    {:mapped (vec mapped)
     :filtered (vec filtered)
     :taken (vec taken)
     :cycled (vec (take 5 repeating))
     :range (vec rng)
     :reduced (reduce + 0 (range 1 6))}))

(def xf (comp (filter odd?) (map #(* % %))))

(defn transducer-demo []
  {:transduced (transduce xf + 0 (range 10))
   :into      (into [] xf (range 10))
   :iteration (vec (iteration (fn [x] (when (< x 5) (inc x)))
                              :initk 0 :vf identity))})

;; ---------------------------------------------------------------------------
;; Records, types and protocols
;; ---------------------------------------------------------------------------

(defprotocol Describable
  (describe [this]))

(defrecord Point [x y]
  Describable
  (describe [_] (str "Point(" x "," y ")")))

(deftype Celsius [degrees]
  Describable
  (describe [_] (str degrees "C"))
  Object
  (toString [_] (str "Celsius<" degrees ">")))

(extend-protocol Describable
  String
  (describe [s] (str "String(" s ")"))
  Long
  (describe [n] (str "Long(" n ")")))

(extend-type java.util.ArrayList
  Describable
  (describe [_] "ArrayList"))

;; ---------------------------------------------------------------------------
;; Multimethods with a hierarchy
;; ---------------------------------------------------------------------------

(derive ::square ::rect)

(defmulti area :shape)

(defmethod area ::circle [{:keys [r]}] (* Math/PI r r))
(defmethod area ::rect [{:keys [w h]}] (* w h))
(defmethod area :default [_] 0)

;; ---------------------------------------------------------------------------
;; State: atoms, refs, agents, watches and validators
;; ---------------------------------------------------------------------------

(def counter (atom 0 :validator #(>= % 0)))
(def account (ref 100))
(def worker (agent 0))

(add-watch counter :trace
           (fn [_ _ old new] (println "  atom" old "->" new)))
(add-watch account :trace
           (fn [_ _ old new] (println "  ref" old "->" new)))
(add-watch worker :trace
           (fn [_ _ old new] (println "  agent" old "->" new)))
(set-validator! worker #(<= 0 % 100))

(defn state-demo []
  (swap! counter inc)
  (dosync
   (alter account - 30))
  (swap! counter + 5)
  (send worker inc)
  (await worker)
  {:counter @counter
   :account @account
   :worker @worker})

;; ---------------------------------------------------------------------------
;; Futures, delays and promises
;; ---------------------------------------------------------------------------

(def ^:private cached (delay (reduce + (range 100))))

(defn concurrency-demo []
  (let [fut (future (Thread/sleep 10) :from-future)
        p   (promise)]
    (future (Thread/sleep 10) (deliver p :delivered))
    {:delay (force cached)
     :future @fut
     :promise (deref p 1000 :timeout)}))

;; ---------------------------------------------------------------------------
;; Metadata, dynamic vars and binding
;; ---------------------------------------------------------------------------

(def tagged (with-meta [1 2 3] {:source :sample}))

(defn meta-demo []
  {:private-meta (-> #'divide meta :private)
   :tagged-meta (meta tagged)
   :bound (binding [*level* 10] *level*)
   :unbound *level*})

;; ---------------------------------------------------------------------------
;; Java interop: new, ., static calls, doto, ..
;; ---------------------------------------------------------------------------

(defn interop-demo []
  (let [list1 (ArrayList.)
        list2 (new ArrayList)]
    (.add list1 1)
    (.add list1 2)
    {:upper (.toUpperCase "abc")
     :sqrt (Math/sqrt 16.0)
     :parse (Integer/parseInt "42")
     :value-of (String/valueOf 42)
     :chained (.. "Hi" toUpperCase toLowerCase)
     :doto (doto list2 (.add :a) (.add :b))
     :list1 list1}))

;; ---------------------------------------------------------------------------
;; Exceptions: try / catch / finally, ex-info / ex-data
;; ---------------------------------------------------------------------------

(defn exception-demo []
  (try
    (throw (ex-info "boom" {:code 7 :ctx :sample}))
    (catch clojure.lang.ExceptionInfo e
      {:message (ex-message e) :data (ex-data e)})
    (catch Exception e
      {:unexpected (.getMessage e)})
    (finally
      (println "  finally ran"))))

;; ---------------------------------------------------------------------------
;; Regexes and string operations
;; ---------------------------------------------------------------------------

(defn regex-demo []
  {:find   (re-find #"\d+" "abc123def")
   :groups (re-find #"(\w+)@(\w+)" "user@host")
   :all    (re-seq #"\w+" "the quick brown fox")})

(defn string-demo []
  {:upper (upper-case "clojure")
   :trim (str/trim "  padded  ")
   :split (str/split "a,b,c" #",")
   :join (str/join "-" [1 2 3])
   :replace (str/replace "a-b-c" "-" "+")
   :referred (upper-case "referred")})

;; ---------------------------------------------------------------------------
;; Reader literals, quoted/syntax-quoted forms and keywords/symbols
;; ---------------------------------------------------------------------------

(def a-set #{1 2 3 4})
(def an-instant #inst "2026-11-13T00:00:00Z")
(def an-uuid #uuid "00000000-0000-0000-0000-000000000000")
(def discarded #_ (throw (ex-info "never evaluated" {})) :kept)
(def quoted-list '(+ 1 2))
(def syntax-quoted `(let [x# 42] x#))
(def anon-fn #(vector % %1 %&))
(def composed #(reduce + %&))

(defn literals-demo []
  {:set a-set
   :instant? (instance? Date an-instant)
   :uuid? (instance? UUID an-uuid)
   :discarded discarded
   :quoted (eval quoted-list)
   :anon (#(+ %1 %2) 3 4)
   :rest-fn (composed 1 2 3 4)
   :kw-local ::circle
   :kw-ns (namespace ::circle)
   :kw-name (name ::circle)
   :sym (symbol "clojure.core" "map")
   :qualified (str/join "." [(:ns (meta #'reduce)) (:name (meta #'reduce))])})

;; ---------------------------------------------------------------------------
;; Collections basics and 1.11/1.12 helpers
;; ---------------------------------------------------------------------------

(defn helpers-demo []
  {:parse-long (parse-long "42")
   :update-vals (update-vals {:a 1 :b 2} inc)
   :update-keys (update-keys {1 :a 2 :b} str)
   :union (cset/union #{1 2} #{2 3})
   :intersection (cset/intersection #{1 2 3} #{2 3 4})
   :excluded-update (clojure.core/update {:n 1} :n inc)}) ; :refer-clojure excluded it

;; ---------------------------------------------------------------------------
;; Entry point: everything above is defined; run prints the showcase.
;; ---------------------------------------------------------------------------

(defn run []
  (println "== constants ==" [max-items greeting ratio a-bigint a-bigdec
                              hex-value octal-value radix-value])
  (println "== functions ==" (greet) "|" (greet "clojure")
           "|" (greet "a" "b" "c") "|" (divide 10 2) "|" (sum-squares 1 2 3))
  (println "== macros ==" (unless false :ran) (twice (inc 1))
           (with-temp 41 :ignored))
  (println "== destructuring =="
           (describe-point [1 2 3 4] {:label "P" :meta true}))
  (println "== control ==" (classify -3) (classify 0) (classify 3)
           (day-kind :sat) (rank 2))
  (println "== threading ==" (pipeline-demo))
  (println "== loop/recur ==" (factorial 5))
  (println "== sequences ==" (seq-demo))
  (println "== transducers ==" (transducer-demo))
  (println "== protocols ==" (describe (->Point 1 2)) (describe (Celsius. 20))
           (describe "hi") (describe 42) (describe (ArrayList.)))
  (println "== multimethods ==" (area {:shape ::circle :r 1})
           (area {:shape ::square :w 2 :h 3}) (area {:shape ::triangle}))
  (println "== state ==" (state-demo))
  (println "== concurrency ==" (concurrency-demo))
  (println "== metadata/dynamic ==" (meta-demo))
  (println "== interop ==" (interop-demo))
  (println "== exceptions ==" (exception-demo))
  (println "== regex ==" (regex-demo))
  (println "== strings ==" (string-demo))
  (println "== literals ==" (literals-demo))
  (println "== helpers ==" (helpers-demo))
  (println "== done =="))

(run)
(shutdown-agents)
