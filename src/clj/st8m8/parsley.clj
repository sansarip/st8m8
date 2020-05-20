(ns st8m8.parsley
  (:require [cheshire.core :as json]
            [clojure.walk :refer [postwalk]]))

(defn st8m8? [m]
  (and (map? m) (:st8m8 (meta m))))

(defn ->json [m]
  (json/generate-string m {:escape-non-ascii true}))

;; helpful testing utility
(defn <-json [s]
  (json/parse-string s true))

;; TODO: generify this for .edn files
(defn treat-symbols [m]
  (postwalk (fn [form]
              (if (and (seq? form)
                       (= 2 (count form))
                       (= 'quote (first form)))
                (str "'" (second form))
                form))
            m))

(defn stringify [m]
  (reduce-kv (fn [c k v]
               (assoc c (if (string? k) k (pr-str k))
                        (cond
                          (map? v) (stringify v)
                          (string? v) v
                          :else (pr-str v))))
             {}
             m))

(defn get-map [forms]
  (cond
    (map? forms) forms
    ;; must check if ^:st8m8 metadata exists on a map if forms is a form or a vector of forms
    (list? forms) (let [m (last forms)]
                    (if (st8m8? m) (get-map m)))
    (vector? forms) (->> forms
                         (some #(let [m (last %)]
                                  (if (st8m8? m) m)))
                         get-map)
    :else nil))

(defn parse
  "Parses Clojure data and returns a serialized Java HashMap"
  [input]
  (let [[f & r :as forms] (read-string (str "[" input "]"))]
    (if-let [result (get-map
                      (cond
                        (and f (not r)) f
                        r forms
                        :else nil))]
      (-> result treat-symbols stringify ->json)
      "")))