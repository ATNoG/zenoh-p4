package org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils;


import java.util.Objects;
import java.lang.Byte;
public class Header{

    private int msgId;

    private boolean flag1;
    private boolean flag2;
    private boolean flag3;


    public Header(byte headerBytes){
        int flags = (headerBytes >> 5);
        String flag_representation = Integer.toBinaryString(flags & 0xFF);
        
         while(flag_representation.length() < 3){
                flag_representation = "0" + flag_representation;
        }
        
        this.flag1 = flag_representation.charAt(0) == '1';
        this.flag2 = flag_representation.charAt(1) == '1';
        this.flag3 = flag_representation.charAt(2) == '1';
        
        
        
        this.msgId = headerBytes & 0x1f;
    }
    
    public Header(boolean flag3, boolean flag2, boolean flag1, int msgId){
            this.flag1 = flag1;
            this.flag2 = flag2;
            this.flag3 = flag3;
            this.msgId = msgId;
    }


    public int getMsgId() {
        return this.msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public boolean isFlag1() {
        return this.flag1;
    }

    public boolean getFlag1() {
        return this.flag1;
    }

    public void setFlag1(boolean flag1) {
        this.flag1 = flag1;
    }

    public boolean isFlag2() {
        return this.flag2;
    }

    public boolean getFlag2() {
        return this.flag2;
    }

    public void setFlag2(boolean flag2) {
        this.flag2 = flag2;
    }

    public boolean isFlag3() {
        return this.flag3;
    }

    public boolean getFlag3() {
        return this.flag3;
    }

    public void setFlag3(boolean flag3) {
        this.flag3 = flag3;
    }


    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Header)) {
            return false;
        }
        Header header = (Header) o;
        return msgId == header.msgId && flag1 == header.flag1 && flag2 == header.flag2 && flag3 == header.flag3;
    }

    @Override
    public int hashCode() {
        return Objects.hash(msgId, flag1, flag2, flag3);
    }


    @Override
    public String toString() {
        return "{" +
            " msgId='" + getMsgId() + "'" +
            ", flag1='" + isFlag1() + "'" +
            ", flag2='" + isFlag2() + "'" +
            ", flag3='" + isFlag3() + "'" +
            "}";
    }


    public byte[] toBytes(){
        byte[] header = new byte[1];
        String id_representation = Integer.toBinaryString(this.msgId & 0xFF);
        
        while(id_representation.length() < 5){
                id_representation = "0" + id_representation;
        }
        
    

        header[0] = Byte.parseByte((this.flag3 ? "1" : "0") + (this.flag2 ? "1" : "0") + (this.flag1 ? "1" : "0") + id_representation, 2);

        return header;
    }
}
