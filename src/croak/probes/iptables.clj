(ns croak.probes.iptables
  (:require [clojure.java.shell :as shell]
            [clojure.string :as string]))

;; simple parsers
(defn- parse-packets [s] (Integer/parseInt s))
(defn- parse-bytes [s] (Integer/parseInt s))
(defn- parse-int [s] (Integer/parseInt s))


(defn- section-name
  "given a chunk of text that is a section of iptables firewall, return the
  iptables chain it belongs to. For example, feeding in

Chain POSTROUTING (policy ACCEPT 103K packets, 10M bytes)
 pkts bytes target     prot opt in     out     source               destination
   41  3119 MASQUERADE  all  --  *      *       10.0.3.0/24         !10.0.3.0/24

  returns \"POSTROUTING\"
  "
  [section]
  {:pre [(string? section)]
   :post [(#{"INPUT" "OUTPUT" "FORWARD" "PREROUTING" "POSTROUTING" "DOCKER"} %)]}
  (-> #"Chain (\w+)"
      (re-seq section)
      first second))


(defn- section-rules
  "given a chunk of section text, return a lazy sequence of rule maps."
  [section]
  {:pre [(string? section)]
   :post [(= (count %) (- (count (string/split section #"\n")) 2))]}
  (for [line (-> section
                 (string/split #"\n")
                 nnext)]
    (let [[_ pkts bytes target prot opt in out source dest extra]
          (first (re-seq #"^\s*(\d+)\s+(\d+)\s+(\w+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s+(\S+)\s*(.*)$" line))]
      {:pkts (parse-packets pkts)
       :bytes  (parse-bytes bytes)
       :target (keyword target)
       :prot prot
       :opt opt
       :in in
       :out out
       :source source
       :dest dest
       :extra (when (not= extra "") extra)})))


(defn- section-stats
  "given a chunk of section text, return the overall packet and bytes counts for the chain.
  returns a map like

  {
   :policy :ACCEPT
   :pkts 1234
   :bytes 786662
  }
  "
  [section]
  {:pre [(string? section)]}
  (if-let [overview (first (re-seq #"Chain (\w+) \(policy (\w+) (\d+) packets, (\d+) bytes\)" section))]
    (let [[_ _ policy pkts bytes] overview]
      {:policy (keyword policy)
       :pkts (parse-packets pkts)
       :bytes (parse-bytes bytes)})
    (if-let [overview (first (re-seq #"Chain (\w+) \((\d+) references\)" section))]
      (let [[_ _ refs] overview]
        {:refs (parse-int refs)}))))


(defn- iptables-sections
  "given the complete output of iptables, split it into sections,
  process each section, and return a map describing the all the chains,
  keyed by chain keyword
  "
  [output]
  {:pre [(string? output)]}
  (let [sections (string/split output #"\n\n")]
    (into {} (for [section sections]
               (let [name (section-name section)
                     rules (section-rules section)
                     stats (section-stats section)]
                 [(keyword name) (into stats {:rules rules})])))))


(defn iptables []
  (let [{:keys [exit out err]}
        (shell/sh "sudo" "-n"
                  "iptables" "-L" "-n" "-x" "-v")]
    (assert (not= exit 1) "'sudo iptables' exited non-zero. Have you setup sudo so this user can run iptables as root with no password?")
    (assert (= exit 0)
            (str
             "'sudo iptables' exited non-zero. Looks like the sudo part worked and the iptables failed."
             " Exit: " exit
             " Out: " out
             " Err: " err
           ))
    (iptables-sections out)))
