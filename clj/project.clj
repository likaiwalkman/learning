(defproject clj "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  ;; :aot [clj.genclass]
  ;; :main clj.interop
  :global-vars {*warn-on-reflection* true
                *assert* false}
  :dependencies [[org.clojure/clojure "1.7.0-alpha5"]
                 [org.clojure/tools.nrepl "0.2.10"]
                 [enlive "1.1.5"]
                 [robert/hooke "1.3.0"]
                 [org.clojure/java.jdbc "0.3.6"]
                 [org.xerial/sqlite-jdbc "3.8.7"]
                 ;; connection pool
                 [c3p0/c3p0 "0.9.1.2"]])
