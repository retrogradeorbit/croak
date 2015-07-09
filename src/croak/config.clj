(ns croak.config)

(def config
  {:prober
   {
    ;; job name
    :firewall
    {
     ;; how often to probe
     :delay 500

     ;; if true, always start with a small initial delay so
     ;; all the time stamps from different runs line up on
     ;; exact intervals
     :align-times true

     ;; use sudo for root access
     :use [:sudo]

     ;; the probe module
     :module :iptables

     ;; a filter function to pre-process the data
     ;;:filter [[:INPUT :bytes] [:OUTPUT :bytes]]
     }
    }

   :archiver
   {
    ;; run every 60 seconds
    :delay 60000
    :align-times true

    :archive-directory "/var/cache/croak"

    ;; size that triggers the archiver
    :trigger 5000
    }})
