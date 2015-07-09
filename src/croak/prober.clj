(ns croak.prober
  (:require [croak.core :as core]
            [croak.time :refer [wait-until next-aligned-time]]
            [clj-time.core :as time]

            [croak.probes.iptables :refer [iptables]]))

(defn prober
  "prober main loop"
  [config]
  (let [step (-> config :delay)
        align-times (-> config :align-times)
        next-tick (if align-times
                    (next-aligned-time step)
                    (time/now))]
    (when align-times (wait-until next-tick))

    (loop [t next-tick]
      (let [n (->> step
                   time/millis
                   (time/plus t))]

        (when (-> config :debug)
          (println "probe @" (str (time/now))))

        (swap! core/=data= assoc t
               (let [data (iptables)]
                 {:INPUT (-> data :INPUT :bytes)
                  :OUTPUT (-> data :OUTPUT :bytes)}))

        (wait-until n)
        (recur n)))))


(comment

  (def f (future (prober
                  {:delay 500
                   :align-times true
                   :debug true}
                  )))

  (future-cancel f)

)