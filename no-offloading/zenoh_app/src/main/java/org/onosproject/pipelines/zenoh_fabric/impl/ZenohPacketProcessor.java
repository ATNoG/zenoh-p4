/*
 * Copyright 2016-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.pipelines.zenoh_fabric.impl;


import org.apache.commons.codec.binary.Hex;
import com.google.common.collect.Maps;
import org.onosproject.mastership.MastershipService;
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.device.DeviceListener;
import org.onosproject.net.device.DeviceService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.onlab.packet.Ethernet;
import org.onlab.packet.MacAddress;
import org.onlab.packet.IpAddress;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.Link;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.Device;
import org.onosproject.net.driver.DriverService;
import org.onosproject.net.DeviceId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.DefaultFlowRule;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.packet.DefaultPacketContext;
import org.onosproject.net.packet.PacketContext;
import org.onosproject.net.packet.DefaultOutboundPacket;
import org.onosproject.net.packet.DefaultInboundPacket;
import org.onosproject.net.packet.InboundPacket;
import org.onosproject.net.packet.OutboundPacket;
import org.onosproject.net.packet.PacketPriority;
import org.onosproject.net.packet.PacketProcessor;
import org.onosproject.net.packet.PacketProvider;
import org.onosproject.net.packet.PacketProgrammable;
import org.onosproject.net.packet.PacketService;
import org.onosproject.net.flow.instructions.Instructions.OutputInstruction;
import org.onosproject.net.flow.instructions.Instructions;
import org.onosproject.net.flow.instructions.Instruction;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.group.GroupService;
import org.onosproject.net.group.GroupDescription;
import org.onosproject.net.group.DefaultGroupKey;
import org.onosproject.net.group.GroupBuckets;
import org.onosproject.net.group.GroupBucket;
import static org.onosproject.net.group.DefaultGroupBucket.createAllGroupBucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.InetAddress;
import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.UUID;
import org.onosproject.net.intf.Interface;
import org.onosproject.net.intf.InterfaceService;
import org.onosproject.net.config.ConfigFactory;
import org.onosproject.net.config.Config;
import org.onosproject.net.config.NetworkConfigService;
import org.onosproject.net.pi.model.PiActionId;
import org.onosproject.net.pi.model.PiActionParamId;
import org.onosproject.net.pi.model.PiMatchFieldId;
import org.onosproject.net.pi.runtime.PiAction;
import org.onosproject.net.pi.runtime.PiActionParam;
import org.onosproject.net.host.InterfaceIpAddress;
import org.onosproject.net.flow.criteria.PiCriterion;
import org.apache.felix.scr.annotations.Service;
import java.nio.ByteBuffer;
import org.onlab.packet.BasePacket;
import org.onlab.packet.ARP;
import org.onlab.packet.UDP;
import org.onlab.packet.IP;
import org.onlab.packet.IPv4;
import org.onlab.packet.Ip4Address;
import org.onlab.packet.TpPort;
import org.onlab.packet.IPacket;
import java.net.InetAddress;
import org.onlab.packet.MacAddress;
import java.util.Set;
import java.util.Map;
import java.util.Optional;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.onlab.packet.IpAddress.Version;

import org.json.JSONObject;
import org.json.JSONArray;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import org.apache.commons.io.IOUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.net.URL;

import java.nio.BufferUnderflowException;

import static org.onosproject.pipelines.zenoh_fabric.impl.AppConstants.CLEAN_UP_DELAY;
import static org.onosproject.pipelines.zenoh_fabric.impl.AppConstants.INITIAL_SETUP_DELAY;

import org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils.Declaration;
import org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils.Header;
import static org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils.Types.*;

import org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils.*;
import java.util.concurrent.ThreadLocalRandom;
import static java.util.stream.Collectors.toList;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


@Component(immediate = true, service = ZenohPacketProcessorService.class)
public class ZenohPacketProcessor implements ZenohPacketProcessorService{


    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected PacketService packetService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public FlowRuleService flowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public MastershipService mastershipService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public NetworkConfigService configService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public GroupService groupService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public ZenohTopologyManagerService topologyZenohService;

    private final DeviceListener deviceListener = new InternalDeviceListener();

    private PacketProcessor processor;

    private ApplicationId appId;

    private UtilsComponent utilsComponent = new UtilsComponent();

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Map<Integer, ZenohHost> zenoh_hosts = new HashMap<>();
    private Map<Integer, ZenohHost> tmp_zenoh_hosts = new HashMap<>();


    private Map<DeviceId, DeviceConfig> list_devices = new HashMap<>();

    private Map<Integer, String> reversePeerIds = new HashMap<>();
    private Map<String, Integer> peerIds = new HashMap<>();


    private Map<Integer, Topic> topicList = new HashMap<>();

    private Map<DeviceId, List<ZenohRouter>> link_routers = new HashMap<>();

    private Map<Integer, List<ZenohHost>> subscribers = new HashMap<>();


    // Conditionals used to control if and which declarations are to be sent to hosts
    private boolean sendDeclarationToHost = false;
    private boolean sendNewDeclarations = false;

    // Lists used to store new and overall Declarations
    private List<Declaration> tmpDeclarations = new ArrayList<>();
    private List<Declaration> declarations = new ArrayList<>();


    // Map used to store information about which ports are bounded to a specific multicast group
    private Map<DeviceId, DevMulticast> multicastInfo = new HashMap<>();

    private JSONObject deviceConfig = null;

    @Activate
    protected void activate() {

        try{


            URL config = this.getClass().getResource("/config/netcfg_fullCaseLinear4.json");
            InputStream is = config.openStream();
            String jsonTxt = IOUtils.toString(is, StandardCharsets.UTF_8);

            log.info(jsonTxt);

            deviceConfig  = new JSONObject(jsonTxt);
            KeepAliveManager kaManager = new KeepAliveManager(this);
            kaManager.start();

            appId = coreService.getAppId("org.onosproject.zenoh_app");


            processor = new ZenohService();
            packetService.addProcessor(processor, PacketProcessor.DIRECTOR_MAX);

            // Requests all packets that are zenoh packets, i.e. all packets sent to the controller with the udp dst 7447
            packetService.requestPackets(DefaultTrafficSelector.builder().matchIPProtocol((byte) 17).matchUdpDst​(TpPort.tpPort(7447)).build(), PacketPriority.MAX, appId);


            // Register listeners to be informed about device and host events.
            deviceService.addListener(deviceListener);
            // Schedule set up of existing devices. Needed when reloading the app.
            utilsComponent.scheduleTask(this::setUpAllDevices, INITIAL_SETUP_DELAY);



            Declaration d1 = new ResourceDeclaration();
            ResKey rk = new ResKey();
            d1.setResourceID(1);
            rk.setResourceId(0);
            d1.setReskey(rk);

            int topic_len = 0;
            int end_topic = 4;
            String topic = "/root";
            rk.setSuffix(topic);

            declarations.add(d1);

        }catch(Exception e){
            log.error(e.toString());
        }
    }


    /**
     * Deactivates the processor by removing it.
     */
    @Deactivate
    protected void deactivate() {
        deviceService.removeListener(deviceListener);

        log.info("Stopped");
    }


    /**
     * Sets up everything necessary to support zenoh session packet processing.
     *
     * @param deviceId the device to set up
     */
    private void setUpDevice(DeviceId deviceId) {


        log.info("CONFIGURATION: " + deviceId.toString());
        JSONObject tmp = (JSONObject) deviceConfig.get("devices");
        Set<String> devices = tmp.keySet();
        for(String device : devices){
            //Configuration belongs to this equipment
            if(device.equals(deviceId.toString())){
                JSONObject tmp2 = tmp.getJSONObject(device).getJSONObject("segmentrouting");

                DeviceConfig dev_cf = new DeviceConfig(device, tmp2.getString("routerMac"),tmp2.getString("ipv4Loopback"));
                log.info(dev_cf.toString());
                list_devices.put(deviceId, dev_cf);
            }
        }


        boolean added = false;
        String peerId;
        while(!added){
            peerId = UUID.randomUUID().toString().replaceFirst("00", "").replace("-", "").replace(" ", "");
            log.info(peerId);
            if (peerIds.containsKey(peerId)){
                continue;
            }else{
                peerIds.put(peerId, deviceId.hashCode());
                reversePeerIds.put(deviceId.hashCode(), peerId);
                added = true;

            }

        }
        insertZenohSessionRules(deviceId);
        multicastInfo.put(deviceId, new DevMulticast());

    }




    private void insertZenohSessionRules(DeviceId deviceId){

        final PiCriterion filter_init = PiCriterion.builder()
                                        .matchTernary(
                                            PiMatchFieldId.of("send"),
                                            1,
                                            1)
                                        .build();

        final PiAction send_to_cpu = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.acl.punt_to_cpu"))
                    .build();


        final String table = "FabricIngress.acl.acl";




        final FlowRule rule1 = Utils.buildFlowRule(
                deviceId, appId, table,
                filter_init, send_to_cpu);



        flowRuleService.applyFlowRules(rule1);


    }



    @Override
    public void sendInitAck(){}

    @Override
    public void sendOpenAck(){}

    @Override
    public void sendKeepAlive(){}

    @Override
    public void sendResourceDeclaration(){}

     /**
     * Listener of device events.
     */
    public class InternalDeviceListener implements DeviceListener {

        @Override
        public boolean isRelevant(DeviceEvent event) {
            switch (event.type()) {
                case DEVICE_ADDED:
                case DEVICE_AVAILABILITY_CHANGED:
                    break;
                default:
                    // Ignore other events.
                    return false;
            }
            // Process only if this controller instance is the master.
            final DeviceId deviceId = event.subject().id();
            return mastershipService.isLocalMaster(deviceId);
        }

        @Override
        public void event(DeviceEvent event) {
            final DeviceId deviceId = event.subject().id();
            if (deviceService.isAvailable(deviceId)) {
                // A P4Runtime device is considered available in ONOS when there
                // is a StreamChannel session open and the pipeline
                // configuration has been set.

                // Events are processed using a thread pool defined in the
                // MainComponent.
                utilsComponent.getExecutorService().execute(() -> {
                    log.info("{} event! deviceId={}", event.type(), deviceId);

                    setUpDevice(deviceId);
                });
            }
        }
    }



    /**
     * Sets up L2 bridging on all devices known by ONOS and for which this ONOS
     * node instance is currently master.
     * <p>
     * This method is called at component activation.
     */
    private void setUpAllDevices() {
        deviceService.getAvailableDevices().forEach(device -> {
            if (mastershipService.isLocalMaster(device.id())) {
                log.info("*** ZENOH_APP - Starting initial set up for {}...", device.id());
                setUpDevice(device.id());
            }
        });
    }



   private class ZenohService implements PacketProcessor {
        /**
         * Learns the source port associated with the packet's DeviceId if it has not already been learned.
         * Calls actLikeSwitch to process and send the packet.
         * @param pc PacketContext object containing packet info
         */
        @Override
        public void process(PacketContext pc) {


            ConnectPoint cp = pc.inPacket().receivedFrom();

            DeviceId devID = cp.deviceId();

            DeviceConfig devConfigTmp = list_devices.get(devID);
            /*
            if (devConfigTmp == null){
                return;
            }

            */

            Ethernet ethernet_frame = (Ethernet) pc.inPacket().parsed();
            if(ethernet_frame.getEtherType() == Ethernet.TYPE_IPV4){
                IPv4 ip_frame = (IPv4) ethernet_frame.getPayload();
                if(ip_frame.getProtocol() ==  IPv4.PROTOCOL_UDP){



                    UDP udp_frame = (UDP) ip_frame.getPayload();

                    if(udp_frame.getDestinationPort() == 7447){
                        if (pc.isHandled()){
                            log.warn("Already Handled");
                            return;

                        }

                        IPacket zenoh_frame = udp_frame.getPayload();
                        byte[] serialized_zenoh_frame = zenoh_frame.serialize();

                        log.info("Zenoh Hex Bytes:");
                        log.info(Ethernet.bytesToHex​(serialized_zenoh_frame));

                        Header h = new Header(serialized_zenoh_frame[0]);


                        Packet packet = processMessageId(pc, h, ethernet_frame, ip_frame, udp_frame, serialized_zenoh_frame, devID, cp);


                        if(sendNewDeclarations){
                            send_NewDeclarations(ip_frame, udp_frame);
                        }



                        if(packet == null){
                            return;
                        }


                        //  preparation of packet out packet
                        Ethernet new_ethernet_frame = new Ethernet();
                        new_ethernet_frame.setDestinationMACAddress​( ethernet_frame.getSourceMACAddress());
                        new_ethernet_frame.setSourceMACAddress​(MacAddress.valueOf(devConfigTmp.getMac()).toBytes());
                        new_ethernet_frame.setEtherType( ethernet_frame.getEtherType());

                        IPv4 new_ip_frame = new IPv4();
                        new_ip_frame.setDestinationAddress​(ip_frame.getSourceAddress());
                        new_ip_frame.setSourceAddress​(devConfigTmp.getIpv4());
                        new_ip_frame.setProtocol(IPv4.PROTOCOL_UDP);
                        new_ip_frame.setTtl​((byte) 127);
                        new_ip_frame.setFlags((byte) 2);
                        new_ip_frame.setIdentification((short) (ip_frame.getIdentification() + 1));


                        UDP new_udp_frame = new UDP();
                        new_udp_frame.setSourcePort​(7447);
                        new_udp_frame.setDestinationPort​( udp_frame.getSourcePort());

                        //  chaining packets
                        new_udp_frame.setPayload(packet);
                        new_ip_frame.setPayload(new_udp_frame);
                        new_ethernet_frame.setPayload(new_ip_frame);


                        log.info(Ethernet.bytesToHex​(new_ethernet_frame.serialize()));


                        sendPacketOut(pc, cp.port(),cp.deviceId(), new_ethernet_frame.serialize());

                        // Increment the serial number attached to this host
                        if(packet.getPacketType() != Types.OPEN && packet.getPacketType() != Types.INIT){
                            zenoh_hosts.get(Objects.hash(ip_frame.getSourceAddress(), udp_frame.getSourcePort())).incrementRouter_serial_number();
                        }

                        //  if declarations are t be sent to host then list of declarations must be sent to host
                        if(sendDeclarationToHost && declarations.size() != 0){
                            sendAllDeclarations(pc, cp, ip_frame, udp_frame, new_ethernet_frame, new_ip_frame);
                        }


                        for(Declaration d: tmpDeclarations){
                            declarations.add(d);

                        }
                        tmpDeclarations.clear();
                    }
                }
            }
        }

    }

    /**
     *
     * Function used to send packets from the controllers to the switches
     *
     *
     *
    */
    @Override
    public void sendPacketOut(PacketContext pc, PortNumber pn, DeviceId deviceId, byte[] packet){

        try{
            OutputInstruction o = Instructions.createOutput(pn);
            OutboundPacket out_p = new DefaultOutboundPacket(deviceId, DefaultTrafficTreatment.builder().add(o).build(), ByteBuffer.wrap(packet));

            TrafficTreatment t = out_p.treatment();
            log.info("Instructions");
            for(Instruction i : t.allInstructions()){
                log.info(i.type().toString());
            }

            packetService.emit(out_p);
            if(pc != null){
                pc.block();
            }

        }catch(Exception e){

            log.warn(e.toString());
        }

    }


    private ClosePacket createClosePacket(byte[] serialized_zenoh_frame, Header h){

        ClosePacket closeP = new ClosePacket(h.isFlag2(), h.isFlag3());
        closeP.setPayloadBytes(serialized_zenoh_frame);


        return closeP;


    }



    private HelloPacket createHelloPacket(Ethernet ethernet_frame, IPv4 ip_frame, UDP udp_frame, byte[] serialized_zenoh_frame, DeviceId devID){
        HelloPacket hp = new HelloPacket(false,true,true);
        ByteBuffer bb = ByteBuffer.allocate(6);
        bb.put((byte)4);
        bb.putInt(reversePeerIds.get(devID.hashCode()).hashCode());
        bb.put((byte) ROUTER);
        hp.setPayloadBytes(bb.array());

        return hp;

    }


    private InitPacket createInitAck(Ethernet ethernet_frame, IPv4 ip_frame, UDP udp_frame, byte[] serialized_zenoh_frame, DeviceId devID, int portNumber){


        int whatAmI = serialized_zenoh_frame[2];
        int peerIdSize = serialized_zenoh_frame[3];

        byte[] peerId = Arrays.copyOfRange(serialized_zenoh_frame, 4, peerIdSize + 4 );


        byte[] cookie = generateCookie(whatAmI, peerIdSize, peerId, 0,0);


        //Host Creation
        ZenohHost zh = new ZenohHost(ip_frame.getSourceAddress(), ip_frame.getDestinationAddress(), ethernet_frame.getSourceMACAddress(), ethernet_frame.getDestinationMACAddress(), udp_frame.getSourcePort());
        zh.setCookie(cookie);
        zh.setPeerId(peerId);
        zh.setDeviceId(devID.toString());
        zh.setPortNumber(portNumber);



        tmp_zenoh_hosts.put(zh.hashCodeKey(), zh);
        InitPacket ip = new InitPacket(false,true);

        log.info(Ethernet.bytesToHex​(cookie));

        //log.info(String.valueOf(reversePeerIds.get(devID.hashCode())));

        log.info(reversePeerIds.toString());
        log.info(Ethernet.bytesToHex(new BigInteger(String.valueOf(reversePeerIds.get(devID.hashCode())), 16).toByteArray()));

        byte[] tmp = new BigInteger(String.valueOf(reversePeerIds.get(devID.hashCode())), 16).toByteArray();


        byte[] MyPeerId;

        MyPeerId = (tmp[0] == 0 ? Arrays.copyOfRange(tmp, 1, tmp.length) : tmp);


        ByteBuffer bb = ByteBuffer.allocate(1 + 1  + MyPeerId.length + 1 + cookie.length );
        //whatAmI
        bb.put((byte) ROUTER);
        //MypeerIdSize
        bb.put((byte) MyPeerId.length);
        //MypeerId
        bb.put(MyPeerId);
        //cookieLen
        bb.put((byte) cookie.length );
        //cookie
        bb.put(cookie);


        ip.setPayloadBytes(bb.array());

        return ip;

    }


    private OpenPacket createOpenAck(Ethernet ethernet_frame, IPv4 ip_frame, UDP udp_frame, byte[] serialized_zenoh_frame, Header h, DeviceId devID){


        ZenohHost zh_tmp = tmp_zenoh_hosts.get(Objects.hash(ip_frame.getSourceAddress(), udp_frame.getSourcePort()));


        ZenohHost zh = zh_tmp.copy();


        //Remove host from temporary list of hosts, session established so can go to final list of hosts
        tmp_zenoh_hosts.remove(Objects.hash(ip_frame.getSourceAddress(), udp_frame.getSourcePort()));


        log.info(zh.toString());
        byte lease = serialized_zenoh_frame[1];

        int i;
        for(i=2; i< serialized_zenoh_frame.length ; i++){

            if( (serialized_zenoh_frame[i] & 0x80) != 0x80){
                break;
            }

        }
        i+=1;

        byte[] initial_sn = Arrays.copyOfRange(serialized_zenoh_frame, 2, i);
        log.info("Initial SN");
        log.info(Ethernet.bytesToHex(initial_sn));

        //push serial number to p4 switch


        byte[] cookie_to_match = zh.getCookie();
        byte[] cookie = Arrays.copyOfRange(serialized_zenoh_frame,i+1, i + 1 + cookie_to_match.length);

        log.info("Cookie to Match");
        log.info(Ethernet.bytesToHex(cookie_to_match));
        log.info("Cookie");
        log.info(Ethernet.bytesToHex(cookie));


        zh.setSerial_number(initial_sn);
        

        log.info(Arrays.toString(zh.getSerial_number()));

        zh.setRouter_serial_number();
        OpenPacket op = new OpenPacket(h.isFlag2(), true);
        
        byte[] sn = zh.getRouter_serial_number();
       
        byte[] payloadBytes = new byte[1 + sn.length ];
        
        //lease time
        payloadBytes[0] = (byte) lease;
        
        log.info(Ethernet.bytesToHex(sn));
        System.arraycopy(sn, 0, payloadBytes, 1, sn.length);
        
        op.setPayloadBytes(payloadBytes);

        // put host in the final list of hosts
        zenoh_hosts.put(zh.hashCodeKey(), zh);


        return op;
    }


    private Packet processMessageId(PacketContext pc, Header h, Ethernet ethernet_frame, IPv4 ip_frame, UDP udp_frame, byte[] serialized_zenoh_frame, DeviceId devID, ConnectPoint cp){
        Packet packet = null;
        ZenohHost zn = null;
        Header h1 = null;
        byte[] new_zenoh_frame = null;

        if(zenoh_hosts.containsKey(Objects.hash(ip_frame.getSourceAddress(), udp_frame.getSourcePort()))){
            zn = zenoh_hosts.get(Objects.hash(ip_frame.getSourceAddress(), udp_frame.getSourcePort()));
        }else if(tmp_zenoh_hosts.containsKey(Objects.hash(ip_frame.getSourceAddress(), udp_frame.getSourcePort()))){
            zn = tmp_zenoh_hosts.get(Objects.hash(ip_frame.getSourceAddress(), udp_frame.getSourcePort()));
        }
        switch(h.getMsgId()){
            case ATTACHMENT:
                log.info("ATTACHMENT PACKET");

                int attachment_len = serialized_zenoh_frame[1];

                log.info("Attach Len:");
                log.info(String.valueOf(attachment_len));

                // if attachment not empty, payload need to be processed
                if(serialized_zenoh_frame.length > (attachment_len + 2)){
                    h1 = new Header(serialized_zenoh_frame[attachment_len + 2]);
                    new_zenoh_frame = Arrays.copyOfRange(serialized_zenoh_frame, attachment_len + 2, serialized_zenoh_frame.length);
                    log.info("new zenoh Frame:");
                    log.info(Ethernet.bytesToHex(new_zenoh_frame));

                    packet = processMessageId(pc, h1, ethernet_frame, ip_frame, udp_frame, new_zenoh_frame, devID, cp);
                }

                break;
            case SCOUT:
                packet = createHelloPacket(ethernet_frame, ip_frame, udp_frame, serialized_zenoh_frame, devID);
                log.info("SCOUT PACKET");
                break;
            case INIT:
                log.info("INIT PACKET");
                packet = createInitAck(ethernet_frame, ip_frame, udp_frame, serialized_zenoh_frame, devID, (int) cp.port().toLong());
                break;
            case OPEN:


                if(udp_frame.getSourcePort() != 7447){
                    sendDeclarationToHost = true;
                }

                log.info("OPEN PACKET");
                log.info("device:" + devID.toString());

                packet = createOpenAck(ethernet_frame, ip_frame, udp_frame, serialized_zenoh_frame, h,devID);
                break;
            case CLOSE:
                log.info("CLOSE PACKET");

                ClosePacket closePacket = createClosePacket(serialized_zenoh_frame, h);
                if(closePacket.getHeader().isFlag2()){
                    if(Arrays.equals(zn.getPeerId(), closePacket.getPeerId())){
                        //remove host from zenoh host list
                        zenoh_hosts.remove(zn.hashCodeKey());

                        log.info("Removed: " + Arrays.toString(zn.getPeerId()));
                        //send packet out
                        sendPacketOut(pc, cp.port(),cp.deviceId(), ethernet_frame.serialize());
                    }
                }

                break;

            case FRAME:
                log.info("FRAME PACKET");
                log.info("Device: " + devID.toString());
                log.info("ConnectionPoint: " + cp.toString());
                int i;
                List<Byte> serialN = new ArrayList<>();
                for(i=1; i< serialized_zenoh_frame.length ; i++){
                    serialN.add(serialized_zenoh_frame[i]);
                    if( (serialized_zenoh_frame[i] & 0x80) != 0x80){
                        break;
                    }

                }
                i+=1;
                int len = 0;
                

                len = Arrays.copyOfRange(serialized_zenoh_frame, 1, i).length;
                
                byte[] packet_serial_number = new byte[serialN.size()];
                
                for(int j = 0; j < packet_serial_number.length ; j++){
                    packet_serial_number[j] = (byte) serialN.get(j).byteValue();
                }

                log.info("Serial Number Packet: " + Ethernet.bytesToHex(packet_serial_number));
                if(zn != null){
                
                    // if serial number expected, then payload need to be processed
                    if(Arrays.equals(zn.getSerial_number(),packet_serial_number)){
                        zn.incrementSerial_number();
                    }else{
                        zn.setSerial_number(packet_serial_number);
                    }
                }
                
                /*
                    log.info("Serial Number: " + Ethernet.bytesToHex(zn.getSerial_number()));
                    // if serial number expected, then payload need to be processed
                    if(Arrays.equals(zn.getSerial_number(),packet_serial_number)){
                        zn.incrementSerial_number();

                    }else{
                        Packet p_acknack = new Packet();

                        byte[] ack_nack = new byte[1 + zn.getSerial_number().length];
                        
                        ack_nack[0] = (byte) 0x07;
                        
                        System.arraycopy(zn.getSerial_number(), 0, ack_nack, 1, zn.getSerial_number().length);
                        

                        p_acknack.setPayload(ack_nack);

                        //  preparation of packet out packet
                        Ethernet new_ethernet_frame = new Ethernet();
                        new_ethernet_frame.setDestinationMACAddress​(zn.getMac_address());
                        new_ethernet_frame.setSourceMACAddress​(zn.getRouter_mac_address());
                        new_ethernet_frame.setEtherType( ethernet_frame.getEtherType());

                        IPv4 new_ip_frame = new IPv4();
                        new_ip_frame.setDestinationAddress​(zn.getIpv4());
                        new_ip_frame.setSourceAddress​(zn.getIpv4_router());
                        new_ip_frame.setProtocol(IPv4.PROTOCOL_UDP);
                        new_ip_frame.setTtl​((byte) 127);
                        new_ip_frame.setFlags((byte) 2);


                        UDP new_udp_frame = new UDP();
                        new_udp_frame.setSourcePort​(7447);
                        new_udp_frame.setDestinationPort​(zn.getUdpPort());

                        //  chaining packets
                        new_udp_frame.setPayload(p_acknack);
                        new_ip_frame.setPayload(new_udp_frame);
                        new_ethernet_frame.setPayload(new_ip_frame);

                        sendPacketOut(null, PortNumber.portNumber(zn.getPortNumber()), DeviceId.deviceId(zn.getDeviceId()), new_ethernet_frame.serialize());

                        return null;
                    }
                }
                */

                h1 = new Header(serialized_zenoh_frame[5]);
                new_zenoh_frame = Arrays.copyOfRange(serialized_zenoh_frame, i, serialized_zenoh_frame.length);

                processMessageId(pc, h1, ethernet_frame, ip_frame, udp_frame, new_zenoh_frame, devID, cp);

                break;
            case DECLARE:
                log.info("DECLARE PACKET");
                sendNewDeclarations  = true;
                int byte_to_process = 1;

                log.info("Hex Bytes:" + Ethernet.bytesToHex(serialized_zenoh_frame));

                //log.info("byte: " + String.valueOf(byte_to_process));
                int numberDeclarations = serialized_zenoh_frame[byte_to_process];
                byte_to_process += 1;

                //log.info("byte: " + String.valueOf(byte_to_process));
                //log.info("Number of Declarations: " + String.valueOf(numberDeclarations));
                int declarationType = serialized_zenoh_frame[byte_to_process] & (byte) 0x1f;
                log.info("Declarations Type: " + String.valueOf(declarationType));
                int[] flags = new int[3];
                flags[0] = ((int) serialized_zenoh_frame[byte_to_process] >>>5)  & 0x01 ;
                flags[1] = ((int)serialized_zenoh_frame[byte_to_process]  >>>6)  & 0x01;
                flags[2] = ((int) serialized_zenoh_frame[byte_to_process] >>>7)  & 0x01 ;

                log.info("Flags: " + Arrays.toString(flags));
                byte_to_process += 1;

                //log.info("byte: " + String.valueOf(byte_to_process));

                Declaration d = createDeclaration(declarationType, flags);
                ResKey rk = new ResKey();
                if(d != null){
                    if(d.getType() == Types.RESOURCE){
                        d.setResourceID((int) serialized_zenoh_frame[byte_to_process]);
                        byte_to_process += 1;

                        log.info("byte Resource: " + String.valueOf(byte_to_process));

                    }
                    rk.setResourceId(serialized_zenoh_frame[byte_to_process]);
                    byte_to_process += 1;
                    //log.info("byte: " + String.valueOf(byte_to_process));
                    rk.setK(flags[2] == 1 ? true : false);
                    d.setReskey(rk);

                    int topic_len = 0;
                    int end_topic = 4;
                    String topic = null;
                    if(flags[2] != 1){
                        topic_len = serialized_zenoh_frame[byte_to_process];
                        byte_to_process += 1;
                        end_topic = byte_to_process + topic_len;
                        topic = new String(Arrays.copyOfRange(serialized_zenoh_frame, byte_to_process, end_topic), StandardCharsets.UTF_8);
                        log.info("Topic:" + topic);
                        rk.setSuffix(topic);
                    }

                    switch(declarationType){
                        case Types.PUBLISHER:
                            break;
                        case Types.SUBSCRIBER:

                            zn.setType(Types.SUBSCRIBER);

                            // Add host to topic list of host
                            topicList.get(d.getReskey().getResourceId()).addHost(zn);

                            // Add device to list of topic devices if it does not exist already
                            boolean devAdded = !topicList.get(d.getReskey().getResourceId()).getList_devices().contains(devID) ? topicList.get(d.getReskey().getResourceId()).addDevice(devID) : false;

                            // Physical port is bounded to the multicast group from which that topic belongs
                            //pushSubRules(d, zn);

                            break;
                        case Types.RESOURCE:
                            // If it is a not know resource, then must be saved
                            if(!topicList.containsKey(d.getResourceID())){
                                topicList.put( d.getResourceID(), new Topic(topic));
                            }


                    }

                    if(!tmpDeclarations.contains(d)){
                        tmpDeclarations.add(d);

                    }
                    if(serialized_zenoh_frame.length > end_topic){
                        h1 = new Header(serialized_zenoh_frame[end_topic]);

                        new_zenoh_frame = Arrays.copyOfRange(serialized_zenoh_frame, end_topic, serialized_zenoh_frame.length);
                        processMessageId(pc, h1, ethernet_frame, ip_frame, udp_frame, new_zenoh_frame, devID, cp);
                    }
                }
                break;

            /**
             *
             *  IN CASE OF A DATA PACKET PATHS MUST BE COMPUTED AND RULES PUSHED TO SWITCHES
             *
             */
            case DATA:


                log.info("DATA PACKET");


                int topic_number = serialized_zenoh_frame[1];

                Topic t = topicList.get(topic_number);


                List<ZenohHost> hosts_device = t.getList_ZH().stream().filter(host -> host.getDeviceId() == devID.toString()).collect(Collectors.toList());


                Set<DeviceId> devicesWithSubs = zenoh_hosts.values().stream().filter(zh -> (zh.getType() == Types.SUBSCRIBER))
                                                                             .map(ZenohHost::getDeviceId)
                                                                             .map(deviceID -> DeviceId.deviceId(deviceID))
                                                                             .collect(Collectors.toSet());

                log.info("devicesWithSubs:" + devicesWithSubs.toString());


                Map<DeviceId,NodePath> pathsFromDev = topologyZenohService.getPaths(devID);
                
                if(pathsFromDev != null){

                    List<Link> links_toSend =  pathsFromDev.values().stream().map(l -> l.getNodePath_links()).filter(p -> p.size() != 0).filter(list -> devicesWithSubs.contains(list.get(list.size()-1).dst().deviceId())).map(p -> p.get(0) ).collect(Collectors.toList()) ;

                    log.info("linksToSend:" + links_toSend.toString());





                    for(Link l: links_toSend){
                        DeviceId srcDevLink = l.src().deviceId();
                        DeviceId dstDevLink = l.dst().deviceId();

                        DeviceConfig dc_src = list_devices.get(srcDevLink);
                        DeviceConfig dc_dst = list_devices.get(dstDevLink);




                        ZenohRouter zr_tmp = new ZenohRouter(dstDevLink);

                        if(link_routers.containsKey(srcDevLink)){
                            if(link_routers.get(srcDevLink).contains(zr_tmp)){
                                int index_router = link_routers.get(srcDevLink).indexOf(zr_tmp);
                                zr_tmp = link_routers.get(srcDevLink).get(index_router);
                                zr_tmp.updateSN();
                            }

                        }else{
                            zr_tmp.setSerial_number();

                            List<ZenohRouter> list_routers = new ArrayList<>();
                            list_routers.add(zr_tmp);

                            link_routers.put(srcDevLink, list_routers);
                        }

                        log.info(zr_tmp.toString());

                        byte[] sn = zr_tmp.getSerial_number();

                        log.info("serialNumber:" + Ethernet.bytesToHex(sn));

                        byte[] frameHeader = new Header(false, false, true, Types.FRAME).toBytes();


                        Packet p = new Packet();

                        byte[] data_packet = new byte[frameHeader.length + sn.length + serialized_zenoh_frame.length];

                        System.arraycopy(frameHeader, 0, data_packet, 0, frameHeader.length);
                        System.arraycopy(sn, 0, data_packet, frameHeader.length, sn.length);
                        System.arraycopy(serialized_zenoh_frame, 0, data_packet, frameHeader.length + sn.length, serialized_zenoh_frame.length );


                        p.setPayload(data_packet);

                        log.info("payload:" + Ethernet.bytesToHex(p.serialize()));

                        //  preparation of packet out packet
                        Ethernet new_ethernet_frame = new Ethernet();
                        new_ethernet_frame.setDestinationMACAddress​(MacAddress.valueOf(dc_dst.getMac()).toBytes());
                        new_ethernet_frame.setSourceMACAddress​(MacAddress.valueOf(dc_src.getMac()).toBytes());
                        new_ethernet_frame.setEtherType( ethernet_frame.getEtherType());

                        IPv4 new_ip_frame = new IPv4();
                        new_ip_frame.setDestinationAddress​(IPv4.toIPv4Address​(dc_dst.getIpv4()));
                        new_ip_frame.setSourceAddress​(IPv4.toIPv4Address​(dc_src.getIpv4()));
                        new_ip_frame.setProtocol(IPv4.PROTOCOL_UDP);
                        new_ip_frame.setTtl​((byte) 127);
                        new_ip_frame.setFlags((byte) 2);


                        UDP new_udp_frame = new UDP();
                        new_udp_frame.setSourcePort​(7447);
                        new_udp_frame.setDestinationPort​(7447);

                        //  chaining packets
                        new_udp_frame.setPayload(p);
                        new_ip_frame.setPayload(new_udp_frame);
                        new_ethernet_frame.setPayload(new_ip_frame);


                        log.info("packet: " +  Ethernet.bytesToHex(new_ethernet_frame.serialize()));

                        sendPacketOut(null, l.src().port(), srcDevLink, new_ethernet_frame.serialize());

                    }
                }
                for(ZenohHost z : hosts_device){
                    
                    byte[] sn = z.getRouter_serial_number();

                    z.incrementRouter_serial_number();

                    byte[] frameHeader = new Header(false, false, true, Types.FRAME).toBytes();


                    Packet p_h = new Packet();

                    byte[] data_packet = new byte[frameHeader.length + sn.length + serialized_zenoh_frame.length];

                    System.arraycopy(frameHeader, 0, data_packet, 0, frameHeader.length);
                    System.arraycopy(sn, 0, data_packet, frameHeader.length, sn.length);

                    System.arraycopy(serialized_zenoh_frame, 0, data_packet, frameHeader.length + sn.length, serialized_zenoh_frame.length);


                    p_h.setPayload(data_packet);

                    //  preparation of packet out packet
                    Ethernet new_ethernet_frame = new Ethernet();
                    new_ethernet_frame.setDestinationMACAddress​(z.getMac_address());
                    new_ethernet_frame.setSourceMACAddress​(z.getRouter_mac_address());
                    new_ethernet_frame.setEtherType( ethernet_frame.getEtherType());

                    IPv4 new_ip_frame = new IPv4();
                    new_ip_frame.setDestinationAddress​(z.getIpv4());
                    new_ip_frame.setSourceAddress​(z.getIpv4_router());
                    new_ip_frame.setProtocol(IPv4.PROTOCOL_UDP);
                    new_ip_frame.setTtl​((byte) 127);
                    new_ip_frame.setFlags((byte) 2);


                    UDP new_udp_frame = new UDP();
                    new_udp_frame.setSourcePort​(7447);
                    new_udp_frame.setDestinationPort​(z.getUdpPort());

                    //  chaining packets
                    new_udp_frame.setPayload(p_h);
                    new_ip_frame.setPayload(new_udp_frame);
                    new_ethernet_frame.setPayload(new_ip_frame);

                    sendPacketOut(null, PortNumber.portNumber(z.getPortNumber()), DeviceId.deviceId(z.getDeviceId()), new_ethernet_frame.serialize());
                }

                break;

        }


        return packet;
    }

    private Declaration createDeclaration(int declarationType, int[] flags){
        Declaration tmp = null;

            switch(declarationType){
                case Types.RESOURCE:
                    tmp = new ResourceDeclaration();
                    tmp.setFlag3(flags[2] == 1 ? true : false);
                    break;
                case Types.PUBLISHER:
                    tmp = new PublisherDeclaration();
                    tmp.setFlag3(flags[2] == 1 ? true : false);

                    log.info(declarations.stream().map(Object::toString).collect(Collectors.joining(", ")));
                    break;
                case Types.SUBSCRIBER:
                    tmp = new SubscriberDeclaration();
                    tmp.setFlag3(flags[2] == 1 ? true : false);
                    tmp.setFlag2(flags[1] == 1 ? true : false);
                    tmp.setFlag1(flags[0] == 1 ? true : false);
                    break;

            }

        return tmp;
    }



    private void pushIngressRules(int resource_id, DeviceId devId){

        /**
         *
         *
         *
         *   INGRESS RULES
         *
         *
         */

        final PiCriterion resourceID = PiCriterion.builder()
                .matchExact(
                        PiMatchFieldId.of("fabric_metadata.zenoh_resourceId"),
                        resource_id
                        )
                .build();

        // Actions
        final PiAction multicast_group = PiAction.builder()
                .withId(PiActionId.of("FabricIngress.zenoh_in.set_multicast"))
                .withParameter(new PiActionParam(PiActionParamId.of("multicast_group_id"), resource_id*100))
                .build();


        final String table_in = "FabricIngress.zenoh_in.TopicSubscriberGroup";




        final FlowRule ruleIn = Utils.buildFlowRule(
                devId, appId, table_in,
                resourceID, multicast_group);


        // Insert rules.
        flowRuleService.applyFlowRules(ruleIn);



    }


    private FlowRule createEgressRule(DeviceId devId, int egressPort, int multicast_group, byte[] src_macAddress, byte[] dst_macAddress, int srcIpv4, int dstIpv4, int udpPort){

        /**
        *
        *
        *
        *   EGRESS RULES
        *
        *
        */


        final PiCriterion filter = PiCriterion.builder()
                .matchExact(
                        PiMatchFieldId.of("standard_metadata.egress_port"),
                        egressPort

                )
                .matchExact(
                        PiMatchFieldId.of("standard_metadata.mcast_grp"),
                        multicast_group
                        )
                .build();



        // Actions
        final PiAction packet_change = PiAction.builder()
                .withId(PiActionId.of("FabricEgress.zenoh_out.sendDatatoSub"))
                .withParameter(new PiActionParam(PiActionParamId.of("src_eth"), src_macAddress))
                .withParameter(new PiActionParam(PiActionParamId.of("dst_eth"), dst_macAddress))
                .withParameter(new PiActionParam(PiActionParamId.of("src_ip"), srcIpv4))
                .withParameter(new PiActionParam(PiActionParamId.of("dst_ip"), dstIpv4))
                .withParameter(new PiActionParam(PiActionParamId.of("udp_dport"), udpPort))
                .build();


        final String table_out = "FabricEgress.zenoh_out.SendToSubscriber";



        final FlowRule ruleOut = Utils.buildFlowRule(
                devId, appId, table_out,
                filter, packet_change);


        return ruleOut;

    }


    private void pushEgressRules(ZenohHost zh, Declaration d){
        try {


            FlowRule ruleOut = createEgressRule(DeviceId.deviceId(zh.getDeviceId()),zh.getPortNumber(), d.getReskey().getResourceId()*100, zh.getRouter_mac_address(), zh.getMac_address(), zh.getIpv4_router(), zh.getIpv4(), zh.getUdpPort());

            // Insert rules.
            flowRuleService.applyFlowRules(ruleOut);

        } catch (Exception e) {
            //TODO: handle exception
        }

    }


    private void updateReplicationGroup(DeviceId devId, int resourceId, int portNumber){

        multicastInfo.get(devId).addPort(resourceId, portNumber);


        Collection<PortNumber> ports = multicastInfo.get(devId).getMulticastGroupsInfo().get(resourceId);

        //Updates replication group ports

        List<GroupBucket> bucketList = ports.stream()
        .map(p -> DefaultTrafficTreatment.builder().setOutput(p).build())
        .map(t -> createAllGroupBucket(t))
        .collect(Collectors.toList());


        log.info("BucketList: " + bucketList.toString());

        groupService.setBucketsForGroup(devId, new DefaultGroupKey(ByteBuffer.allocate(4).putInt(resourceId).array()),
        new GroupBuckets(bucketList), new DefaultGroupKey(ByteBuffer.allocate(4).putInt(resourceId).array()),appId);


    }


    private void pushSubRules(Declaration d, ZenohHost zh){
        log.info("PUSHING RULES TO DEVICE");
        try{



            int resource_id = d.getReskey().getResourceId();
            DeviceId devId = DeviceId.deviceId(zh.getDeviceId());



            // If topic general rule not pushed to switch then it must be pushed and replication group must be created
            if(!multicastInfo.get(devId).getMulticastGroupsInfo().containsKey(resource_id)){

                pushIngressRules(resource_id, devId);
                multicastInfo.get(devId).addPort(resource_id, zh.getPortNumber());

                // Forge group object.
                GroupDescription multicastGroup = Utils.buildMulticastGroup(
                    appId, devId, resource_id*100, multicastInfo.get(devId).getMulticastGroupsInfo().get(resource_id));

                // Insert.
                groupService.addGroup(multicastGroup);

            }else{
                updateReplicationGroup(devId, resource_id, zh.getPortNumber());
            }

            pushEgressRules(zh, d);

        }catch(Exception e){
            log.error(e.toString());
        }
    }





    private byte[] generateCookie(int whatAmI, int peerIdSize, byte[] pId, int sn_resolution, int nonce){

        String encryptionKey = "zenoh_p4zenoh_p4";

        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES + peerIdSize + ((sn_resolution != 0) ? sn_resolution : 0) +  ((nonce != 0) ? nonce : 0));

        bb.put((byte) whatAmI);
        bb.put(pId);
        if (sn_resolution != 0){
            bb.put((byte) sn_resolution);
        }

        if (nonce != 0){
            bb.put((byte) nonce);
        }


        byte[] cipherText = null;
        try{

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            byte[] key = encryptionKey.getBytes("UTF-8");
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            IvParameterSpec ivparameterspec = new IvParameterSpec(key);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivparameterspec);
            cipherText = cipher.doFinal(bb.array());
        }catch(Exception e){
            log.error(e.toString());
        }
        return cipherText;
    }

    @Deprecated
    private byte[] reverseArray(byte[] arr){

        byte[] tmp = new byte[arr.length];
        int j = arr.length;
        for(byte b : arr){
            tmp[j-1] = b;
            j-=1;
        }

        return tmp;
    }

    private void sendAllDeclarations(PacketContext pc, ConnectPoint cp, IPv4 ip_frame, UDP udp_frame, Ethernet new_ethernet_frame, IPv4 new_ip_frame){
        zenoh_hosts.get(Objects.hash(ip_frame.getSourceAddress(), udp_frame.getSourcePort())).setKeepAlive(true);


        byte[] zenoh_declarations_packet;

        byte[] frameHeader = new Header(false, false, true, Types.FRAME).toBytes();

        byte[] serial_number = zenoh_hosts.get(Objects.hash(ip_frame.getSourceAddress(), udp_frame.getSourcePort())).getRouter_serial_number();


        int overall_size = 0;
        for(Declaration d: declarations){

            overall_size += d.serialize().length;

        }

        byte[] declarations_bytes = new byte[overall_size];
        int filled = 0;

        for(Declaration d: declarations){
            byte[] declaration_b = d.serialize();

            log.info("Declaration:");
            log.info(Ethernet.bytesToHex(declaration_b));

            System.arraycopy(declaration_b, 0, declarations_bytes, filled, declaration_b.length);
            filled += declaration_b.length;

        }

        log.info("Declarations:");
        log.info(Ethernet.bytesToHex(declarations_bytes));







        zenoh_declarations_packet = new byte[frameHeader.length + serial_number.length + declarations_bytes.length];

        System.arraycopy(frameHeader, 0, zenoh_declarations_packet, 0, frameHeader.length);
        System.arraycopy(serial_number, 0, zenoh_declarations_packet, frameHeader.length, serial_number.length);
        System.arraycopy(declarations_bytes, 0, zenoh_declarations_packet, frameHeader.length + serial_number.length, declarations_bytes.length);



        Packet p = new Packet();
        p.setPayload(zenoh_declarations_packet);


        UDP new_udp_frame = new UDP();
        new_udp_frame.setSourcePort​(7447);
        new_udp_frame.setDestinationPort​( udp_frame.getSourcePort());
        new_ip_frame.setIdentification((short) (new_ip_frame.getIdentification() + 1));
        new_udp_frame.setPayload(p);

        new_ip_frame.setPayload(new_udp_frame);
        new_ethernet_frame.setPayload(new_ip_frame);
        sendPacketOut(pc, cp.port(),cp.deviceId(), new_ethernet_frame.serialize());
        zenoh_hosts.get(Objects.hash(ip_frame.getSourceAddress(), udp_frame.getSourcePort())).incrementRouter_serial_number();

        sendDeclarationToHost = false;
    }


    private void send_NewDeclarations(IPv4 ip_frame, UDP udp_frame){

        Ethernet eth;
        IPv4 ipv4;
        UDP udp;
        Packet p;

        for (Map.Entry<Integer, ZenohHost> host : zenoh_hosts.entrySet()) {
            int key = host.getKey();
            ZenohHost zh = host.getValue();
            if(key != Objects.hash(ip_frame.getSourceAddress(), udp_frame.getSourcePort()) || zh.getType() != Types.SUBSCRIBER){

                DeviceConfig dc_tmp = list_devices.get(DeviceId.deviceId(zh.getDeviceId()));

                byte[] zenoh_declarations_packet;

                byte[] frameHeader = new Header(false, false, true, Types.FRAME).toBytes();

                byte[] serial_number = zenoh_hosts.get(key).getRouter_serial_number();


                int overall_size = 0;
                for(Declaration d: tmpDeclarations){

                    overall_size += d.serialize().length;

                }

                byte[] declarations_bytes = new byte[overall_size];
                int filled = 0;

                for(Declaration d: tmpDeclarations){
                    byte[] declaration_b = d.serialize();

                    log.info("Declaration:");
                    log.info(Ethernet.bytesToHex(declaration_b));

                    System.arraycopy(declaration_b, 0, declarations_bytes, filled, declaration_b.length);
                    filled += declaration_b.length;

                }

                log.info("Declarations:");
                log.info(Ethernet.bytesToHex(declarations_bytes));


                zenoh_declarations_packet = new byte[frameHeader.length + serial_number.length + declarations_bytes.length];

                System.arraycopy(frameHeader, 0, zenoh_declarations_packet, 0, frameHeader.length);
                System.arraycopy(serial_number, 0, zenoh_declarations_packet, frameHeader.length, serial_number.length);
                System.arraycopy(declarations_bytes, 0, zenoh_declarations_packet, frameHeader.length + serial_number.length, declarations_bytes.length);



                eth = new Ethernet();
                eth.setDestinationMACAddress​( zh.getMac_address());
                eth.setSourceMACAddress​(MacAddress.valueOf(dc_tmp.getMac()).toBytes());
                eth.setEtherType(Ethernet.TYPE_IPV4);

                ipv4 = new IPv4();
                ipv4.setDestinationAddress​(zh.getIpv4());
                ipv4.setSourceAddress​(dc_tmp.getIpv4());
                ipv4.setProtocol(IPv4.PROTOCOL_UDP);
                ipv4.setTtl​((byte) 127);
                ipv4.setFlags((byte) 2);
                ipv4.setIdentification((short) ThreadLocalRandom.current().nextInt(0, Short.MAX_VALUE));


                udp = new UDP();
                udp.setSourcePort​(7447);
                udp.setDestinationPort​(zh.getUdpPort());


                p = new Packet();
                p.setPayload(zenoh_declarations_packet);

                udp.setPayload(p);
                ipv4.setPayload(udp);
                eth.setPayload(ipv4);

                sendPacketOut(null, PortNumber.portNumber(zh.getPortNumber()) , DeviceId.deviceId(zh.getDeviceId()), eth.serialize());

                zenoh_hosts.get(key).incrementRouter_serial_number();


            }


        }
        sendNewDeclarations = false;


    }

    @Override
    public List<ZenohHost> getZenohHosts(){
        List<ZenohHost> tmp = new ArrayList<>(zenoh_hosts.values());
        for(ZenohHost zn: tmp){
            log.info(zn.toString());
        }
        return tmp;

    }

    @Override
    public List<ZenohHost> getZenohHostsKeepAlive(){


        List<ZenohHost> tmp = zenoh_hosts.values().stream().filter(host -> host.getKeepAlive()).collect(Collectors.toList());;
        for(ZenohHost zn: tmp){
            log.info(zn.toString());
        }
        return tmp;

    }




    @Override
    public Map<DeviceId, DeviceConfig> getListDevices(){
        return list_devices;

    }

}
