package org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils;

import java.util.Objects;
public class Declaration{


    // | flag3 flag2 flag1 |
    private boolean flag1;
    private boolean flag2;
    private boolean flag3;

    private ResKey reskey;


    public Declaration() {
    }


    public Declaration(boolean flag3, boolean flag2, boolean flag1){
        this.flag1 = flag1;
        this.flag2 = flag2;
        this.flag3 = flag3;
    }

    public int getType(){
        return 0;
    }

    public void setResourceID(int resourceID) {
    }

    public int getResourceID() {
        return 0;
    }

    public boolean getFlag1() {
        return this.flag1;
    }

    public void setFlag1(boolean flag1) {
        this.flag1 = flag1;
    }

    public boolean getFlag2() {
        return this.flag2;
    }

    public void setFlag2(boolean flag2) {
        this.flag2 = flag2;
    }

    public boolean getFlag3() {
        return this.flag3;
    }

    public void setFlag3(boolean flag3) {
        this.flag3 = flag3;
    }

    public ResKey getReskey() {
        return this.reskey;
    }

    public void setReskey(ResKey reskey) {
        this.reskey = reskey;
    }



    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Declaration)) {
            return false;
        }
        Declaration declaration = (Declaration) o;
        return flag1 == declaration.flag1 && flag2 == declaration.flag2 && flag3 == declaration.flag3 && reskey.equals(declaration.reskey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flag1, flag2, flag3, reskey);
    }




    @Override
    public String toString() {
        return "{" +
            " flag1='" + getFlag1() + "'" +
            ", flag2='" + getFlag2() + "'" +
            ", flag3='" + getFlag3() + "'" +
            ", reskey='" + getReskey().toString() + "'" +
            "}";
    }





    public byte[] serialize(){
        byte[] declare_type = new byte[1];

        declare_type[0] = (byte) Types.DECLARE;

        return declare_type;

    }




}
