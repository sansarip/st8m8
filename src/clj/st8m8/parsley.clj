(ns st8m8.parsley
  (:gen-class
    :name st8m8.parsley
    :methods [#^{:static true} [find_fsm [String] String]
              #^{:static true} [replace_fsm [String String] String]])
  (:require [cheshire.core :as json]
            [rewrite-clj.node :as n]
            [rewrite-clj.parser :as p]
            [clojure.pprint :refer [pprint]]
            [clojure.walk :refer [postwalk]]
            [clojure.string :as string]))

(defn st8m8? [m]
  (and (map? m) (:st8m8 (meta m))))

(defn ->json [m]
  (json/generate-string m {:escape-non-ascii true}))

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

(defn get-st8m8-map [forms]
  (cond
    (map? forms) forms
    ;; Must check if ^:st8m8 metadata exists on a map if forms is a form or a vector of forms
    (list? forms) (let [m (last forms)]
                    (if (st8m8? m) (get-st8m8-map m)))
    (vector? forms) (->> forms
                         (some #(let [m (last %)]
                                  (if (st8m8? m) m)))
                         get-st8m8-map)
    :else nil))

(defn pprn-str [forms]
  (binding [*print-meta* true]
    (with-out-str (pprint forms))))

(defn get-st8m8-node [nodes]
  (if-let [forms (try (n/sexpr nodes) (catch Exception _))]
    (get-st8m8-map forms)))

(defn get-st8m8-node-indexed [nodes]
  (loop [i 0]
    (if (< i (count nodes))
      (let [node (get nodes i)]
        (if (get-st8m8-node node)
          [node i]
          (recur (inc i)))))))

(defn find-fsm
  "Returns a JSON string representation of a St8M8-fsm represented in Clojure,
  symbols are quoted e.g. 'symbol"
  [file-contents]
  (let [[f & r :as forms] (try (read-string (str "[" file-contents "]"))
                               (catch Exception _))]
    (if-let [result (get-st8m8-map
                      (cond
                        (and f (not r)) f
                        r forms
                        :else nil))]
      (-> result treat-quoted-symbols stringify ->json))))

(defn -find_fsm [file-path]
  (find-fsm (slurp file-path)))

(defn json->replacement-node [{tag :tag children :children :as node} replacement]
  ;;TODO: Incorporate existing metadata on the fsm map
  (let [;;TODO: Requires error-handling and testing
        replacement-node (fn [metadata]
                           (n/meta-node
                             (n/token-node metadata)
                             (n/map-node (postwalk
                                           #(cond-> % (string? %) read-string)
                                           (json/parse-string replacement)))))]
    (n/coerce
      (if (= :list tag)
        (n/list-node (map
                       #(if-let [st8m8 (get-st8m8-node %)]
                          (replacement-node (meta st8m8))
                          %)
                       children))
        replacement-node))))

(defn replace-fsm
  "Replaces a St8M8-fsm represented in Clojure with a JSON representation of an fsm
  after transforming the JSON representation of an fsm to a Clojure representation."
  [file-contents replacement-json]
  (let [{node-children :children :as nodes} (p/parse-string (str "[" file-contents "]"))
        node-children-vec (vec node-children)]
    (if-let [[node index] (get-st8m8-node-indexed node-children-vec)]
      (reduce
        #(str % (n/string %2))
        ""
        (assoc node-children-vec index (json->replacement-node node replacement-json))))))

(defn -replace_fsm [file-path replacement-json]
  (replace-fsm (slurp file-path) replacement-json))

(defn -main [])
