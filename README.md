# croak
A remote host health monitoring and reporting application

## Overview

This application aims to do one thing, and do it well. And that is to
gather system information at regular intervals reliably, and report
that to a remote system. It is designed to handle network outages
gracefully and to not loose data. It is not a server, it doesn't make
graphs and it doesn't send alerts. Instead it is intended to work with
other systrems that do these things.