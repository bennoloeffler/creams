(defproject clojurenote-demo "0.1.0"
  :description "Demonstration app to show usage of clojurenote"
  :url "https://github.com/mikebroberts/clojurenote"
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [compojure "1.6.2"]
                 [environ "1.2.0"]
                 [clojurenote "0.4.0"]]

  :plugins [[lein-ring "0.12.5"]
            [lein-environ "1.2.0"]]

  :ring {:handler clojurenote-demo.handler/app}
  :profiles {:dev {:dependencies [[ring-mock "0.1.5"]]}})
