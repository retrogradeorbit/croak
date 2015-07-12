(ns croak.storage
  (:require [me.raynes.fs :as fs]
            [clojure.java.io :as io]
            [clj-time.format :as format]
            [clj-time.coerce :as coerce]
            [clojure.edn :as edn]))

(def ^:dynamic *image-storage-path* "/tmp/storage")
(def ^:dynamic *time-format* "yyyy-MM-dd-HH:mm:ss.SSS")
(def ^:dynamic *time-re* #"data-(\d+\-\d+\-\d+-\d+:\d+:\d+\.\d+).edn")

(defn data->disk
  "preprocessor to prepare data before its written to disk. Turns
  timestamps into longs to reduce the size of the edn file"
  [data]
  (into
   {} (for [[k v] data] [(coerce/to-long k) v])))

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
                       (re-seq *time-re*)
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

(defn load-file [filename]
  (edn/read-string (slurp (io/file *image-storage-path* filename))))
