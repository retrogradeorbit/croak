(ns croak.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [croak.prober :as prober]
            [croak.archiver :as archiver])
  (:gen-class))

(def cli-options
  [["-c" "--config CONFIG" "Configuration file"
    :default nil]
   ["-h" "--help"]])

(defn testit []
  (add-watch prober/=data= :archiver (archiver/archive-watcher {:archive-count 5000
                                                                :debug true}))
  (prober/prober
      {:delay 500
       :align-times true
       :debug true}))


(defn -main
  [& args]
  (println (parse-opts args cli-options))
  (try (deref (testit))
       (finally
         ;; http://dev.clojure.org/jira/browse/CLJ-959
         (shutdown-agents))))
