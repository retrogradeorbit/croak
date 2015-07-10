(ns croak.core
  (:require [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

;; the ram storage of the probers results
(def =data= (atom {}))

(def cli-options
  [["-c" "--config CONFIG" "Configuration file"
    :default nil]
   ["-h" "--help"]])

(defn -main
  [& args]
  (println (parse-opts args cli-options)))
