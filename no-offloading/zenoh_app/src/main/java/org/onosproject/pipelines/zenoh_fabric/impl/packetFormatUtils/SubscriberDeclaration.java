package org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils;

import java.util.Objects;


public class SubscriberDeclaration extends Declaration{



    private int type = Types.SUBSCRIBER;

    public SubscriberDeclaration() {
        super(true, false, true);
    }


    public int getType() {
        return this.type;
    }


    @Override
    public byte[] serialize(){

        byte[] declaration;
        byte[] rk = super.getReskey().serialize();

        declaration = new byte[rk.length +3];

        declaration[0] = (byte) super.serialize()[0];
        //number of declarations
        declaration[1] = (byte) 1;
        //declaration type
        declaration[2] =  (byte)(((super.getFlag3()) ? (byte) 0x80 : (byte) 0x00) |  ((super.getFlag2()) ? (byte) 0x40 : (byte) 0x00) |
        ((super.getFlag1()) ? (byte) 0x20 : (byte) 0x00) | (byte) this.type);

        System.arraycopy(rk, 0, declaration, 3, rk.length);

        return declaration;
    }

}



