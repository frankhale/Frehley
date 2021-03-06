(defproject frehley "0.4.1-SNAPSHOT"
  :description "Frehley is a toy text editor based on NW.js and the Ace editor library"
  :url "http://github.com/frankhale/Frehley"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.9.946"]
                 [jayq "2.5.5"]]
  :plugins [[lein-cljsbuild "1.1.7"]]
  :cljsbuild
  {:builds
    [{:source-paths ["src"],
      :id "dev",
      :compiler
      {:main "editor.core",
       :output-to "resources/scripts/editor.js",
       :foreign-libs [{:file "resources/scripts/ace/ace.js"
                             :provides ["ace"]}]
       :optimizations :whitespace
       :pretty-print true}}]})
