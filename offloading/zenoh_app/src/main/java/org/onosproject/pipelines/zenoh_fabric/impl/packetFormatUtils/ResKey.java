package org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ResKey{

    private boolean k;
    private int resourceId;
    private String suffix;



    public ResKey() {
    }


    public boolean getK() {
        return this.k;
    }

    public void setK(boolean k) {
        this.k = k;
    }

    public int getResourceId() {
        return this.resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public String getSuffix() {
        return this.suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }


    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof ResKey)) {
            return false;
        }
        ResKey resKey = (ResKey) o;
        return k == resKey.k && resourceId == resKey.resourceId && suffix.equals(resKey.suffix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(k, resourceId, suffix);
    }


    @Override
    public String toString() {
        return "{" +
            " k='" + getK() + "'" +
            ", resourceId='" + getResourceId() + "'" +
            ", suffix='" + getSuffix() + "'" +
            "}";
    }

    public byte[] serialize(){

        byte[] reskey;
        if(this.k){
            reskey = new byte[1];
            reskey[0] = (byte) this.resourceId;
        }else{

            byte[] tmp = this.suffix.getBytes(StandardCharsets.UTF_8);
            reskey = new byte[tmp.length +2];
            reskey[0] = (byte) this.resourceId;
            reskey[1] = (byte) tmp.length;

            System.arraycopy(tmp, 0, reskey, 2, tmp.length);
        }

        return reskey;
    }



}
