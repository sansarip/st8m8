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
  (for-all [[_forms-str forms [_ _ expected :as _first-fsm-form]] my-gen/forms-str-gen]
    (testing "get-map returns the first valid fsm"
      (is (= expected (parsley/get-map forms))))))

(defspec test-get-map-returns-nil-when-no-st8m8-map
  20
  (for-all [forms (my-gen/forms-gen :fsm? false)]
    (testing "get-map returns nil"
      (is (= nil (parsley/get-map forms))))))

(deftest test-get-map-returns-nil-for-empty-vector
  (testing "get-map returns nil for an empty vector"
    (is (= nil (parsley/get-map [])))))

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

