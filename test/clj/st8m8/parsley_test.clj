(ns st8m8.parsley-test
  (:require [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [st8m8.parsley :refer [parse]]))

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


(def fsm-gen (->> (gen/map gen/keyword (gen/return nil))
                  (gen/map (gen/one-of [gen/keyword gen/symbol]))
                  (gen/fmap connect-edges)
                  (gen/fmap #(with-meta % {:st8m8 true}))))
(def fsm-str-gen (gen/fmap prn-str* fsm-gen))
(def fsm-form-str-gen (->> fsm-gen
                           (gen/tuple gen/symbol-ns)
                           (gen/fmap (comp reverse #(into '() %)))
                           (gen/fmap prn-str*)))

(defspec test-parse-returns-string 10
         (prop/for-all [m (gen/one-of [fsm-str-gen fsm-form-str-gen])]
                       (parse m)))

