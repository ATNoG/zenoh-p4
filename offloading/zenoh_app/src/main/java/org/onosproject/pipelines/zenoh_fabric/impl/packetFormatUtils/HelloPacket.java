package org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils;

import org.onlab.packet.IPacket;

import org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils.Types;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class HelloPacket extends Packet{

    private int packetType = Types.HELLO;
    private Header header;
    private byte[] payload;

    public HelloPacket(){};


    public HelloPacket(boolean l, boolean w, boolean i){
        super();
        this.header = new Header(l, w, i, this.packetType);
    };



    public void setPayloadBytes(byte[] payload){

        if(header.isFlag1()){
            this.payload= payload;
        }

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
