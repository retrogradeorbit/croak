(ns croak.archiver
  (:require [croak.prober :as prober]
            [croak.storage :as storage]))

(defn archive-watcher
  "builds a watcher function that behaves according to the values in
  the hashmap `config`"
  [{:keys [archive-count debug]
    :or {:archive-count 1000 :debug  true}}]
  ^{:doc "returns a future that runs a watcher for data func
  that regularly writes it to file when size exceeds trigger. if the
  write succeeds remove those entries from the atom."}
  (fn
    [key refr old n]
    (when (>= (count n) archive-count)
      (let [timestamps (->> n keys sort (take archive-count))
            to-write (into {} (for [t timestamps] [t (n t)]))
            filename (storage/make-filename (first timestamps))
            ]
        (when debug (println "writing" archive-count "records to" (str filename)))
        (future
          (spit filename  (prn-str (storage/data->disk to-write)))
          (swap! refr
                 (fn [data]
                   ;; remove all the writen timestamps from atom
                   (apply dissoc data timestamps))))))))

(comment
  (add-watch croak.prober/=data= :archiver (archive-watcher {:archive-count 5}))
)
