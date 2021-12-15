package org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils;

import org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils.Types;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class KeepAlivePacket extends Packet{

    private int packetType = Types.KEEPALIVE;

    public KeepAlivePacket(){
        super();
    };


    @Override
    public byte[] serialize(){
        byte[] ka = new byte[1];

        ka[0] = (byte) packetType;

        return ka;
    }






}
