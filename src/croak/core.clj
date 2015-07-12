(ns croak.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [croak.prober :as prober]
            [croak.probes.iptables :refer [iptables]]
            [croak.archiver :as archiver])
  (:gen-class))

(def cli-options
  [["-c" "--config CONFIG" "Configuration file"
    :default nil]
   ["-h" "--help"]])

(defn testit []
  (add-watch prober/=data= :archiver (archiver/archive-watcher {:archive-count 1000
                                                                :debug true}))
  (prober/prober
      {:delay 500
       :align-times true
       :debug true}))


(defn shutdown-hook []
  (let [to-write @prober/=data=
        timestamps (keys to-write)
        filename (archiver/make-filename (first timestamps))]
    (println "writing" (count to-write) "records to" (str filename))
    (spit filename  (prn-str (archiver/data->disk to-write)))))


(defn add-shutdown-hook []
  (.addShutdownHook
   (Runtime/getRuntime)
   (Thread. shutdown-hook)))


(defn -main
  [& args]
  (add-shutdown-hook)
  (println (parse-opts args cli-options))
  (try (deref (testit))
       (finally
         ;; http://dev.clojure.org/jira/browse/CLJ-959
         (shutdown-agents)
         )))
