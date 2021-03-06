# croak
A remote host health monitoring and reporting application.

## Overview

This application aims to do one thing, and do it well. And that is to
gather system information at regular intervals reliably, and report
that to a remote system. It is designed to handle network outages
gracefully and to not loose data. It is not a server, it doesn't make
graphs and it doesn't send alerts. Instead it is intended to work with
other systems that do these things.

Croak has three components. The prober, the archiver and the reporter.
The prober, at various intervals, collects various system metrics by
running various programmes, does some optional processing on them,
and stores that information in memory.

The archiver watches that ram storage, and when it gets above a
definable amount, it archives that information to storage on disk.
It does this repeatedly, paging memory out to successive files.

The reporter tries to take this stored data on disk and in ram and
tries to verifiably report this information to some remote system
using some method, like an HTTP POST, or a series of UDP packets. It
may use multiple methods and multiple hosts. When the information
has been reliably transfered to all recipients, it is removed from
disk/memory.

Croak is written in clojure and can be used as a stand-alone
application or as a library in another clojure programme.

## Running

When running under leiningen use the trampoline.

```bash
$ lein trampoline run
```

### Config

The configuration file presently looks like this:

```clojure
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
```

## Scratchpad

Because this is still under development, here are some ways to
call the functions.

```clojure

croak.probes.iptables> (clojure.pprint/pprint (iptables))
{:INPUT
 {:policy :ACCEPT,
  :pkts 844,
  :bytes 217134,
  :rules
  ( {:pkts 0,
    :extra "tcp dpt:53",
    :source "0.0.0.0/0",
    :out "*",
    :prot "tcp",
    :bytes 0,
    :target :ACCEPT,
    :dest "0.0.0.0/0",
    :in "lxcbr0",
    :opt "--"}
   ...
nil

croak.probes.iptables> ((juxt :bytes :pkts) (-> (iptables) :INPUT))
[448731 1414]

croak.prober> (def f (future (prober
                  {:delay 500
                   :align-times true
                   :debug true}
                  )))
#'croak.prober/f
probe @ 2015-07-09T14:51:14.000Z
probe @ 2015-07-09T14:51:14.500Z
probe @ 2015-07-09T14:51:15.000Z
probe @ 2015-07-09T14:51:15.500Z
probe @ 2015-07-09T14:51:16.000Z
probe @ 2015-07-09T14:51:16.500Z
croak.prober> (future-cancel f)
true

croak.prober> (ns croak.core)
nil
croak.core> (count @=data=)
6

```

## License

Copyright (c) 2015 Crispin Wellington

Distributed under the Eclipse Public License version 1.0