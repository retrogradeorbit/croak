(ns croak.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [croak.prober :as prober]
            [croak.probes.iptables :refer [iptables]]
            [croak.archiver :as archiver]
            [croak.storage :as storage]
            [croak.tail :as tail]
            [croak.reporter :as reporter])
  (:gen-class))

(def cli-options
  [["-c" "--config CONFIG" "Configuration file"
    :default nil]
   ["-h" "--help"]])

(defn run-probes [probes]
  (map #(future (prober/prober %))
       probes))

(defn run-tails [tails]
  (map #(future (tail/tailer %))
       tails))


(defn shutdown-hook []
  (let [to-write @prober/=data=
        timestamps (keys to-write)
        filename (storage/make-filename (first timestamps))]
    ;(println "writing" (count to-write) "records to" (str filename))
    (spit filename  (prn-str (storage/data->disk to-write)))))


(defn add-shutdown-hook []
  (.addShutdownHook
   (Runtime/getRuntime)
   (Thread. shutdown-hook)))


(defn -main
  [& args]
  (let [{:keys [options arguments errors summary]}
        (parse-opts args cli-options)]
    (cond (:help options) (println summary)
          (:config options)
          (let [config-file (:config options)
                conf-data (slurp config-file)
                config (read-string conf-data)]

            (add-shutdown-hook)

            ;; start up reporter. this sends the data to the server
            (future
              (try
                (reporter/reporter (:reporter config))
                (catch Exception e
                  (println "Reporter has exited with following exception:")
                  (clojure.stacktrace/print-stack-trace e))))

            ;; setup the archiver (to disk) watch on the atom
            (add-watch prober/=data= :archiver
             (archiver/archive-watcher {:archive-count 1000
                                        :debug true}))

            (try
              (let [probes (run-probes (:probes config))
                    tails (run-tails (:tails config))]

                (doall (map deref (concat probes tails))))
              (finally
                ;; http://dev.clojure.org/jira/browse/CLJ-959
                (shutdown-agents)))))))
