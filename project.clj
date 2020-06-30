(defproject app "0.1.0-SNAPSHOT"
  :description "Stuff"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [io.pedestal/pedestal.service       "0.5.8"]
                 [io.pedestal/pedestal.jetty         "0.5.8"]
                 [io.pedestal/pedestal.immutant      "0.5.8"]
                 [io.pedestal/pedestal.tomcat        "0.5.8"]]
  :repl-options {:init-ns app.core}
  :main app.core)
