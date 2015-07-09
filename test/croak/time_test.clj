(ns croak.time-test
  (:require [clojure.test :refer :all]
            [croak.time :refer :all]
            [clj-time.coerce :as coerce]
            [clj-time.core :as time]))

(deftest a-test
  (testing "next-aligned-time aligns a time and delay correctly"
    (is
     ;; round to 500ms
     (=
      (next-aligned-time (time/date-time 1975 11 20 6 30 30 456) 500)
      (time/date-time 1975 11 20 6 30 30 500))

     ;; right on an alignment returns same time
     (=
      (next-aligned-time (time/date-time 1975 11 20 6 30 30 500) 500)
      (time/date-time 1975 11 20 6 30 30 500)))))
