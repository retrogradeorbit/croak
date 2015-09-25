(ns croak.tail
  (:require [croak.time :refer [wait-until next-aligned-time]]
            [clj-time.core :as time]
            [croak.prober :as prober]
            [clojure.java.io :as io]
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

(defn tailer
  "tailer main loop"
  [{:keys [name file delay align-times debug process]
    :or {:delay 5000
         :align-times true
         :debug false
         :process identity}
    :as config}]
  (let [next-tick (if align-times
                    (next-aligned-time delay)
                    (time/now))
        tchan (tail-chan file)]
    (when align-times (wait-until next-tick))

    (loop [t next-tick]
      (let [n (->> delay
                   time/millis
                   (time/plus t))]

        (let [d (<!! tchan)
              v (eval (list process (<!! tchan)))]

          (when debug
            (println "tailer" name ":" (str t) ":" v))

          (swap! prober/=data=
                 (fn [data]
                   (assoc data t
                          (assoc
                           (get data t {})
                           name v)))))

        (wait-until n)
        (recur n)))))




(comment
  (def c (tail-chan "/var/log/syslog"))

                                        ;(take! c (comp println prn-str))

  (def thr (future
             (loop []
               (let [lines (<!! c)]
                 (println (prn-str lines)))
               (Thread/sleep 1000)
               (recur))))

  (future-cancel thr)

  (def fut (future (test-tail-f "/var/log/syslog")))

  (future-cancel fut))
