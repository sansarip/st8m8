(ns st8m8.generators
  (:require [clojure.test.check.generators :as gen]
            [st8m8.test-utilities :as tu]))

(def fsm-gen
  (let [key-gen (gen/one-of [gen/keyword gen/symbol])]
    (->> (gen/map key-gen (gen/return nil))
         (gen/map key-gen)
         (gen/fmap tu/connect-edges)
         (gen/fmap #(with-meta % {:st8m8 true})))))

;; Always adds first-form, even when forms are empty
(defn before-first-fsm
  "Conj `first-form` before the first st8m8-fsm-form in `forms`.
  Always conj's `first-form`, even if `forms` is empty
  or does not contains a st8m8-form."
  [[first-form forms]]
  [first-form (loop [c []
                     done? false
                     [[_ _ fsm :as this-form] & rest-forms] forms]
                (cond (and fsm (not done?))
                      (if (:st8m8 (meta fsm))
                        (recur (apply conj c first-form this-form rest-forms) true rest-forms)
                        (recur (conj c this-form) false rest-forms))
                      (and (not fsm) (not done?))
                      (conj c first-form)
                      :else c))])

(defn def-form-gen [content-gen]
  (->> content-gen
       (gen/tuple (gen/return 'def) gen/symbol-ns)
       (gen/fmap (comp reverse #(into '() %)))))

;; TODO: Refactor this
(def quoted-fsm
  (gen/fmap (fn [fsm]
              (reduce-kv
                (fn [c k v]
                  (assoc c (list 'quote k)
                           (reduce-kv (fn [c k v]
                                        (assoc c
                                          (list 'quote k)
                                          (list 'quote v)))
                                      {}
                                      v)))
                {}
                fsm))
            fsm-gen))


(def a-common-generator (gen/one-of [gen/keyword
                                     gen/string-ascii
                                     gen/large-integer
                                     gen/boolean]))

(defn with-metadata-gen
  ([expression-gen always-metadata?]
   (->> expression-gen
        (gen/tuple (cond-> (gen/map gen/keyword a-common-generator)
                           always-metadata? gen/not-empty))
        (gen/fmap (fn [[metadata obj]]
                    (try (with-meta obj metadata)
                         ;; obj cannot have metadata
                         (catch ClassCastException _ obj))))))
  ([expression-gen]
   (with-metadata-gen expression-gen false)))

(defn forms-gen
  ([& {:keys [first-form-gen fsm?]
       :or   {fsm? true}}]
   (cond-> (gen/one-of (cond-> [(with-metadata-gen gen/any)]
                               fsm? (conj fsm-gen)))

           '-> def-form-gen
           '-> gen/vector
           '-> gen/not-empty
           (gen/generator? first-form-gen) (->> (gen/tuple first-form-gen)
                                                (gen/fmap before-first-fsm))))
  ([] (forms-gen nil)))

(def forms-str-gen
  (->> fsm-gen
       def-form-gen
       (forms-gen :first-form-gen)
       (gen/fmap (fn [[first-fsm forms]]
                   (vector (tu/forms->str forms)
                           forms
                           first-fsm)))))