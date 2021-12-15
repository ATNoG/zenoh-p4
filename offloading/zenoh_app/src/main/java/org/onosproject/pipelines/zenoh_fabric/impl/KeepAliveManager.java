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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.onlab.packet.IpAddress.Version;
import static org.onosproject.pipelines.zenoh_fabric.impl.AppConstants.CLEAN_UP_DELAY;
import static org.onosproject.pipelines.zenoh_fabric.impl.AppConstants.INITIAL_SETUP_DELAY;

import org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils.Declaration;
import org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils.Header;
import static org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils.Types.*;

import org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils.HelloPacket;
import org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils.InitPacket;
import org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils.OpenPacket;
import org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils.Types;
import org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils.ZenohHost;
import org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils.ResourceDeclaration;
import org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils.SubscriberDeclaration;
import org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils.PublisherDeclaration;
import org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils.ResKey;
import org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils.Packet;
import org.onosproject.pipelines.zenoh_fabric.impl.packetFormatUtils.KeepAlivePacket;

import java.lang.Short;
import java.util.concurrent.ThreadLocalRandom;


import java.lang.Thread;
public class KeepAliveManager extends Thread{


    private ZenohPacketProcessorService zpService;

    public KeepAliveManager(ZenohPacketProcessorService zpService){
        this.zpService = zpService;
    }

    @Override
    public void run() {
        while(true){
            try {

                Thread.sleep(3000);
            } catch (Exception e) {
                //TODO: handle exception
            }


            List<ZenohHost> tmp_zenoh_hosts = this.zpService.getZenohHostsKeepAlive();
            Map<DeviceId, DeviceConfig> list_devices = this.zpService.getListDevices();
            for(ZenohHost zh : tmp_zenoh_hosts ){

                DeviceConfig dc = list_devices.get(DeviceId.deviceId(zh.getDeviceId()));


                Ethernet new_ethernet_frame = new Ethernet();
                new_ethernet_frame.setDestinationMACAddress​( zh.getMac_address());
                new_ethernet_frame.setSourceMACAddress​(MacAddress.valueOf(dc.getMac()).toBytes());
                new_ethernet_frame.setEtherType(Ethernet.TYPE_IPV4);

                IPv4 new_ip_frame = new IPv4();
                new_ip_frame.setDestinationAddress​(zh.getIpv4());
                new_ip_frame.setSourceAddress​(dc.getIpv4());
                new_ip_frame.setProtocol(IPv4.PROTOCOL_UDP);
                new_ip_frame.setTtl​((byte) 127);
                new_ip_frame.setFlags((byte) 2);
                new_ip_frame.setIdentification((short) ThreadLocalRandom.current().nextInt(0, Short.MAX_VALUE));


                UDP new_udp_frame = new UDP();
                new_udp_frame.setSourcePort​(7447);
                new_udp_frame.setDestinationPort​(zh.getUdpPort());


                Packet p = new KeepAlivePacket();

                new_udp_frame.setPayload(p);
                new_ip_frame.setPayload(new_udp_frame);
                new_ethernet_frame.setPayload(new_ip_frame);

                this.zpService.sendPacketOut(null, PortNumber.portNumber(zh.getPortNumber()) , DeviceId.deviceId(zh.getDeviceId()), new_ethernet_frame.serialize());

            }
        }
    }



}
