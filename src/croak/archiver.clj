(ns croak.archiver)

(comment
  (defn archive-watcher
  "watcher for data func that regularly writes it
  to file when size exceeds trigger"
  [key refr old n]
  (when (>= (count n) archive-count)
    (let [timestamps (->> n keys sort (take archive-count))
          to-write (into {} (for [t timestamps] [t (n t)]))]
      (future (println "writing timestamps!" archive-count)
              (spit (make-filename (first timestamps)) (prn-str to-write))
              (swap! refr
                     (fn [data]
                       (apply dissoc data timestamps))))))))
