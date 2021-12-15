package org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils;

import org.onlab.packet.IPacket;

public class Packet implements IPacket{

    private byte[] payload;

    public Packet(){};


    /*public Packet(Header header, byte[] payload){
        this.header = header;
        this.payload = payload;
    };
    */


    public byte[] getPayloadBytes() {
        return this.payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    @Override
    public IPacket getParent(){
        return null;
    }

    @Override
    public IPacket getPayload(){
        return null;
    }

    @Override
    public void resetChecksum(){
    }


    @Override
    public IPacket setParent​(IPacket packet){
        return null;
    }


    @Override
    public IPacket setPayload​(IPacket packet){
        return null;
    }

    @Override
    public byte[] serialize(){
        return this.payload;
    }

    public int getPacketType(){
        return 0;
    }




}
