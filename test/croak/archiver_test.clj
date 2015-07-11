(ns croak.archiver-test
  (:require [clojure.test :refer :all]
            [croak.archiver :refer :all]
            [clj-time.core :as time]))

(deftest test-make-filename
  (testing "make-filename creates a correctly formatted filename"
    (binding [croak.archiver/*image-storage-path* "/storage/"
              croak.archiver/*time-format* "yyyy-MM-dd-HH:mm:ss.SSS"]
      (is
       (=
        (str (make-filename (time/date-time 1975 11 20 6 30 30 0)))
        "/storage/data-1975-11-20-06:30:30.000.dat")))))
