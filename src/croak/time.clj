(ns croak.time
  (:require [clj-time.core :as time]
            [clj-time.format :as format]
            [clj-time.coerce :as coerce]))

(defn wait
  "Sleep the present thread until the passed in date time arrives"
  [^org.joda.time.DateTime t]
  (let [start (time/now)
        delay (-> start
                  (time/interval t)
                  time/in-millis)]
    (Thread/sleep delay)))

(defn next-aligned-time
  "given a delay in milliseconds, find out the exact time the next
  aligned segment is on"
  ([delay]
   (next-aligned-time (time/now) delay))
  ([t delay]
   (-> t
       coerce/to-long
       (quot delay)
       (* delay)
       (+ delay)
       coerce/from-long)))
