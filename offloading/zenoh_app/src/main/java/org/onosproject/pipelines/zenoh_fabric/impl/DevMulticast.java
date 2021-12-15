package org.onosproject.pipelines.zenoh_fabric.impl;

import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;

import java.util.Objects;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils.*;


public class DevMulticast{

    // Key = multicast group, value = physical ports associated with that multicast group
    private Map<Integer,List<PortNumber>> multicastGroupsInfo;


    public DevMulticast() {
        multicastGroupsInfo = new HashMap<>();
    }

    public DevMulticast(Map<Integer,List<PortNumber>> multicastGroupsInfo) {
        this.multicastGroupsInfo = multicastGroupsInfo;
    }

    public Map<Integer,List<PortNumber>> getMulticastGroupsInfo() {
        return this.multicastGroupsInfo;
    }

    public void setMulticastGroupsInfo(Map<Integer,List<PortNumber>> multicastGroupsInfo) {
        this.multicastGroupsInfo = multicastGroupsInfo;
    }

    public DevMulticast multicastGroupsInfo(Map<Integer,List<PortNumber>> multicastGroupsInfo) {
        setMulticastGroupsInfo(multicastGroupsInfo);
        return this;
    }

    public boolean addPort(int mGroup, int portNum){
        if(this.multicastGroupsInfo.containsKey(mGroup)){

            this.multicastGroupsInfo.get(mGroup).add( PortNumber.portNumber(portNum) );

        }else{

            List<PortNumber> listPorts = new ArrayList<>();
            listPorts.add( PortNumber.portNumber(portNum) );
            this.multicastGroupsInfo.put(mGroup, listPorts);

        }

        return this.multicastGroupsInfo.get(mGroup).contains(PortNumber.portNumber(portNum));
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof DevMulticast)) {
            return false;
        }
        DevMulticast devMulticast = (DevMulticast) o;
        return Objects.equals(multicastGroupsInfo, devMulticast.multicastGroupsInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(multicastGroupsInfo);
    }

    @Override
    public String toString() {
        return "{" +
            " multicastGroupsInfo='" + getMulticastGroupsInfo() + "'" +
            "}";
    }



}
