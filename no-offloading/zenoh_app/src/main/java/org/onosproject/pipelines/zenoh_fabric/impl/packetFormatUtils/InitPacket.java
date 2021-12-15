package org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils;

import org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils.Types;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class InitPacket extends Packet{

    private int packetType = Types.INIT;
    private Header header;
    private byte[] payload;

    public InitPacket(){};


    public InitPacket(boolean s, boolean a){
        super();
        this.header = new Header(false, s, a, this.packetType);
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
