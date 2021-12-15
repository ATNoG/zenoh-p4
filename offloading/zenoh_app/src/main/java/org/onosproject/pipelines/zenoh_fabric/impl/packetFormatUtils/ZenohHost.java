package org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils;

import java.util.Objects;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import org.onlab.packet.IPv4;

import java.util.concurrent.ThreadLocalRandom;


public class ZenohHost{


    private int type = Types.PUBLISHER;
    private byte[] peerId;
    private byte[] cookie;
    private List<Byte> serial_number;
    private List<Byte> router_serial_number_byte;
    private String deviceId;
    private int portNumber;
    private final int ipv4;
    private final int ipv4_router;
    private final byte[] mac_address;
    private final byte[] router_mac_address;
    private final int UdpPort;
    private boolean keepAlive = false;

    public ZenohHost(int ipv4, int ipv4_router, byte[] mac_address, byte[] router_mac, int UdpPort) {
        this.ipv4 =  ipv4;
        this.ipv4_router = ipv4_router;
        this.mac_address = mac_address;
        this.router_mac_address = router_mac;
        this.UdpPort = UdpPort;
        this.serial_number = new ArrayList<>();
    }

    public byte[] getPeerId() {
        return this.peerId;
    }

    public void setPeerId(byte[] peerId) {
        this.peerId = peerId;
    }

    public byte[] getCookie() {
        return this.cookie;
    }

    public void setCookie(byte[] cookie) {
        this.cookie = cookie;
    }

    public byte[] getSerial_number(){
        if(this.serial_number != null){
            byte[] sn = new byte[this.serial_number.size()];
            
            for(int i=0; i < sn.length ; i++){
                sn[i] = this.serial_number.get(i).byteValue();
            }
            
            return sn;
        }
        
        return null;
    }

    public void setSerial_number(byte[] serial_number) {
        this.serial_number.clear();
        for(byte b : serial_number){
            this.serial_number.add(b);
        }
        
    }


    public byte[] getRouter_serial_number() {
    
        byte[] sn = new byte[this.router_serial_number_byte.size()];
        
        for(int i=0; i < sn.length ; i++){
            sn[i] = this.router_serial_number_byte.get(i).byteValue();
        }
        
        return sn;
    }

    public void setRouter_serial_number() {
        this.createInitialSN();
    }

    public void incrementRouter_serial_number() {
        boolean updated = false;
        //iterate until not find 0x0FF and increment 1, if 0xFF put 0x80 and process next, else break
        for(int i=0;i < this.router_serial_number_byte.size(); i++){
            if(i == this.router_serial_number_byte.size() -1){
                if( ( (byte) this.router_serial_number_byte.get(i).byteValue() + (byte) 0x01 ) > (byte) 0x7F){
                    this.router_serial_number_byte.set(i, (byte) 0x80);
                    this.router_serial_number_byte.add((byte) 0x00);
                }
            }else if(this.router_serial_number_byte.get(i).byteValue() == (byte) 0xFF){
                this.router_serial_number_byte.set(i, (byte) 0x80);
                this.router_serial_number_byte.set(i+1, (byte) ( this.router_serial_number_byte.get(i+1).byteValue() + 0x01) );
                updated = true;
            }else{
                if(!updated){
                    this.router_serial_number_byte.set(i, (byte) (this.router_serial_number_byte.get(i).byteValue() + 0x01) );
                }
                break;
            }
            
        
        }
    }



    public void incrementSerial_number() {
        boolean updated = false;
        //iterate until not find 0x0FF and increment 1, if 0xFF put 0x80 and process next, else break
        for(int i=0;i < this.serial_number.size(); i++){
            if(i == this.serial_number.size() -1){
                if( ( (byte) this.serial_number.get(i).byteValue() + (byte) 0x01 ) > (byte) 0x7F){
                    this.serial_number.set(i, (byte) 0x80);
                    this.serial_number.add((byte) 0x00);
                }
            }else if(this.serial_number.get(i).byteValue() == (byte) 0xFF){
                this.serial_number.set(i, (byte) 0x80);
                this.serial_number.set(i+1, (byte) ( this.serial_number.get(i+1).byteValue() + 0x01) );
                updated = true;
            }else{
                if(!updated){
                    this.serial_number.set(i, (byte) (this.serial_number.get(i).byteValue() + 0x01) );
                }
                break;
            }
            
        
        }
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }


    public int getPortNumber() {
        return this.portNumber;
    }

    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }


    public int getIpv4() {
        return this.ipv4;
    }


    public int getIpv4_router() {
        return this.ipv4_router;
    }


    public byte[] getRouter_mac_address() {
        return this.router_mac_address;
    }


    public byte[] getMac_address() {
        return this.mac_address;
    }


    public int getUdpPort() {
        return this.UdpPort;
    }



    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }
    
    public boolean getKeepAlive() {
        return this.keepAlive;
    }

    public void setKeepAlive(boolean ka) {
        this.keepAlive = ka;
    }


    public ZenohHost copy(){
        ZenohHost zh =  new ZenohHost(this.ipv4, this.ipv4_router, this.mac_address, this.router_mac_address, this.UdpPort);
        zh.setCookie(this.cookie);
        zh.setPeerId(this.peerId);
        zh.setDeviceId(this.deviceId);
        zh.setPortNumber(this.portNumber);


        return zh;
    }


    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof ZenohHost)) {
            return false;
        }
        ZenohHost zenohHost = (ZenohHost) o;
        return Arrays.equals(peerId, zenohHost.peerId) && Arrays.equals(cookie, zenohHost.cookie) && serial_number == zenohHost.serial_number && deviceId.equals(zenohHost.deviceId);

    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(peerId), Arrays.hashCode(cookie), serial_number, deviceId);
    }



    public int hashCodeKey() {
        return Objects.hash(ipv4, UdpPort);
    }





    @Override
    public String toString() {
        return "{" +
            " peerId='" + Arrays.toString(getPeerId()) + "'" +
            ", cookie='" + Arrays.toString(getCookie()) + "'" +
            ", serial_number='" + this.serial_number==null ? "not set" : Arrays.toString(getSerial_number())  + "'" +
            ", deviceId='" + getDeviceId() + "'" +
            "}";
    }



    private void createInitialSN(){
        //zenoh ints must start with a 1 and terminate with a 0 on the MSB of the int bytes

        byte[] client_sn_tmp = new byte[4];
        client_sn_tmp[0] = (byte) ThreadLocalRandom.current().nextInt(0x080, 0x0FF);
        client_sn_tmp[1] = (byte) ThreadLocalRandom.current().nextInt(0x080, 0x0FF);
        client_sn_tmp[2] = (byte) ThreadLocalRandom.current().nextInt(0x080, 0x0FF);

        //termination
        client_sn_tmp[3] = (byte) ThreadLocalRandom.current().nextInt(0x00, 0x7F);

        
        
        
        this.router_serial_number_byte = new ArrayList<>();
        this.router_serial_number_byte.add(client_sn_tmp[0]);
        this.router_serial_number_byte.add(client_sn_tmp[1]);
        this.router_serial_number_byte.add(client_sn_tmp[2]);
        this.router_serial_number_byte.add(client_sn_tmp[3]);





    }

}
