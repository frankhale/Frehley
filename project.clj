(defproject editor "0.2.5-SNAPSHOT"
  :description "A rudimentary editor that uses the Ace editor control"
  :url "http://github.com/frankhale/editor"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2202"]]
  :plugins [[lein-cljsbuild "1.0.3"]
			[org.bodil/lein-noderepl "0.1.11"]]
  :cljsbuild
	{:builds
		[{:source-paths ["src"],
		  :id "dev",
		  :compiler
			{:output-to "resources/scripts/editor.js",
			 :foreign-libs [{:file "resources/scripts/ace/ace.js"
                             :provides ["ace"]}]
			 :optimizations :whitespace
			 :pretty-print true}}]})
