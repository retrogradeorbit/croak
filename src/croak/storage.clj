(ns croak.storage
  (:require [me.raynes.fs :as fs]
            [clojure.java.io :as io]
            [clj-time.format :as format]))

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
