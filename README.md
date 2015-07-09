# croak
A remote host health monitoring and reporting application

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

## Scratchpad

Because this is still under development, here are some ways to
call the functions.

```
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

```