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

import java.util.concurrent.ThreadLocalRandom;
import static java.util.stream.Collectors.toList;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


@Component(immediate = true, enabled = true)
public class L2Component{    
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public FlowRuleService flowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public MastershipService mastershipService;


    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public DeviceService deviceService;
    
    private ApplicationId appId;

    private UtilsComponent utilsComponent = new UtilsComponent();

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Activate
    protected void activate() {

        try{
            
            appId = coreService.getAppId("org.onosproject.zenoh_app");

            // Schedule set up of existing devices. Needed when reloading the app.
            utilsComponent.scheduleTask(this::setUpAllDevices, 90);
            
            
            log.info("*******L2 ACTIVATED****************");
        }catch(Exception e){
            log.error(e.toString());
        }
    }


    /**
     * Deactivates the processor by removing it.
     */
    @Deactivate
    protected void deactivate() {

        log.info("Stopped");
    }


    /**
     * Sets up everything necessary to support zenoh session packet processing.
     *
     * @param deviceId the device to set up
     */
    private void setUpDevice(DeviceId deviceId) {
    }

    
    

    private void L2RulesCase1(){
    
        log.info("L2RULES");
        
        DeviceId deviceId = DeviceId.deviceId("device:sw1");
        
        final PiCriterion filter1 = PiCriterion.builder()
                                        .matchExact(
                                            PiMatchFieldId.of("dstAddr"),
                                            IPv4.toIPv4AddressBytes​("10.0.1.11"))
                                        .build();

                                        
        final PiAction fw1 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:01").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 1))
                    .build();
        
        
        final PiCriterion filter2 = PiCriterion.builder()
                                    .matchExact(
                                        PiMatchFieldId.of("dstAddr"),
                                        IPv4.toIPv4AddressBytes​("10.0.1.12"))
                                    .build();

        
        final PiAction fw2 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:02").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 2))
                    .build();
                    
                    
        final PiCriterion filter3 = PiCriterion.builder()
                                        .matchExact(
                                            PiMatchFieldId.of("dstAddr"),
                                            IPv4.toIPv4AddressBytes​("10.0.1.13"))
                                        .build();

                                        
        final PiAction fw3 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:03").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 3))
                    .build();
        
        
        final PiCriterion filter4 = PiCriterion.builder()
                                    .matchExact(
                                        PiMatchFieldId.of("dstAddr"),
                                        IPv4.toIPv4AddressBytes​("10.0.1.14"))
                                    .build();

        
        final PiAction fw4 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:04").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 4))
                    .build();
                    
        final PiCriterion filter5 = PiCriterion.builder()
                                        .matchExact(
                                            PiMatchFieldId.of("dstAddr"),
                                            IPv4.toIPv4AddressBytes​("10.0.1.15"))
                                        .build();

                                        
        final PiAction fw5 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:05").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 5))
                    .build();
        
        
        
        
        final PiCriterion filter6 = PiCriterion.builder()
                                        .matchExact(
                                            PiMatchFieldId.of("dstAddr"),
                                            IPv4.toIPv4AddressBytes​("10.0.2.11"))
                                        .build();

                                        
        final PiAction fw6 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:0b").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 6))
                    .build();
        
        
        final PiCriterion filter7 = PiCriterion.builder()
                                    .matchExact(
                                        PiMatchFieldId.of("dstAddr"),
                                        IPv4.toIPv4AddressBytes​("10.0.2.12"))
                                    .build();

        
        final PiAction fw7 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:0c").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 7))
                    .build();
                    
                    
        final PiCriterion filter8 = PiCriterion.builder()
                                        .matchExact(
                                            PiMatchFieldId.of("dstAddr"),
                                            IPv4.toIPv4AddressBytes​("10.0.2.13"))
                                        .build();

                                        
        final PiAction fw8 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:0d").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 8))
                    .build();
        
        
        final PiCriterion filter9 = PiCriterion.builder()
                                    .matchExact(
                                        PiMatchFieldId.of("dstAddr"),
                                        IPv4.toIPv4AddressBytes​("10.0.2.14"))
                                    .build();

        
        final PiAction fw9 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:0e").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 9))
                    .build();
                    
        final PiCriterion filter10 = PiCriterion.builder()
                                        .matchExact(
                                            PiMatchFieldId.of("dstAddr"),
                                            IPv4.toIPv4AddressBytes​("10.0.2.15"))
                                        .build();

                                        
        final PiAction fw10 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:0f").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 10))
                    .build();
        
        
        
        final PiCriterion filtersw = PiCriterion.builder()
                                .matchExact(
                                    PiMatchFieldId.of("dstAddr"),
                                    IPv4.toIPv4AddressBytes​("10.0.3.10"))
                                .build();

                                        
        final PiAction fwsw = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:10").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 11))
                    .build();
        
        
        
        
        final String table = "FabricIngress.l2.l2_bridging";




        final FlowRule rule1 = Utils.buildFlowRule(
                deviceId, appId, table,
                filter1, fw1);

        final FlowRule rule2 = Utils.buildFlowRule(
                deviceId, appId, table,
                filter2, fw2);

        final FlowRule rule3 = Utils.buildFlowRule(
                deviceId, appId, table,
                filter3, fw3);


        final FlowRule rule4 = Utils.buildFlowRule(
                deviceId, appId, table,
                filter4, fw4);

        final FlowRule rule5 = Utils.buildFlowRule(
                deviceId, appId, table,
                filter5, fw5);

        final FlowRule rule6 = Utils.buildFlowRule(
                deviceId, appId, table,
                filter6, fw6);

        final FlowRule rule7 = Utils.buildFlowRule(
                deviceId, appId, table,
                filter7, fw7);

        final FlowRule rule8 = Utils.buildFlowRule(
                deviceId, appId, table,
                filter8, fw8);

        final FlowRule rule9 = Utils.buildFlowRule(
                deviceId, appId, table,
                filter9, fw9);
                
        final FlowRule rule10 = Utils.buildFlowRule(
                deviceId, appId, table,
                filter10, fw10);

        final FlowRule rulesw = Utils.buildFlowRule(
                deviceId, appId, table,
                filtersw, fwsw);
                
        


        flowRuleService.applyFlowRules(rule1, rule2, rule3,rule4, rule5, rule6,rule7, rule8, rule9,rule10, rulesw);


    }

    private void L2RulesCase2(){
    
        DeviceId deviceId_1 = DeviceId.deviceId("device:sw1");
        
        DeviceId deviceId_3 = DeviceId.deviceId("device:sw3");
        
        
        final PiCriterion filter1 = PiCriterion.builder()
                                        .matchExact(
                                            PiMatchFieldId.of("dstAddr"),
                                            IPv4.toIPv4AddressBytes​("10.0.1.11"))
                                        .build();

                                        
        final PiAction fw1 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:01").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 1))
                    .build();
        
        
        final PiCriterion filter2 = PiCriterion.builder()
                                    .matchExact(
                                        PiMatchFieldId.of("dstAddr"),
                                        IPv4.toIPv4AddressBytes​("10.0.1.12"))
                                    .build();

        
        final PiAction fw2 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:02").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 2))
                    .build();
                    
                    
        final PiCriterion filter3 = PiCriterion.builder()
                                        .matchExact(
                                            PiMatchFieldId.of("dstAddr"),
                                            IPv4.toIPv4AddressBytes​("10.0.1.13"))
                                        .build();

                                        
        final PiAction fw3 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:03").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 3))
                    .build();
        
        
        final PiCriterion filter4 = PiCriterion.builder()
                                    .matchExact(
                                        PiMatchFieldId.of("dstAddr"),
                                        IPv4.toIPv4AddressBytes​("10.0.1.14"))
                                    .build();

        
        final PiAction fw4 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:04").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 4))
                    .build();
                    
        final PiCriterion filter5 = PiCriterion.builder()
                                        .matchExact(
                                            PiMatchFieldId.of("dstAddr"),
                                            IPv4.toIPv4AddressBytes​("10.0.1.15"))
                                        .build();

                                        
        final PiAction fw5 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:05").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 5))
                    .build();
        
        
        
        
        final PiCriterion filter6 = PiCriterion.builder()
                                        .matchExact(
                                            PiMatchFieldId.of("dstAddr"),
                                            IPv4.toIPv4AddressBytes​("10.0.2.11"))
                                        .build();

                                        
        final PiAction fw6 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:0b").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 1))
                    .build();
        
        
        final PiCriterion filter7 = PiCriterion.builder()
                                    .matchExact(
                                        PiMatchFieldId.of("dstAddr"),
                                        IPv4.toIPv4AddressBytes​("10.0.2.12"))
                                    .build();

        
        final PiAction fw7 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:0c").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 2))
                    .build();
                    
                    
        final PiCriterion filter8 = PiCriterion.builder()
                                        .matchExact(
                                            PiMatchFieldId.of("dstAddr"),
                                            IPv4.toIPv4AddressBytes​("10.0.2.13"))
                                        .build();

                                        
        final PiAction fw8 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:0d").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 3))
                    .build();
        
        
        final PiCriterion filter9 = PiCriterion.builder()
                                    .matchExact(
                                        PiMatchFieldId.of("dstAddr"),
                                        IPv4.toIPv4AddressBytes​("10.0.2.14"))
                                    .build();

        
        final PiAction fw9 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:0e").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 4))
                    .build();
                    
        final PiCriterion filter10 = PiCriterion.builder()
                                        .matchExact(
                                            PiMatchFieldId.of("dstAddr"),
                                            IPv4.toIPv4AddressBytes​("10.0.2.15"))
                                        .build();

                                        
        final PiAction fw10 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:0f").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 5))
                    .build();
        
        
        final PiCriterion filtersw = PiCriterion.builder()
                                .matchExact(
                                    PiMatchFieldId.of("dstAddr"),
                                    IPv4.toIPv4AddressBytes​("10.0.3.10"))
                                .build();
        

        final PiAction fw_sw3 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:aa:00:00:00:01").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 6))
                    .build();
        
        final PiAction fw_sw1_sw3 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:aa:00:00:00:03").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 7))
                    .build();
        
        
        
        final PiAction fw_sw1 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:10").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 6))
                    .build();
        
        
        
        
        final String table = "FabricIngress.l2.l2_bridging";




        final FlowRule rule1 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter1, fw1);

        final FlowRule rule2 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter2, fw2);

        final FlowRule rule3 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter3, fw3);


        final FlowRule rule4 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter4, fw4);

        final FlowRule rule5 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter5, fw5);

        final FlowRule rule6 = Utils.buildFlowRule(
                deviceId_3, appId, table,
                filter6, fw6);

        final FlowRule rule7 = Utils.buildFlowRule(
                deviceId_3, appId, table,
                filter7, fw7);

        final FlowRule rule8 = Utils.buildFlowRule(
                deviceId_3, appId, table,
                filter8, fw8);

        final FlowRule rule9 = Utils.buildFlowRule(
                deviceId_3, appId, table,
                filter9, fw9);
                
        final FlowRule rule10 = Utils.buildFlowRule(
                deviceId_3, appId, table,
                filter10, fw10);

        final FlowRule rulesw1 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filtersw, fw_sw1);
                
        
        final FlowRule rulesw3 = Utils.buildFlowRule(
                deviceId_3, appId, table,
                filtersw, fw_sw3);
                
        final FlowRule rulesw1_h11 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter6, fw_sw1_sw3);
        final FlowRule rulesw1_h12 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter7, fw_sw1_sw3);
        final FlowRule rulesw1_h13 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter8, fw_sw1_sw3);
        final FlowRule rulesw1_h14 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter9, fw_sw1_sw3);
        final FlowRule rulesw1_h15 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter10, fw_sw1_sw3);
         
        
        

        flowRuleService.applyFlowRules(rule1, rule2, rule3,rule4, rule5, rule6,rule7, rule8, rule9,rule10, rulesw1, rulesw3, rulesw1_h11, rulesw1_h12,rulesw1_h13,rulesw1_h14,rulesw1_h15);


    }

    private void L2RulesCase3(){
    
        DeviceId deviceId_1 = DeviceId.deviceId("device:sw1");
        
        DeviceId deviceId_3 = DeviceId.deviceId("device:sw3");
        
        DeviceId deviceId_2 = DeviceId.deviceId("device:sw2");
        
        
        
        final PiCriterion filter1 = PiCriterion.builder()
                                        .matchExact(
                                            PiMatchFieldId.of("dstAddr"),
                                            IPv4.toIPv4AddressBytes​("10.0.1.11"))
                                        .build();

                                        
        final PiAction fw1 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:01").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 1))
                    .build();
        
        
        final PiCriterion filter2 = PiCriterion.builder()
                                    .matchExact(
                                        PiMatchFieldId.of("dstAddr"),
                                        IPv4.toIPv4AddressBytes​("10.0.1.12"))
                                    .build();

        
        final PiAction fw2 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:02").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 2))
                    .build();
                    
                    
        final PiCriterion filter3 = PiCriterion.builder()
                                        .matchExact(
                                            PiMatchFieldId.of("dstAddr"),
                                            IPv4.toIPv4AddressBytes​("10.0.1.13"))
                                        .build();

                                        
        final PiAction fw3 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:03").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 3))
                    .build();
        
        
        final PiCriterion filter4 = PiCriterion.builder()
                                    .matchExact(
                                        PiMatchFieldId.of("dstAddr"),
                                        IPv4.toIPv4AddressBytes​("10.0.1.14"))
                                    .build();

        
        final PiAction fw4 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:04").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 4))
                    .build();
                    
        final PiCriterion filter5 = PiCriterion.builder()
                                        .matchExact(
                                            PiMatchFieldId.of("dstAddr"),
                                            IPv4.toIPv4AddressBytes​("10.0.1.15"))
                                        .build();

                                        
        final PiAction fw5 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:05").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 5))
                    .build();
        
        
        
        
        final PiCriterion filter6 = PiCriterion.builder()
                                        .matchExact(
                                            PiMatchFieldId.of("dstAddr"),
                                            IPv4.toIPv4AddressBytes​("10.0.2.11"))
                                        .build();

                                        
        final PiAction fw6 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:0b").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 1))
                    .build();
        
        
        final PiCriterion filter7 = PiCriterion.builder()
                                    .matchExact(
                                        PiMatchFieldId.of("dstAddr"),
                                        IPv4.toIPv4AddressBytes​("10.0.2.12"))
                                    .build();

        
        final PiAction fw7 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:0c").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 2))
                    .build();
                    
                    
        final PiCriterion filter8 = PiCriterion.builder()
                                        .matchExact(
                                            PiMatchFieldId.of("dstAddr"),
                                            IPv4.toIPv4AddressBytes​("10.0.2.13"))
                                        .build();

                                        
        final PiAction fw8 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:0d").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 3))
                    .build();
        
        
        final PiCriterion filter9 = PiCriterion.builder()
                                    .matchExact(
                                        PiMatchFieldId.of("dstAddr"),
                                        IPv4.toIPv4AddressBytes​("10.0.2.14"))
                                    .build();

        
        final PiAction fw9 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:0e").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 4))
                    .build();
                    
        final PiCriterion filter10 = PiCriterion.builder()
                                        .matchExact(
                                            PiMatchFieldId.of("dstAddr"),
                                            IPv4.toIPv4AddressBytes​("10.0.2.15"))
                                        .build();

                                        
        final PiAction fw10 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:0f").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 5))
                    .build();
        
        
        final PiCriterion filtersw = PiCriterion.builder()
                                .matchExact(
                                    PiMatchFieldId.of("dstAddr"),
                                    IPv4.toIPv4AddressBytes​("10.0.3.10"))
                                .build();
        
        
        
        final PiAction fw_sw1_sw2 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:aa:00:00:00:02").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 7))
                    .build();
        
        
        final PiAction fw_sw3_sw2 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:aa:00:00:00:02").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 6))
                    .build();
        
        
        final PiAction fw_sw1 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:10").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 6))
                    .build();
        
        final PiAction fw_sw2 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:aa:00:00:00:01").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 1))
                    .build();
        
        final PiAction fw_sw2_sw3 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:aa:00:00:00:03").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 2))
                    .build();
        
        
        
        
        
        final String table = "FabricIngress.l2.l2_bridging";




        final FlowRule rule1 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter1, fw1);

        final FlowRule rule2 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter2, fw2);

        final FlowRule rule3 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter3, fw3);


        final FlowRule rule4 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter4, fw4);

        final FlowRule rule5 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter5, fw5);

        final FlowRule rule6 = Utils.buildFlowRule(
                deviceId_3, appId, table,
                filter6, fw6);

        final FlowRule rule7 = Utils.buildFlowRule(
                deviceId_3, appId, table,
                filter7, fw7);

        final FlowRule rule8 = Utils.buildFlowRule(
                deviceId_3, appId, table,
                filter8, fw8);

        final FlowRule rule9 = Utils.buildFlowRule(
                deviceId_3, appId, table,
                filter9, fw9);
                
        final FlowRule rule10 = Utils.buildFlowRule(
                deviceId_3, appId, table,
                filter10, fw10);

        final FlowRule rulesw1 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filtersw, fw_sw1);
                
        
        final FlowRule rulesw3 = Utils.buildFlowRule(
                deviceId_3, appId, table,
                filtersw, fw_sw3_sw2);
                
                
        final FlowRule rulesw2 = Utils.buildFlowRule(
                deviceId_2, appId, table,
                filtersw, fw_sw2);
                
        final FlowRule rulesw2_h11 = Utils.buildFlowRule(
                deviceId_2, appId, table,
                filter6, fw_sw2_sw3);
        final FlowRule rulesw2_h12 = Utils.buildFlowRule(
                deviceId_2, appId, table,
                filter7, fw_sw2_sw3);
        final FlowRule rulesw2_h13 = Utils.buildFlowRule(
                deviceId_2, appId, table,
                filter8, fw_sw2_sw3);
        final FlowRule rulesw2_h14 = Utils.buildFlowRule(
                deviceId_2, appId, table,
                filter9, fw_sw2_sw3);
        final FlowRule rulesw2_h15 = Utils.buildFlowRule(
                deviceId_2, appId, table,
                filter10, fw_sw2_sw3);
        
                
        final FlowRule rulesw1_h11 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter6, fw_sw1_sw2);
        final FlowRule rulesw1_h12 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter7, fw_sw1_sw2);
        final FlowRule rulesw1_h13 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter8, fw_sw1_sw2);
        final FlowRule rulesw1_h14 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter9, fw_sw1_sw2);
        final FlowRule rulesw1_h15 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter10, fw_sw1_sw2);
         
         
        

        flowRuleService.applyFlowRules(rule1, rule2, rule3,rule4, rule5, rule6,rule7, rule8, rule9,rule10, rulesw1, rulesw3, rulesw2,rulesw2_h11,rulesw2_h12,rulesw2_h13,rulesw2_h14,rulesw2_h15, rulesw1_h11,rulesw1_h12,rulesw1_h13,rulesw1_h14,rulesw1_h15);


    }


    
    private void L2RulesCase4(){
    
        DeviceId deviceId_1 = DeviceId.deviceId("device:sw1");
        
        DeviceId deviceId_3 = DeviceId.deviceId("device:sw3");
        
        DeviceId deviceId_2 = DeviceId.deviceId("device:sw2");
        
        DeviceId deviceId_4 = DeviceId.deviceId("device:sw4");
        
        
        
        final PiCriterion filter1 = PiCriterion.builder()
                                        .matchExact(
                                            PiMatchFieldId.of("dstAddr"),
                                            IPv4.toIPv4AddressBytes​("10.0.1.11"))
                                        .build();

                                        
        final PiAction fw1 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:01").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 1))
                    .build();
        
        
        final PiCriterion filter2 = PiCriterion.builder()
                                    .matchExact(
                                        PiMatchFieldId.of("dstAddr"),
                                        IPv4.toIPv4AddressBytes​("10.0.1.12"))
                                    .build();

        
        final PiAction fw2 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:02").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 2))
                    .build();
                    
                    
        final PiCriterion filter3 = PiCriterion.builder()
                                        .matchExact(
                                            PiMatchFieldId.of("dstAddr"),
                                            IPv4.toIPv4AddressBytes​("10.0.1.13"))
                                        .build();

                                        
        final PiAction fw3 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:03").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 3))
                    .build();
        
        
        final PiCriterion filter4 = PiCriterion.builder()
                                    .matchExact(
                                        PiMatchFieldId.of("dstAddr"),
                                        IPv4.toIPv4AddressBytes​("10.0.1.14"))
                                    .build();

        
        final PiAction fw4 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:04").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 4))
                    .build();
                    
        final PiCriterion filter5 = PiCriterion.builder()
                                        .matchExact(
                                            PiMatchFieldId.of("dstAddr"),
                                            IPv4.toIPv4AddressBytes​("10.0.1.15"))
                                        .build();

                                        
        final PiAction fw5 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:05").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 5))
                    .build();
        
        
        
        
        final PiCriterion filter6 = PiCriterion.builder()
                                        .matchExact(
                                            PiMatchFieldId.of("dstAddr"),
                                            IPv4.toIPv4AddressBytes​("10.0.2.11"))
                                        .build();

                                        
        final PiAction fw6 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:0b").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 1))
                    .build();
        
        
        final PiCriterion filter7 = PiCriterion.builder()
                                    .matchExact(
                                        PiMatchFieldId.of("dstAddr"),
                                        IPv4.toIPv4AddressBytes​("10.0.2.12"))
                                    .build();

        
        final PiAction fw7 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:0c").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 2))
                    .build();
                    
                    
        final PiCriterion filter8 = PiCriterion.builder()
                                        .matchExact(
                                            PiMatchFieldId.of("dstAddr"),
                                            IPv4.toIPv4AddressBytes​("10.0.2.13"))
                                        .build();

                                        
        final PiAction fw8 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:0d").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 3))
                    .build();
        
        
        final PiCriterion filter9 = PiCriterion.builder()
                                    .matchExact(
                                        PiMatchFieldId.of("dstAddr"),
                                        IPv4.toIPv4AddressBytes​("10.0.2.14"))
                                    .build();

        
        final PiAction fw9 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:0e").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 4))
                    .build();
                    
        final PiCriterion filter10 = PiCriterion.builder()
                                        .matchExact(
                                            PiMatchFieldId.of("dstAddr"),
                                            IPv4.toIPv4AddressBytes​("10.0.2.15"))
                                        .build();

                                        
        final PiAction fw10 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:0f").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 5))
                    .build();
        
        
        final PiCriterion filtersw = PiCriterion.builder()
                                .matchExact(
                                    PiMatchFieldId.of("dstAddr"),
                                    IPv4.toIPv4AddressBytes​("10.0.3.10"))
                                .build();
        
        

        final PiAction fw_sw3 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:aa:00:00:00:04").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 6))
                    .build();
        
        
        final PiAction fw_sw1 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:10").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 6))
                    .build();
        
        final PiAction fw_sw2 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:aa:00:00:00:01").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 1))
                    .build();
    
        
        final PiAction fw_sw4_sw3 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:aa:00:00:00:03").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 2))
                    .build();
        
        final PiAction fw_sw2_sw4 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:aa:00:00:00:04").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 2))
                    .build();
                    
        
        final PiAction fw_sw4_sw2 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:aa:00:00:00:02").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 1))
                    .build();
                    
        
        
        final PiAction fw_sw1_sw2 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:aa:00:00:00:02").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 7))
                    .build();
        
        
        
        
        
        
        final String table = "FabricIngress.l2.l2_bridging";




        final FlowRule rule1 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter1, fw1);

        final FlowRule rule2 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter2, fw2);

        final FlowRule rule3 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter3, fw3);


        final FlowRule rule4 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter4, fw4);

        final FlowRule rule5 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter5, fw5);

        final FlowRule rule6 = Utils.buildFlowRule(
                deviceId_3, appId, table,
                filter6, fw6);

        final FlowRule rule7 = Utils.buildFlowRule(
                deviceId_3, appId, table,
                filter7, fw7);

        final FlowRule rule8 = Utils.buildFlowRule(
                deviceId_3, appId, table,
                filter8, fw8);

        final FlowRule rule9 = Utils.buildFlowRule(
                deviceId_3, appId, table,
                filter9, fw9);
                
        final FlowRule rule10 = Utils.buildFlowRule(
                deviceId_3, appId, table,
                filter10, fw10);

        final FlowRule rulesw1 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filtersw, fw_sw1);
                
        
        final FlowRule rulesw3 = Utils.buildFlowRule(
                deviceId_3, appId, table,
                filtersw, fw_sw3);
                
                
        final FlowRule rulesw2 = Utils.buildFlowRule(
                deviceId_2, appId, table,
                filtersw, fw_sw2);
                
                
        
                
        final FlowRule rulesw2_h11 = Utils.buildFlowRule(
                deviceId_2, appId, table,
                filter6, fw_sw2_sw4);
        final FlowRule rulesw2_h12 = Utils.buildFlowRule(
                deviceId_2, appId, table,
                filter7, fw_sw2_sw4);
        final FlowRule rulesw2_h13 = Utils.buildFlowRule(
                deviceId_2, appId, table,
                filter8, fw_sw2_sw4);
        final FlowRule rulesw2_h14 = Utils.buildFlowRule(
                deviceId_2, appId, table,
                filter9, fw_sw2_sw4);
        final FlowRule rulesw2_h15 = Utils.buildFlowRule(
                deviceId_2, appId, table,
                filter10, fw_sw2_sw4);
        
                
        final FlowRule rulesw1_h11 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter6, fw_sw1_sw2);
        final FlowRule rulesw1_h12 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter7, fw_sw1_sw2);
        final FlowRule rulesw1_h13 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter8, fw_sw1_sw2);
        final FlowRule rulesw1_h14 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter9, fw_sw1_sw2);
        final FlowRule rulesw1_h15 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter10, fw_sw1_sw2);
                
        
           
        final FlowRule rulesw4 = Utils.buildFlowRule(
                deviceId_4, appId, table,
                filtersw, fw_sw4_sw2);
            
        final FlowRule rulesw4_h11 = Utils.buildFlowRule(
                deviceId_4, appId, table,
                filter6, fw_sw4_sw3);
        final FlowRule rulesw4_h12 = Utils.buildFlowRule(
                deviceId_4, appId, table,
                filter7, fw_sw4_sw3);
        final FlowRule rulesw4_h13 = Utils.buildFlowRule(
                deviceId_4, appId, table,
                filter8, fw_sw4_sw3);
        final FlowRule rulesw4_h14 = Utils.buildFlowRule(
                deviceId_4, appId, table,
                filter9, fw_sw4_sw3);
        final FlowRule rulesw4_h15 = Utils.buildFlowRule(
                deviceId_4, appId, table,
                filter10, fw_sw4_sw3);
         
         
        

        flowRuleService.applyFlowRules(rule1, rule2, rule3,rule4, rule5, rule6,rule7, rule8, rule9,rule10, rulesw1, rulesw3, rulesw2,rulesw2_h11,rulesw2_h12,rulesw2_h13,rulesw2_h14,rulesw2_h15, rulesw1_h11,rulesw1_h12,rulesw1_h13,rulesw1_h14,rulesw1_h15, rulesw4,rulesw4_h11,rulesw4_h12,rulesw4_h13,rulesw4_h14,rulesw4_h15);


    }
    
    
    private void L2RulesCase5(){
    
        DeviceId deviceId_1 = DeviceId.deviceId("device:sw1");
        
        DeviceId deviceId_3 = DeviceId.deviceId("device:sw3");
        
        DeviceId deviceId_2 = DeviceId.deviceId("device:sw2");
        
        DeviceId deviceId_4 = DeviceId.deviceId("device:sw4");
        
        DeviceId deviceId_5 = DeviceId.deviceId("device:sw5");
        
        
        
        final PiCriterion filter1 = PiCriterion.builder()
                                        .matchExact(
                                            PiMatchFieldId.of("dstAddr"),
                                            IPv4.toIPv4AddressBytes​("10.0.1.11"))
                                        .build();

                                        
        final PiAction fw1 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:01").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 1))
                    .build();
        
        
        final PiCriterion filter2 = PiCriterion.builder()
                                    .matchExact(
                                        PiMatchFieldId.of("dstAddr"),
                                        IPv4.toIPv4AddressBytes​("10.0.1.12"))
                                    .build();

        
        final PiAction fw2 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:02").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 2))
                    .build();
                    
                    
        final PiCriterion filter3 = PiCriterion.builder()
                                        .matchExact(
                                            PiMatchFieldId.of("dstAddr"),
                                            IPv4.toIPv4AddressBytes​("10.0.1.13"))
                                        .build();

                                        
        final PiAction fw3 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:03").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 3))
                    .build();
        
        
        final PiCriterion filter4 = PiCriterion.builder()
                                    .matchExact(
                                        PiMatchFieldId.of("dstAddr"),
                                        IPv4.toIPv4AddressBytes​("10.0.1.14"))
                                    .build();

        
        final PiAction fw4 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:04").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 4))
                    .build();
                    
        final PiCriterion filter5 = PiCriterion.builder()
                                        .matchExact(
                                            PiMatchFieldId.of("dstAddr"),
                                            IPv4.toIPv4AddressBytes​("10.0.1.15"))
                                        .build();

                                        
        final PiAction fw5 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:05").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 5))
                    .build();
        
        
        
        
        final PiCriterion filter6 = PiCriterion.builder()
                                        .matchExact(
                                            PiMatchFieldId.of("dstAddr"),
                                            IPv4.toIPv4AddressBytes​("10.0.2.11"))
                                        .build();

                                        
        final PiAction fw6 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:0b").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 1))
                    .build();
        
        
        final PiCriterion filter7 = PiCriterion.builder()
                                    .matchExact(
                                        PiMatchFieldId.of("dstAddr"),
                                        IPv4.toIPv4AddressBytes​("10.0.2.12"))
                                    .build();

        
        final PiAction fw7 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:0c").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 2))
                    .build();
                    
                    
        final PiCriterion filter8 = PiCriterion.builder()
                                        .matchExact(
                                            PiMatchFieldId.of("dstAddr"),
                                            IPv4.toIPv4AddressBytes​("10.0.2.13"))
                                        .build();

                                        
        final PiAction fw8 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:0d").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 3))
                    .build();
        
        
        final PiCriterion filter9 = PiCriterion.builder()
                                    .matchExact(
                                        PiMatchFieldId.of("dstAddr"),
                                        IPv4.toIPv4AddressBytes​("10.0.2.14"))
                                    .build();

        
        final PiAction fw9 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:0e").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 4))
                    .build();
                    
        final PiCriterion filter10 = PiCriterion.builder()
                                        .matchExact(
                                            PiMatchFieldId.of("dstAddr"),
                                            IPv4.toIPv4AddressBytes​("10.0.2.15"))
                                        .build();

                                        
        final PiAction fw10 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:0f").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 5))
                    .build();
        
        
        final PiCriterion filtersw = PiCriterion.builder()
                                .matchExact(
                                    PiMatchFieldId.of("dstAddr"),
                                    IPv4.toIPv4AddressBytes​("10.0.3.10"))
                                .build();
        
        
        

        final PiAction fw_sw3 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:aa:00:00:00:05").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 6))
                    .build();
        
        
        final PiAction fw_sw1 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:00:00:00:00:10").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 6))
                    .build();
        
        final PiAction fw_sw2 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:aa:00:00:00:01").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 1))
                    .build();
    
        
        final PiAction fw_sw5_sw3 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:aa:00:00:00:03").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 2))
                    .build();
        
        final PiAction fw_sw4_sw5 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:aa:00:00:00:05").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 2))
                    .build();
        
        final PiAction fw_sw5_sw4 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:aa:00:00:00:04").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 1))
                    .build();
        
        
        
        final PiAction fw_sw2_sw4 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:aa:00:00:00:04").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 2))
                    .build();
                    
        
        final PiAction fw_sw4_sw2 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:aa:00:00:00:02").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 1))
                    .build();
                    
        
        
        final PiAction fw_sw1_sw2 = PiAction.builder()
                    .withId(PiActionId.of("FabricIngress.l2.bridging"))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("dstMac"), MacAddress.valueOf("00:aa:00:00:00:02").toBytes() ))
                    .withParameter(new PiActionParam(
                        PiActionParamId.of("port"), 7))
                    .build();
        
        
        
        
        
        
        final String table = "FabricIngress.l2.l2_bridging";




        final FlowRule rule1 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter1, fw1);

        final FlowRule rule2 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter2, fw2);

        final FlowRule rule3 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter3, fw3);


        final FlowRule rule4 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter4, fw4);

        final FlowRule rule5 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter5, fw5);

        final FlowRule rule6 = Utils.buildFlowRule(
                deviceId_3, appId, table,
                filter6, fw6);

        final FlowRule rule7 = Utils.buildFlowRule(
                deviceId_3, appId, table,
                filter7, fw7);

        final FlowRule rule8 = Utils.buildFlowRule(
                deviceId_3, appId, table,
                filter8, fw8);

        final FlowRule rule9 = Utils.buildFlowRule(
                deviceId_3, appId, table,
                filter9, fw9);
                
        final FlowRule rule10 = Utils.buildFlowRule(
                deviceId_3, appId, table,
                filter10, fw10);

        final FlowRule rulesw1 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filtersw, fw_sw1);
                
        
        final FlowRule rulesw3 = Utils.buildFlowRule(
                deviceId_3, appId, table,
                filtersw, fw_sw3);
                
                
        final FlowRule rulesw2 = Utils.buildFlowRule(
                deviceId_2, appId, table,
                filtersw, fw_sw2);
                
                
        
                
        final FlowRule rulesw2_h11 = Utils.buildFlowRule(
                deviceId_2, appId, table,
                filter6, fw_sw2_sw4);
        final FlowRule rulesw2_h12 = Utils.buildFlowRule(
                deviceId_2, appId, table,
                filter7, fw_sw2_sw4);
        final FlowRule rulesw2_h13 = Utils.buildFlowRule(
                deviceId_2, appId, table,
                filter8, fw_sw2_sw4);
        final FlowRule rulesw2_h14 = Utils.buildFlowRule(
                deviceId_2, appId, table,
                filter9, fw_sw2_sw4);
        final FlowRule rulesw2_h15 = Utils.buildFlowRule(
                deviceId_2, appId, table,
                filter10, fw_sw2_sw4);
        
                
        final FlowRule rulesw1_h11 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter6, fw_sw1_sw2);
        final FlowRule rulesw1_h12 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter7, fw_sw1_sw2);
        final FlowRule rulesw1_h13 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter8, fw_sw1_sw2);
        final FlowRule rulesw1_h14 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter9, fw_sw1_sw2);
        final FlowRule rulesw1_h15 = Utils.buildFlowRule(
                deviceId_1, appId, table,
                filter10, fw_sw1_sw2);
                
        
           
        final FlowRule rulesw4 = Utils.buildFlowRule(
                deviceId_4, appId, table,
                filtersw, fw_sw4_sw2);
            
        final FlowRule rulesw4_h11 = Utils.buildFlowRule(
                deviceId_4, appId, table,
                filter6, fw_sw4_sw5);
        final FlowRule rulesw4_h12 = Utils.buildFlowRule(
                deviceId_4, appId, table,
                filter7, fw_sw4_sw5);
        final FlowRule rulesw4_h13 = Utils.buildFlowRule(
                deviceId_4, appId, table,
                filter8, fw_sw4_sw5);
        final FlowRule rulesw4_h14 = Utils.buildFlowRule(
                deviceId_4, appId, table,
                filter9, fw_sw4_sw5);
        final FlowRule rulesw4_h15 = Utils.buildFlowRule(
                deviceId_4, appId, table,
                filter10, fw_sw4_sw5);
         
         
        
        final FlowRule rulesw5 = Utils.buildFlowRule(
                deviceId_5, appId, table,
                filtersw, fw_sw5_sw4);
            
        final FlowRule rulesw5_h11 = Utils.buildFlowRule(
                deviceId_5, appId, table,
                filter6, fw_sw5_sw3);
        final FlowRule rulesw5_h12 = Utils.buildFlowRule(
                deviceId_5, appId, table,
                filter7, fw_sw5_sw3);
        final FlowRule rulesw5_h13 = Utils.buildFlowRule(
                deviceId_5, appId, table,
                filter8, fw_sw5_sw3);
        final FlowRule rulesw5_h14 = Utils.buildFlowRule(
                deviceId_5, appId, table,
                filter9, fw_sw5_sw3);
        final FlowRule rulesw5_h15 = Utils.buildFlowRule(
                deviceId_5, appId, table,
                filter10, fw_sw5_sw3);
         
         
        


        flowRuleService.applyFlowRules(rule1, rule2, rule3,rule4, rule5, rule6,rule7, rule8, rule9,rule10, rulesw1, rulesw3, rulesw2,rulesw2_h11,rulesw2_h12,rulesw2_h13,rulesw2_h14,rulesw2_h15, rulesw1_h11,rulesw1_h12,rulesw1_h13,rulesw1_h14,rulesw1_h15, rulesw4,rulesw4_h11,rulesw4_h12,rulesw4_h13,rulesw4_h14,rulesw4_h15,rulesw5,rulesw5_h11,rulesw5_h12,rulesw5_h13,rulesw5_h14,rulesw5_h15);


    }


    /**
     * Sets up L2 bridging on all devices known by ONOS and for which this ONOS
     * node instance is currently master.
     * <p>
     * This method is called at component activation.
     */
    private void setUpAllDevices() {
        
    
        log.info("*******L2 setup****************");
        //L2RulesCase1();
        //L2RulesCase2();
        //L2RulesCase3();
        //L2RulesCase4();
        L2RulesCase5();
        
    }
}
