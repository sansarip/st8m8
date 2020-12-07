(ns st8m8.parsley-test
  (:require [clojure.test :refer [is testing deftest]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :refer [for-all]]
            [st8m8.generators :as my-gen]
            [st8m8.parsley :as parsley]
            [clojure.pprint :refer [pprint]]
            [st8m8.test-utilities :as tu]
            [clojure.string :as string]
            [cheshire.core :as json]))

(defspec test-get-map-returns-first-st8m8-map
  20
  (for-all [[_forms-str forms [_ _ expected :as _first-fsm-form]] (my-gen/forms-str-gen)]
    (testing "get-map returns the first valid fsm"
      (is (= expected (parsley/get-st8m8-map forms))))))

(defspec test-get-map-returns-nil-when-no-st8m8-map
  20
  (for-all [forms (my-gen/forms-gen :fsm? false)]
    (testing "get-map returns nil"
      (is (= nil (parsley/get-st8m8-map forms))))))

(deftest test-get-map-returns-nil-for-empty-vector
  (testing "get-map returns nil for an empty vector"
    (is (= nil (parsley/get-st8m8-map [])))))

(defspec test-treat-quoted-symbols-removes-quoting-from-states
  20
  (for-all [fsm my-gen/quoted-fsm]
    (let [treated-fsm (parsley/treat-quoted-symbols fsm)]
      (testing "Every outbound-state, transition, and inbound-state is not quoted"
        (is (->> treated-fsm
                 vals
                 (map vals)
                 (conj [(keys fsm)])
                 flatten
                 (every? tu/not-quoted?)))))))

(defspec test-find-returns-json-with-same-length
  20
  (for-all [[forms-str _ [_ _ fsm]] (my-gen/forms-str-gen)]
    (let [json (parsley/find-fsm forms-str)]
      (testing "The resulting json has the same length as the input map"
        (is (= (count fsm))
            (= (count (json/parse-string json))))))))

(deftest test-find-returns-empty-map-json-for-empty-str
  (= nil (parsley/find-fsm "")))

(defspec test-find-returns-empty-map-json-for-non-maps
  20
  (for-all [[forms-str] (my-gen/forms-str-gen :fsm? false)]
    (= nil (parsley/find-fsm forms-str))))

#_
(defspec test-replace-returns-empty-str-when-no-fsm
  20
  (for-all [[forms-str] (my-gen/forms-str-gen :fsm? false)]
    (is (= "" (parsley/replace-fsm forms-str "{}")))))