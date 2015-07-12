(defproject croak "0.1.0-SNAPSHOT"
  :description "A remote host health monitoring and reporting application"
  :url "https://github.com/retrogradeorbit/croak"
  :license {:name "GNU General Public License version 3"
            :url "http://www.gnu.org/licenses/gpl-3.0.en.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [clj-time "0.8.0"]
                 [org.clojure/tools.cli "0.3.1"]
                 [me.raynes/fs "1.4.6"]
                 [http-kit "2.1.18"]
                 ]
  :main ^:skip-aot croak.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
