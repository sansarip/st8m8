(defproject st8m8 "0.1.0-SNAPSHOT"
  :plugins [[lein-exec "0.3.7"]]
  :description "A Clojure parser for the St8m8 plugin"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [de.ubercode.clostache/clostache "1.4.0"]
                 [cheshire "5.10.0"]
                 [rewrite-clj "0.6.1"]]
  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :aliases {"exec:template" ["exec" "-p" "templater.clj"]}
  :repl-options {:init-ns st8m8.parsley}
  :aot :all
  :profiles {:dev {:dependencies [[org.clojure/test.check "0.10.0"]]}})
