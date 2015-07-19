{
 :probes
 [
  {:probe :iptables
   :delay 1000
   :align-times true
   :debug true}

  {:probe :meminfo
   :delay 20000
   :align-times true
   :debug true}
  ]

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
   :url "http://localhost:5000/data"
   }

  }
}
