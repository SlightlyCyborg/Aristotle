(defproject elliot "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [org.clojure/core.async "0.3.443"]
                 [ring "1.6.2"]
                 [clj-http "3.7.0"]
                 [http-kit "2.2.0"]
                 [compojure "1.6.0"]
                 [hiccup "1.0.5"]
                 [garden "1.3.2"]
                 [cheshire "5.7.1"]
                 [stuarth/clj-oauth2 "0.3.2"]
                 [org.apache.opennlp/opennlp-tools "1.8.1"]
                 [korma "0.4.0"]
                 [org.xerial/sqlite-jdbc "3.7.15-M1"]
                 [pegasus "0.7.0"]
                 [clj-solr "1.1"]]
  :main elliot.server)


