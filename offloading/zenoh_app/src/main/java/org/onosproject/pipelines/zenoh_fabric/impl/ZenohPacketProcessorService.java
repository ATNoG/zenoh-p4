package org.onosproject.pipelines.zenoh_fabric.impl;

import org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils.*;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.packet.PacketContext;

import java.util.List;
import java.util.Map;

public interface ZenohPacketProcessorService{

    public void sendInitAck();

    public void sendOpenAck();

    public void sendKeepAlive();

    public void sendResourceDeclaration();

    public List<ZenohHost> getZenohHosts();
    
    public List<ZenohHost> getZenohHostsKeepAlive();

    public Map<DeviceId, DeviceConfig> getListDevices();

    public void sendPacketOut(PacketContext pc, PortNumber pn, DeviceId deviceId, byte[] packet);


}
