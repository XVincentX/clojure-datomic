(defproject app "1.0.0"
  :description "Test application written my be to do stupid stuff"
  :url "https://github.com/XVincentX/clojure-playground"
  :license {:name "EPL-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [com.datomic/client-pro "0.9.63"]
                 [io.pedestal/pedestal.service "0.5.8"]
                 [io.pedestal/pedestal.route "0.5.8"]
                 [io.pedestal/pedestal.jetty "0.5.8"]
                 [org.clojure/data.json "1.0.0"]
                 [org.slf4j/slf4j-simple "1.7.30"]
                 [environ "1.2.0"]]
  :plugins [[lein-ancient "0.6.15"]]
  :repl-options {:init-ns app.core}
  :min-lein-version "2.9.4"
  :aot [app.core]
  :uberjar-name "app.jar"
  :main app.core)
