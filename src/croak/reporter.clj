(ns croak.reporter
  (:require [org.httpkit.client :as http]
            [croak.storage :as storage]
            [clojure.data.json :as json])
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
    (if error
      (println "Failed, exception: " error)
      (println "HTTP success: " status))))

(defn reporter
  "reporter mainline function"
  [config]
  (loop []
    ;(wait-until-up "localhost.localdomain")

    ;; upload any files first
    (let [[time filename] (first (sort (storage/get-filenames)))]
      (println "loading" filename)
      (-> filename
           storage/load-edn
           (send-data {:url "http://localhost:5000/data"
                       :method :post})))))
