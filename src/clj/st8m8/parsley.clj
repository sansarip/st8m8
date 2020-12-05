(ns st8m8.parsley
    (:gen-class
      :name st8m8.parsley
      :methods [#^{:static true} [find_fsm [String] String]])
    (:require [cheshire.core :as json]
      [rewrite-clj.node :as rwn]
      [rewrite-clj.parser :as rwp]
      [clojure.pprint :refer [pprint]]
      [clojure.walk :refer [postwalk]]
      [clojure.string :as string]))

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

(defn get-st8m8-map-indexed [forms]
      (some #(if (get-st8m8-map %) %) forms)
      (loop [i 0
             start-pos 0]
            (if (< i (count forms))
              (let [form (get forms i)
                    form-end-col (:end-col (meta form))
                    st8m8 (get-st8m8-map form)
                    {st8m8-start-col :col
                     st8m8-end-col   :end-col} (meta st8m8)]
                   (if st8m8
                     [(+ start-pos (cond-> st8m8-start-col (> st8m8-start-col 0) inc))
                      st8m8]
                     ;; TODO: Adding form-end-col is inaccurate if the form has newlines
                     (recur (inc i) (+ start-pos form-end-col)))))))

(defn read-string* [input]
      (try (read-string (str "[" input "]"))
           (catch Exception e
             (pprint e))))
#_(defn parse-string [input]
        (try (e/parse-string (str "[" input "]") {:syntax-quote true})
             (catch Exception e
               (pprint e))))

(defn find_fsm
      "Returns a JSON string representation of a St8M8-fsm represented in Clojure,
      symbols are quoted e.g. 'symbol"
      [file-contents]
      (let [[f & r :as forms] (read-string* file-contents)]
           (if-let [result (get-st8m8-map
                             (cond
                               (and f (not r)) f
                               r forms
                               :else nil))]
                   (-> result treat-quoted-symbols stringify ->json)
                   "{}")))

(defn -find_fsm [file-path]
      (find_fsm (slurp file-path)))


(defn json->replacement-str [st8m8-form replacement]
      (let [?def (if (list? st8m8-form)
                   (take
                     (dec (count st8m8-form))
                     st8m8-form))]
           (cond-> (with-meta (<-json replacement) {:st8m8 true})
                   ?def (->>
                          list
                          (concat ?def))
                   '-> pprn-str)))

(defn replace-lines [content start-index end-index replacement]
      (str
        (subs content 0 (dec start-index))
        (pprn-str replacement)
        (subs content end-index)))

;; TODO: Using edamame doesn't actually seem to be workable in babashka
;; TODO: Look into just making this into a jar and making it a gradle dependency
(defn replace-fsm
      "Replaces a St8M8-fsm represented in Clojure with a JSON representation of an fsm
      after transforming the JSON representation of an fsm to a Clojure representation."
      [file-contents replacement]
      #_(let [[f & r :as forms] (parse-string file-contents)]
             (if-let [st8m8-form (get-st8m8-map-indexed forms)]
                     ;; TODO: Get the st8m8 form and its start and end character indices relative to the whole doc
                     (let [{:keys [row end-row]} (meta st8m8-form)]
                          (replace-lines
                            file-contents
                            row
                            end-row
                            (json->replacement-str st8m8-form replacement)))
                     "")))

(defn -main [])
