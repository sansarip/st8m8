(ns st8m8.parsley-test
  (:require [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [st8m8.parsley :refer [parse]]
            [st8m8.test-utilities :as tu]))

(def fsm-gen (->> (gen/map gen/keyword (gen/return nil))
                  (gen/map (gen/one-of [gen/keyword gen/symbol]))
                  (gen/fmap tu/connect-edges)
                  (gen/fmap #(with-meta % {:st8m8 true}))))
(def fsm-str-gen (gen/fmap tu/prn-str* fsm-gen))
(def fsm-form-str-gen (->> fsm-gen
                           (gen/tuple gen/symbol-ns)
                           (gen/fmap (comp reverse #(into '() %)))
                           (gen/fmap tu/prn-str*)))

(defspec test-parse-returns-string 100
         (prop/for-all [m (gen/one-of [fsm-str-gen fsm-form-str-gen])]
                       (parse m)))

