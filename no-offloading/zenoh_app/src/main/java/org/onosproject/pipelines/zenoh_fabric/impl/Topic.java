package org.onosproject.pipelines.zenoh_fabric.impl;

import org.onosproject.net.DeviceId;


import java.util.List;
import java.util.ArrayList;
import org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils.*;


public class Topic{

    private List<ZenohHost> list_ZH;
    private List<DeviceId> list_devices;
    private String topic;

    public Topic(String topic) {
        this.topic = topic;
        this.list_ZH = new ArrayList<>();
        this.list_devices = new ArrayList<>();
    }

    public List<ZenohHost> getList_ZH() {
        return this.list_ZH;
    }

    public void setList_ZH(List<ZenohHost> list_ZH) {
        this.list_ZH = list_ZH;
    }


    public void addHost(ZenohHost zh){
        this.list_ZH.add(zh);
    }

    public List<DeviceId> getList_devices() {
        return this.list_devices;
    }

    public void setList_devices(List<DeviceId> list_devices) {
        this.list_devices = list_devices;
    }

    public boolean addDevice(DeviceId devId){
        this.list_devices.add(devId);

        return list_devices.contains(devId);
    }

    public String getTopic(){
        return this.topic;
    }



}
