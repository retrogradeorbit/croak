(ns croak.tail)


(defn process-tail [file pos]
  (let [flen (.length file)]
    (if (< flen pos)
      ;; file has shortened
      (recur file 0)
      (if (> len pos)
        ;;file has lengthened
        (let [fh (doto
                     (RandomAccessFile file "r")
                   (.seek pos))]
          (try
            ;; return the new pos, and the lines read
            [(.getFilePointer fh) (loop [lines []]
                                    (if-let [line (.readLine fh)]
                                      (recur (conj lines line))
                                      lines))]

            (finally (.close fh))))))))
