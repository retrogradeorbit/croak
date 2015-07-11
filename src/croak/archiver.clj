(ns croak.archiver
  (:require [croak.prober :as prober]
            [clj-time.format :as format]
            [me.raynes.fs :as fs]
            [clojure.java.io :as io]))


(def ^:dynamic *image-storage-path* "/tmp/storage")

(def ^:dynamic *time-format* "yyyy-MM-dd-HH:mm:ss.SSS")

(defn init-storage!
  ([]
   (init-storage! *image-storage-path*))
  ([path]
   (when-not (fs/exists? path)
     (when-not (fs/mkdir path)
       (throw (ex-info (str "Can't create storage path:" path)
                       {:type ::fs-exception}))))))


(defn make-filename [timestamp]
  (->> (str "data-" (-> *time-format*
                        format/formatter
                        (format/unparse timestamp)) ".edn")
       (io/file *image-storage-path*)))


(defn get-filenames []
  (let [items
        (filter seq
                (map
                 #(->> %
                       str
                       (re-seq #"data-(\d+\-\d+\-\d+ \d+:\d+:\d+\.\d+Z).edn")
                       first
                       reverse)
                 (fs/list-dir *image-storage-path*)))

        fname-parse (fn [[ind fname]]
                      [(-> *time-format*
                           format/formatter
                           (format/parse ind))
                       fname])]
    (into
     {}
     (map fname-parse items))))


(defn archive-watcher
  "builds a watcher function that behaves according to the values in
  the hashmap `config`"
  [{:keys [archive-count debug] :or {:archive-count 1000 :debug true}}]
  ^{:doc "returns a future that runs a watcher for data func that regularly
  writes it to file when size exceeds trigger. if the write succeeds
  remove those entries from the atom."}
  (fn
    [key refr old n]
    (when (>= (count n) archive-count)
      (let [timestamps (->> n keys sort (take archive-count))
            to-write (into {} (for [t timestamps] [t (n t)]))
            filename (make-filename (first timestamps))
            ]
        (when debug (println "writing" archive-count "records to" (str filename)))
        (future
          (spit filename  (prn-str to-write))
          (swap! refr
                 (fn [data]
                   ;; remove all the writen timestamps from atom
                   (apply dissoc data timestamps))))))))

(comment
  (add-watch croak.prober/=data= :archiver (archive-watcher {:archive-count 5}))
)
