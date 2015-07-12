(ns croak.config)


(def default-config
  {
   :prober
   {
    ;; millisecs
    :delay 500

    ;; if true, always start with a small initial delay so
    ;; all the time stamps from different runs line up on
    ;; exact intervals
    :align-times true

    ;; print what you're doing
    :debug true
    }

   :archiver
   {
    ;; put the files here
    :storage "/tmp/storage"

    ;; how many records to store per file
    :count 1000

    ;; print what you're doing
    :debug true
    }

   :reporter
   {
    ;; http-kit options
    :opts
    {
     :method :post
     :url "http://localhost.localdomain:3128/machine-name/"
     :basic-auth ["user" "pass"]
     }

    ;; how to send the data
    :encoding :json
    }
   }
  )


(def config-paths
  ["~/.croak.clj"
   "~/.croak/config.clj"
   "/usr/local/etc/croak.clj"
   "/usr/local/etc/croak/cronfig.clj"])


(defn read-config
  []

  )
