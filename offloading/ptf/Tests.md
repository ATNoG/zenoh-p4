# Tests


## Zenoh Pipeline behaviour tests


### **Case 1**

#### Test storage of serial number from client in the ingress pipeline


### **Case 2**

#### Test storage of serial number from 2 clients in the ingress pipeline


### **Case 3**

#### Test storage of serial number from controller to client in the egress pipeline


### **Case 4**

#### Test storage of serial number from controller to 2 clients in the egress pipeline


### **Case 5**

#### Test the serial number verification match in the ingress pipeline


### **Case 6**

#### Test the serial number increment in the egress pipeline


### **Case 7**

#### Test the behaviour of the dataplane on a data packet arrival after session establishment

###### **Steps**

1. Session establishment with subscriber
2. Subscriber sends declaration packets
3. Session establishment with publisher
4. Controller sends declarations to publisher
5. Publisher sends a single data packet to dataplane
6. Dataplane receives, processes and forwards data packet to desired destination



### **Case 8**

#### Test the behaviour of the dataplane on multiple data packets arrival after session establishment, needed to check how egress pipeline behaves

    used to check if egress pipeline registers are being updated

###### **Steps**

1. Session establishment with subscriber
2. Subscriber sends declaration packets
3. Session establishment with publisher
4. Controller sends declarations to publisher
5. Publisher sends multiple data packets to dataplane
6. Dataplane receive, process and forward data packets to destination


### **Case 9**

#### Test byte overflow on registers

ex: sn \xff\xff\x80\7f

###### **Steps**

1. Session establishment with subscriber
2. Subscriber sends declaration packets
3. Session establishment with publisher
4. Controller sends declarations to publisher
5. Publisher sends multiple data packets to dataplane
6. Dataplane receive, process and forward data packets to destination
