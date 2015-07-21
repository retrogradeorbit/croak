(ns croak.prober
  (:require [croak.time :refer [wait-until next-aligned-time]]
            [clj-time.core :as time]

            [croak.probes.iptables :refer [iptables]]
            [croak.probes.meminfo :refer [meminfo]]))

;; the ram storage of the probers results
(def =data= (atom {}))

(def probes
  {:iptables
   (fn [] (let [data (iptables)]
            {:INPUT (-> data :INPUT :bytes)
             :OUTPUT (-> data :OUTPUT :bytes)}))
   :meminfo
   meminfo
   })

(defn prober
  "prober main loop"
  [{:keys [delay align-times probe debug]
    :as config}]
  (let [   next-tick (if align-times
                       (next-aligned-time delay)
                       (time/now))]
    (when align-times (wait-until next-tick))

    (loop [t next-tick]
      (let [n (->> delay
                   time/millis
                   (time/plus t))]

        (when debug
          (println "probe" probe ":" (str t)))

        (swap! =data=
               (fn [data]
                 (assoc data t
                        (assoc
                         (get data t {})
                         probe ((probe probes))))))

        (wait-until n)
        (recur n)))))
