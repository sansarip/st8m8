(defproject st8m8 "0.1.0-SNAPSHOT"
  :description "A Clojure parser for the St8m8 plugin"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]]
  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :repl-options {:init-ns st8m8.parsley}
  :profiles {:dev {:dependencies [[org.clojure/test.check "0.10.0"]
                                  [tupelo "0.9.201"]]}})