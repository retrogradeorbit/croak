(ns croak.time
  (:require [clj-time.core :as time]
            [clj-time.format :as format]
            [clj-time.coerce :as coerce]))

(defn sleep
  [delay]
  (Thread/sleep delay))

(defn millis-until
  "Number of milliseconds until a particular datetime arrives"
  [^org.joda.time.DateTime t]
  (let [start (time/now)]
    (-> start
        (time/interval t)
        time/in-millis)))

(defn wait-for
  "Sleep the present thread until the passed in date time arrives"
  [^org.joda.time.DateTime t]
  (-> t millis-until sleep))

(defn next-aligned-time
  "given a delay in milliseconds, find out the exact time the next
  aligned segment is on"
  ([delay]
   (next-aligned-time (time/now) delay))
  ([t delay]
   (let [lt (coerce/to-long t)]
     (if (= 0 (rem lt delay))
       t
       (-> lt
           (quot delay)
           (* delay)
           (+ delay)
           coerce/from-long)))))
