(use 'clojure.pprint)
(require '[clojure.string :as string])

(defn ns-req->repl-req [[f & reqs]]
  (conj (map (partial str "'") reqs) (name f)))

(def ns-regex #"\(ns .*\s*\(:require ((\[.*\])|\s)*\)\)")

(defn fix-ns []
  (let [source (slurp "src/clj/st8m8/parsley.clj")
        fixed-ns (-> (str "[" source "]")
                     read-string
                     ;; Drop the ns and keep the requirements
                     (update 0 #(-> %
                                    (nth 2)
                                    ns-req->repl-req
                                    (->> (map read-string))))
                     first
                     pprint
                     with-out-str)]
    (str (string/replace source ns-regex fixed-ns)
         "\n\n(find-fsm (slurp *input*))")))

(spit "src/main/resources/scripts/parse.clj" (fix-ns))