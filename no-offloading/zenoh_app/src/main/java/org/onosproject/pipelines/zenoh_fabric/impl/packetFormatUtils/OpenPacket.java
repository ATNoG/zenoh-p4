package org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils;

import org.onlab.packet.IPacket;

import org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils.Types;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class OpenPacket extends Packet{

    private int packetType = Types.OPEN;
    private Header header;
    private byte[] payload;

    public OpenPacket(){};


    public OpenPacket(boolean t, boolean a){
        super();
        this.header = new Header(false, t, a, this.packetType);
    };



    public void setPayloadBytes(byte[] payload){
        this.payload= payload;
    }

    @Override
    public int getPacketType() {
        return this.packetType;
    }

    public void setPacketType(int packetType) {
        this.packetType = packetType;
    }

    public Header getHeader() {
        return this.header;
    }



    @Override
    public byte[] serialize(){


        int fal = this.header.toBytes().length;        //determines length of firstArray
        int sal = this.payload.length;   //determines length of secondArray
        byte[] result = new byte[fal + sal];  //resultant array of size first array and second array
        System.arraycopy(this.header.toBytes(), 0, result, 0, fal);
        System.arraycopy(this.payload, 0, result, fal, sal);

        return result;
    }






}
