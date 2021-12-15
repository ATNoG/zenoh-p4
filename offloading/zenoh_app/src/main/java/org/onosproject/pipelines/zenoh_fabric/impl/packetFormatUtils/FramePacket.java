package org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils;

import org.onlab.packet.IPacket;

import org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils.Types;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class FramePacket extends Packet{

    private int packetType = Types.FRAME;
    private Header header;
    private byte[] payload;

    public FramePacket(){};


    public FramePacket(boolean e, boolean f, boolean r){
        super();
        this.header = new Header(e, f, r, this.packetType);
    };



    public void setPayloadBytes(byte[] payload){

        this.payload= payload;

    }


    @Override
    public int getPacketType() {
        return this.packetType;
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
