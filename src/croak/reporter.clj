(ns croak.reporter
  (:require [org.httpkit.client :as http]
            [croak.storage :as storage])
)

(def ^:dynamic *base-opts*
  {
   :timeout 4000
   :user-agent "Croak/0.1"})

(defn send-data [data opts]
  (let [d (assoc (into *base-opts* opts)
                 :query-params {:data (prn-str data)}
                 )]
    (http/request d)))

(defn reporter
  "reporter mainline function"
  [config]
  (loop []
    ;(wait-until-up "localhost.localdomain")

    ;; upload any files first
    (let [[time filename] (first (sort (storage/get-filenames)))]
      (println @(-> filename
                     storage/load-edn
                     (send-data {:url "http://localhost"
                                 :method :post}))))))
