(ns croak.probes.meminfo
  (:require [clojure.java.shell :as shell]
            [clojure.java.io :as io]
            [clojure.string :as string]))

(defn camel-to-hyphen [s]
  (->> s
       (re-seq #"[A-Z][a-z]*")
       (map string/lower-case )
       (string/join "-")))

(defn meminfo-line [[k v]]
  [(keyword (string/replace k #"[\(\)]" "_")) (read-string v)])

(defn meminfo []
  (into {} (map
            #(->> %
                  (re-seq #"(\S+):\s+(\d+)")
                  first
                  next
                  meminfo-line
                  )

            (string/split-lines
             ;; https://clojuredocs.org/clojure.core/slurp
             ;; On Linux, some JVMs have a bug where they cannot read a file in the /proc
             ;; filesystem as a buffered stream or reader.  A workaround to this JVM issue
             ;; is to open such a file as unbuffered:
             (slurp (java.io.FileReader. "/proc/meminfo"))))))
