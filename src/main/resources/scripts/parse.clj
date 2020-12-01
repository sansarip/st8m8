(require '[cheshire.core :as json] '[clojure.walk :refer [postwalk]])


(defn st8m8? [m]
  (and (map? m) (:st8m8 (meta m))))

(defn ->json [m]
  (json/generate-string m {:escape-non-ascii true}))

;; Helpful testing utility
(defn <-json [s]
  (json/parse-string s true))

(defn treat-quoted-symbols [m]
  (postwalk (fn [form]
              (if (and (seq? form)
                       (= 2 (count form))
                       (= 'quote (first form)))
                (second form)
                form))
            m))

;; TODO: Generify this for .edn files
(defn stringify [m]
  (postwalk (fn [form]
              (cond
                (string? form) (str "\"" form "\"")
                (symbol? form) (str "'" form)
                (not (coll? form)) (pr-str form)
                :else form))
            m))

(defn get-map [forms]
  (cond
    (map? forms) forms
    ;; Must check if ^:st8m8 metadata exists on a map if forms is a form or a vector of forms
    (list? forms) (let [m (last forms)]
                    (if (st8m8? m) (get-map m)))
    (vector? forms) (->> forms
                         (some #(let [m (last %)]
                                  (if (st8m8? m) m)))
                         get-map)
    :else nil))

(defn first-fsm-form [forms]
  (loop [i 0]
    (if (< i (count forms))
      (let [form (get forms i)
            next-i (inc i)]
        (if (get-map form)
          [i form]
          (recur next-i))))))

(defn find-fsm
  "Returns a JSON string representation of a St8M8-fsm represented in Clojure,
  symbols are quoted e.g. 'symbol"
  [file-contents]
  (let [[f & r :as forms] (read-string (str "[" file-contents "]"))]
    (if-let [result (get-map
                      (cond
                        (and f (not r)) f
                        r forms
                        :else nil))]
      (-> result treat-quoted-symbols stringify ->json)
      "{}")))

(defn replace-fsm
  "Replaces a St8M8-fsm represented in Clojure with a JSON representation of an fsm
  after transforming the JSON representation of an fsm to a Clojure representation."
  [file-contents replacement]
  (let [[f & r :as forms] (read-string (str "[" file-contents "]"))]
    (if-let [[index form] (first-fsm-form forms)]
      "")))



(find-fsm (slurp *input*))