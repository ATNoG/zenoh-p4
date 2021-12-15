package org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils;

import java.util.Objects;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import org.onosproject.net.DeviceId;
import org.onlab.packet.IPv4;

import java.util.concurrent.ThreadLocalRandom;


public class ZenohRouter{


    private byte[] peerId;
    private List<Byte> serial_number;
    private DeviceId deviceId;


    public ZenohRouter(DeviceId deviceId) {
        this.deviceId = deviceId;
    }



    public ZenohRouter(byte[] peerId, DeviceId deviceId) {
        this.peerId = peerId;
        this.deviceId = deviceId;
    }


    public byte[] getPeerId() {
        return this.peerId;
    }

    public void setPeerId(byte[] peerId) {
        this.peerId = peerId;
    }


    public void updateSN(){
        for(int i=0;i < this.serial_number.size(); i++){
            if(this.serial_number.get(i).byteValue() == (byte) 0xFF){
                this.serial_number.set(i, (byte) 0x80);
                if(i != this.serial_number.size() -1 ){
                    this.serial_number.set(i+1, (byte) 0x80);
                }else{
                    this.serial_number.add((byte) 0x80);
                }
                
            }else if(i == this.serial_number.size() -1){
                if(( (byte) this.serial_number.get(i).byteValue() + (byte) 0x01)  > (byte) 0x7F){
                    this.serial_number.set(i, (byte) 0x80);
                    this.serial_number.add((byte) 0x00);
                }
            
            }else{
                this.serial_number.set(i, (byte) ( this.serial_number.get(i).byteValue() + 0x01 ) );
                break;
            }
            
        
        }
    }

    public byte[] getSerial_number() {
        byte[] sn = new byte[this.serial_number.size()];
        
        for(int i=0; i < sn.length ; i++){
            sn[i] = this.serial_number.get(i).byteValue();
        }
        
        return sn;
    }

    public void setSerial_number() {
        createInitialSN();
    }

    public DeviceId getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(DeviceId deviceId) {
        this.deviceId = deviceId;
    }


    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof ZenohRouter)) {
            return false;
        }
        ZenohRouter zenohRouter = (ZenohRouter) o;
        return Objects.equals(deviceId, zenohRouter.deviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId);
    }


    private void createInitialSN(){
        //zenoh ints must start with a 1 and terminate with a 0 on the MSB of the int bytes

        
        byte[] client_sn_tmp = new byte[4];
        client_sn_tmp[0] = (byte) ThreadLocalRandom.current().nextInt(0x080, 0x0FF);
        client_sn_tmp[1] = (byte) ThreadLocalRandom.current().nextInt(0x080, 0x0FF);
        client_sn_tmp[2] = (byte) ThreadLocalRandom.current().nextInt(0x080, 0x0FF);

        //termination
        client_sn_tmp[3] = (byte) ThreadLocalRandom.current().nextInt(0x00, 0x7F);

        
        
        
        this.serial_number = new ArrayList<>();
        this.serial_number.add(client_sn_tmp[0]);
        this.serial_number.add(client_sn_tmp[1]);
        this.serial_number.add(client_sn_tmp[2]);
        this.serial_number.add(client_sn_tmp[3]);






    }


    @Override
    public String toString() {
        return "{" +
            ", serial_number='" + Arrays.toString(getSerial_number()) + "'" +
            ", deviceId='" + getDeviceId() + "'" +
            "}";
    }


}
