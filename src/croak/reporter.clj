(ns croak.reporter
  (:require [org.httpkit.client :as http]
            [croak.storage :as storage])
)

(def ^:dynamic *base-opts*
  {
   :timeout 200
   :user-agent "Croak-1"})

(defn reporter
  "reporter mainline function"
  [config]
  (loop []
    ;(wait-until-up "localhost.localdomain")

    ;; upload any files first
    (let [[time filename] (first (sort (storage/get-filenames)))]
      (println "=====" filename)
      (println (storage/load-file filename)))



    ;; machine
    #_ (http/request (assoc (into *base-opts* request-opts)
                         :query-params ["data" "..."]))
    )

  )
