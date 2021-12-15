# zenoh-p4
P4 implementation of the Zenoh protocol

## Repository Architecture
The repo is divided into 3 folders, each of each with a specific scenario

### Scenario Architecture

```/p4``` contains all the code related to p4 pipeline

```/ptf``` contains all the code related to p4 pipeline validation

```/mininet``` contains all the code related to mininet init script, whereas ```/mininet/topology``` contains some examples of topologies that can be used in ```/mininet/basic.py```

```/zenoh_app``` contains all the ONOS app code to manage each of the scenarios

```/config``` contains some examples of topologies to be pushed to ONOS


### Makefile

```make start```  - to start the containers

```make prepare``` - to compile and pack app

```make app-install``` - to install app in ONOS

```make push-netconf-fullCase``` - to install a specific configuration to ONOS

```make p4_compile``` - to compile the p4 pipeline

```make p4-test``` - to test the p4 pipeline with PTF

Traditional sequence is ```make start```, wait for onos and mininet to fully start, then ```make app-install```, wait for app to be installed and running in the controller and then ```make push-netconf-fullCase```



