(ns croak.reporter
  (:require [org.httpkit.client :as http]
            [croak.storage :as storage]
            [croak.prober :as prober]
            [clojure.data.json :as json]
            [clojure.java.io :as io])
)

(def ^:dynamic *base-opts*
  {
   :timeout 4000
   :user-agent "Croak/0.1"})

(defn send-data [data opts]
  (let [d (assoc (into *base-opts* opts)
                 :form-params
                 {
                  :host "knives"
                  :data (json/write-str data)}
                 )
        {:keys [status headers body error] :as resp}
        @(http/request d)]
    (when error
      (println "Failed, exception: " error)
      ;(println "HTTP success: " status)
      )
    status))

(defn upload-file [filename]
  ;(println "loading" filename)
  (if (= 200 (-> filename
                   storage/load-edn
                   ;println
                   (send-data {:url "http://localhost:5000/data"
                               :method :post})))
    (do ;(println "deleting" filename)
        (io/delete-file (str "/tmp/storage/" filename))
        true)
    false))

(defn upload-data [data]
  ;(println "uploading ram" (count data))
  (when (= 200 (-> data
                   storage/data->disk
                   (send-data {:url "http://localhost:5000/data"
                               :method :post})))
    ;(println "cleaning ram of" (count data))
    (swap! prober/=data=
           (fn [d]
             (apply dissoc d (keys data))))))

(defn reporter
  "reporter mainline function"
  [{:keys [minimum-set minimum-send]
    :or {minimum-set 3
         minimum-send 10
         }
    :as config}]
  ;; (println "reporter :" (sort (storage/get-filenames)))
  ;; (println "minimum-set" minimum-set)
  ;; (println "minimum-send" minimum-send)

    ;; upload all files, oldest to newest

  (loop [remaining (sort (storage/get-filenames))]
                                        ;(wait-until-up "localhost.localdomain")

    ;(println "." (first remaining))
    ;; upload any files first
    (if-let [time-file (first remaining)]
      (if (upload-file (second time-file))
        ;; upload success
        (when (seq (rest remaining))
          (recur (rest remaining)))

        ;; upload again
        (do (Thread/sleep 7000)
            (recur remaining)))))

    ;; if no more files, try uploading memory

  (let [data @prober/=data=
        num (count data)]
    ;(println "num" num)
    (when (>= num (+ minimum-send minimum-set))
      ;; upload all leave minimum-set behind
      ;(println "upload")
      (let [report-count (- num minimum-set)
            timestamps (->> data keys sort (take report-count))
            ;_ (println "!" timestamps)
            to-report (into {} (for [t timestamps] [t (data t)]))]
        ;(println "rc:" report-count)
        (upload-data to-report))))

  (Thread/sleep 10000)
  (recur config))
