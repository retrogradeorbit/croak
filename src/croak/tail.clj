(ns croak.tail
  (:require [clojure.java.io :as io])
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

