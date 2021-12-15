package org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils;

import static org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils.Types.*;

import java.util.Objects;


public class ResourceDeclaration extends Declaration{

    private int type = Types.RESOURCE;
    private int resourceID;

    public ResourceDeclaration(){
        super(false, false, false);
    };


    public int getType() {
        return this.type;
    }

    @Override
    public int getResourceID() {
        return this.resourceID;
    }

    @Override
    public void setResourceID(int resourceID) {
        this.resourceID = resourceID;
    }


    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof ResourceDeclaration)) {
            return false;
        }
        ResourceDeclaration resourceDeclaration = (ResourceDeclaration) o;
        return type == resourceDeclaration.type && resourceID == resourceDeclaration.resourceID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, resourceID);
    }



    @Override
    public byte[] serialize(){

        byte[] declaration;
        byte[] rk = super.getReskey().serialize();

        declaration = new byte[rk.length +4];

        declaration[0] = super.serialize()[0];
        //number of declarations
        declaration[1] = (byte) 1;
        //declaration type
        declaration[2] = (byte)(((super.getFlag3()) ? (byte) 0x80 : (byte) 0x00) |  ((super.getFlag2()) ? (byte) 0x40 : (byte) 0x00) |
        ((super.getFlag1()) ? (byte) 0x20 : (byte) 0x00) | (byte) this.type);
        //resource id
        declaration[3] = (byte) this.resourceID;

        System.arraycopy(rk, 0, declaration, 4, rk.length);

        return declaration;
    }
}



