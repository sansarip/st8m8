(ns st8m8.parsley-test
  (:require [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [st8m8.parsley :refer [parse]]
            [st8m8.test-utilities :as tu]
            [clojure.string :as string]))

(def fsm-gen (->> (gen/map gen/keyword (gen/return nil))
                  (gen/map (gen/one-of [gen/keyword gen/symbol]))
                  (gen/fmap tu/connect-edges)
                  (gen/fmap #(with-meta % {:st8m8 true}))))
(def fsm-str-gen (gen/fmap tu/prn-str* fsm-gen))
(def fsm-form-gen (->> (gen/one-of [fsm-gen gen/any])
                       (gen/tuple (gen/return 'def) gen/symbol-ns)
                       (gen/fmap (comp reverse #(into '() %)))))
(def fsm-forms-gen (->> fsm-form-gen
                        (gen/vector)))

(defspec test-parse-returns-equivalent-map-str 100
         (prop/for-all [[m m-str] (gen/fmap #(vector % (tu/prn-str* %)) fsm-gen)]
                       (tu/is= (prn-str m) (parse m-str))))

(defspec test-parse-first-st8m8-from-forms 100
         (prop/for-all [[f f-str] (gen/fmap (fn [forms]
                                              [(if-let [r (some #(if (= (meta (last %)) {:st8m8 true}) (last %))
                                                                forms)]
                                                 (prn-str r)
                                                 "")
                                               (reduce #(string/join "\n" [% (tu/prn-str* %2)]) "" forms)])
                                            fsm-forms-gen)]
                       (tu/is= f (parse f-str))))



