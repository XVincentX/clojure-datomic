(defproject app "0.1.0-SNAPSHOT"
  :description "Stuff"
  :url "https://github.com/XVincentX/clojure-playground"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [com.datomic/client-pro "0.9.57"]
                 [io.pedestal/pedestal.service "0.5.7"]
                 [io.pedestal/pedestal.route "0.5.7"]
                 [io.pedestal/pedestal.jetty "0.5.7"]
                 [org.clojure/data.json "1.0.0"]
                 [org.slf4j/slf4j-simple "1.7.30"]
                 [environ "1.1.0"]]
  :repl-options {:init-ns app.core}
  :min-lein-version "2.9.3"
  :profiles {:uberjar {:aot :all}}
  :main app.core)
