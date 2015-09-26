(ns croak.reporter
  (:require [org.httpkit.client :as http]
            [croak.storage :as storage]
            [croak.prober :as prober]
            [clojure.data.json :as json]
            [clojure.java.io :as io])
  (:import [java.net InetAddress]))

(def ^:dynamic *base-opts*
  {
   :timeout 4000
   :user-agent "Croak/0.1"})

(defn send-data [data opts hostname]
  (let [d (assoc (into *base-opts* opts)
                 :form-params
                 {
                  :host hostname
                  :data (json/write-str data)}
                 )
        {:keys [status headers body error] :as resp}
        @(http/request d)]
    (when error
      (println "Reporter send-data failed: " (str error)))
    status))

(defn upload-file [filename opts hostname]
  (if (= 200 (-> filename
                 storage/load-edn
                 (send-data opts hostname)))
    (do
      (io/delete-file (str "/tmp/storage/" filename))
      true)
    false))

(defn upload-data [data opts hostname]
  (when (= 200 (-> data
                   storage/data->disk
                   (send-data opts hostname)))
    (swap! prober/=data=
           (fn [d]
             (apply dissoc d (keys data))))))

(defn reporter
  "reporter mainline function"
  [{:keys [minimum-set minimum-send
           fail-delay success-delay
           hostname opts]
    :or {minimum-set 3
         minimum-send 10
         fail-delay 7000
         success-delay 10000
         hostname (.getHostName (InetAddress/getLocalHost))
         opts {:method :post}
         }
    :as config}]
  ;; upload all files, oldest to newest
  (loop [remaining (sort (storage/get-filenames))]
    ;(wait-until-up "localhost.localdomain")

    ;; upload any files first
    (if-let [time-file (first remaining)]
      (if (upload-file (second time-file) opts hostname)
        ;; upload success
        (when (seq (rest remaining))
          (recur (rest remaining)))

        ;; upload again
        (do (Thread/sleep fail-delay)
            (recur remaining)))))

  ;; if no more files, try uploading memory
  (let [data @prober/=data=
        num (count data)]
    (when (>= num (+ minimum-send minimum-set))
      ;; upload all leave minimum-set behind
      (let [report-count (- num minimum-set)
            timestamps (->> data keys sort (take report-count))
            to-report (into {} (for [t timestamps] [t (data t)]))]
        (upload-data to-report opts hostname))))

  (Thread/sleep success-delay)
  (recur config))
