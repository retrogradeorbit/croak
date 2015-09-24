(ns croak.tail
  (:require [clojure.java.io :as io]
            [clojure.core.async :refer [chan go <! >! take! <!! thread]])
  (:import [java.io RandomAccessFile]))


(defn process-tail [file pos]
  (let [flen (.length file)]
    (if (< flen pos)
      ;; file has shortened
      (recur file 0)
      (if (> flen pos)
        ;;file has lengthened
        (let [fh (RandomAccessFile. file "r")]
          (try
            (.seek fh pos)

            (let [lines-read (loop [lines []]
                               (if-let [line (.readLine fh)]
                                 (recur (conj lines line))
                                 lines))]

              ;; return the new pos, and the lines read
              [(.getFilePointer fh) lines-read])

            (finally (.close fh))))

        ;; file hasn't changed
        [pos []]))))

(defn test-tail-f [filename]
  (let [file (io/file filename)]
    (loop [pos (.length file)]
      (let [[pos lines] (process-tail file pos)]
        (println (prn-str lines))
        (Thread/sleep 1000)
        (recur pos)))))

(defn tail-chan [filename]
  (let [c (chan)]
    (go
      (let [file (io/file filename)]
        (loop [pos (.length file)]
          (let [[pos lines] (process-tail file pos)]
            (>! c lines)
            (recur pos)))))
    c))
(def fut (future (test-tail-f "/var/log/syslog")))

(future-cancel fut)
