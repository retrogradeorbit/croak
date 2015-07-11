(ns croak.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [croak.prober :as prober]
            [croak.archiver :as archiver])
  (:gen-class))

(def cli-options
  [["-c" "--config CONFIG" "Configuration file"
    :default nil]
   ["-h" "--help"]])

(defn -main
  [& args]
  (println (parse-opts args cli-options)))

(defn testit []
  (def f (future (prober/prober
                  {:delay 5000
                   :align-times true
                   :debug true}
                  )))

  (add-watch prober/=data= :archiver (archiver/archive-watcher {:archive-count 5
                                                                :debug true}))

  (comment
    (future-cancel f))

)

(testit)
