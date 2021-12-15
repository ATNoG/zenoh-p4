package org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils;

import org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils.Types;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class ClosePacket extends Packet{

    private int packetType = Types.CLOSE;
    private Header header;
    private byte[] payload;

    public ClosePacket(){};


    public ClosePacket(boolean k, boolean i){
        super();
        this.header = new Header(false, k, i, this.packetType);
    };


    public byte[] getPeerId(){
        if(this.header.getFlag2()){
            int peerIdLen = this.payload[0];
            byte[] peerId = Arrays.copyOfRange(this.payload, 1, peerIdLen);

            return peerId;
        }

        return null;
    }

    public void setPayloadBytes(byte[] payload){
        this.payload= payload;
    }



    @Override
    public int getPacketType() {
        return this.packetType;
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
