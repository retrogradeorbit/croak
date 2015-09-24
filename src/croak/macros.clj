(ns croak.macros
  (:require [croak.time :refer [wait-until next-aligned-time]]
            [clj-time.core :as time]))

(defmacro defloop [name data-atom func & body]
  `(defn ~name
     [{:keys [delay align-times probe debug]
       :as config}]
     (let [next-tick (if align-times
                       (next-aligned-time delay)
                       (time/now))]
       (when align-times (wait-until next-tick))

       (loop [t next-tick]
         (let [n (->> delay
                      time/millis
                      (time/plus t))]

           (when debug
             (println ~(str name) probe ":" (str t)))

           (swap! ~data-atom
                  (fn [data]
                    (assoc data t
                           (~func
                            (get data t {})
                            ~@body))))

           (wait-until n)
           (recur n))))))
