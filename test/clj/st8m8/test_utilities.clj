(ns st8m8.test-utilities
  (:require [clojure.pprint :refer [pprint]]
            [clojure.string :as string]))

(defn pprint* [s]
  (-> s pprint with-out-str string/trim))

(defn prn-str* [o]
  (binding [*print-meta* true]
    (prn-str o)))

(defn connect-edges
  ([m]
   (reduce-kv connect-edges m m))
  ([c k v]
   (let [nv (reduce-kv (partial connect-edges c) v v)]
     (assoc c k nv)))
  ([p c k _]
   (let [pk (keys p)
         n (->> pk (random-sample 0.5) first)]
     (assoc c k (or n (first pk))))))

(defn forms->str [forms]
  (->> forms
       (map prn-str*)
       (interpose "\n")
       (apply str)))
