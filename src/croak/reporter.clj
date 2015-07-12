(ns croak.reporter
  (:require [org.httpkit.client :as http])
)

(def ^:dynamic *base-opts*
  {
   :timeout 200
   :user-agent "Croak-1"})

(defn reporter
  "reporter mainline function"
  [config]
  (loop []
    (wait-until-up "localhost.localdomain")

    ;; machine
    (http/request (assoc (into *base-opts* request-opts)
                         :query-params ["data" "..."]))
    )

  )
