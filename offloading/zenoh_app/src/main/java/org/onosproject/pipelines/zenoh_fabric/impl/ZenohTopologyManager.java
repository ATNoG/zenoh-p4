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
import org.onosproject.net.config.Config;
import org.onosproject.net.pi.model.PiActionId;
import org.onosproject.net.pi.model.PiActionParamId;
import org.onosproject.net.pi.model.PiMatchFieldId;
import org.onosproject.net.pi.runtime.PiAction;
import org.onosproject.net.pi.runtime.PiActionParam;
import org.onosproject.net.host.InterfaceIpAddress;
import org.onosproject.net.flow.criteria.PiCriterion;
import org.onosproject.net.config.NetworkConfigService;
import org.onosproject.net.link.LinkService;
import org.onosproject.net.link.LinkListener;
import org.onosproject.net.link.LinkEvent;
import org.apache.felix.scr.annotations.Service;
import org.onosproject.net.Link.State;
import static org.onosproject.pipelines.zenoh_fabric.impl.AppConstants.INITIAL_SETUP_DELAY;


import org.onosproject.net.Link;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.Queue;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

@Component(immediate = true, service = ZenohTopologyManagerService.class)
public class ZenohTopologyManager implements ZenohTopologyManagerService{



    private final Logger log = LoggerFactory.getLogger(getClass());

    //data structure with computed paths, list of Pairs where a pair is described by source and destination nodes


    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public DeviceService deviceService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public FlowRuleService flowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    private LinkService linkService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public MastershipService mastershipService;


    private UtilsComponent utilsComponent = new UtilsComponent();

    private Map<DeviceId, ArrayList<Link>> topology = new HashMap<>();
    private Map<Integer, ArrayList<Link>> multicastGroup_linksConfigured = new HashMap<>();


    private final LinkListener linkListener = new InternalLinkListener();

    private Map<DeviceId, Map<DeviceId, NodePath>> paths = new HashMap<>();

    @Activate
    protected void activate() {

        try{




            // Register listeners to be informed about device and host events.
            linkService.addListener(linkListener);
            // Schedule set up of existing devices. Needed when reloading the app.
            utilsComponent.scheduleTask(this::computeTopology, INITIAL_SETUP_DELAY);


        }catch(Exception e){
            log.error(e.toString());
        }
    }


    /**
     * Deactivates the processor by removing it.
     */
    @Deactivate
    protected void deactivate() {
        linkService.removeListener(linkListener);

        log.info("Stopped");
    }



    public class InternalLinkListener implements LinkListener {

        @Override
        public boolean isRelevant(LinkEvent event) {
            switch (event.type()) {
                case LINK_ADDED:
                case LINK_REMOVED:
                case LINK_UPDATED:
                    break;
                default:
                    // Ignore other events.
                    return false;
            }
            return true;
        }

        @Override
        public void event(LinkEvent event) {
            utilsComponent.getExecutorService().execute(() -> {
                    log.info("{} event!", event.type());

                    computeTopology();
                });
        }
    }


    private void computeTopology(){
        Iterator device_iterator = deviceService.getAvailableDevices().iterator();

        while(device_iterator.hasNext()){
            Device dev_next = (Device) device_iterator.next();
            DeviceId dev = dev_next.id();
            topology.put(dev, new ArrayList<>());

            Set<Link> links_device = linkService.getDeviceEgressLinks​(dev);
            log.info("device:" + dev.toString());
            log.info("Links:" + links_device.toString());

            for(Link l: links_device){
                if(l.state() == State.ACTIVE){
                    topology.get(dev).add(l);
                }
            }
        }

        runDijkstra();

    }


    private void runDijkstra(){
        for(DeviceId dev : topology.keySet()){
            Map<DeviceId, NodePath> path_mapping = new HashMap<>();
            for(DeviceId dev_tmp : topology.keySet()){
                if(dev_tmp != dev){
                    path_mapping.put(dev_tmp, new NodePath());
                }
            }

            path_mapping.put(dev, new NodePath());
            path_mapping.get(dev).setNodePath_cost(0);

            Set<DeviceId> finished = new HashSet<>();
            finished.add(dev);

            DeviceId next_dev = dev;
            Queue<DeviceId> devices_to_process = new LinkedList<>();
            devices_to_process.add(dev);
            finished.add(dev);


            while(devices_to_process.size() != 0){
                DeviceId dev_process = devices_to_process.remove();

                ArrayList<Link> dev_links = topology.get(dev_process);
                for(Link l : dev_links){
                    //  if node not processed
                    if(!finished.contains(l.dst().deviceId())){

                        DeviceId dstNode = l.dst().deviceId();
                        DeviceId srcNode = l.src().deviceId();

                        if(!devices_to_process.contains(dstNode)){
                            devices_to_process.add(dstNode);
                        }

                        if(path_mapping.get(srcNode).getNodePath_cost() + 1 <  path_mapping.get(dstNode).getNodePath_cost()){
                            path_mapping.get(dstNode).setNodePath_links(path_mapping.get(srcNode).copyNodePath());
                            path_mapping.get(dstNode).addLink(l);
                            path_mapping.get(dstNode).setNodePath_cost(path_mapping.get(dstNode).getNodePath_links().size());


                        }

                    }

                }


                finished.add(dev_process);


            }
            log.info("path from device: " + dev.toString() );
            log.info(path_mapping.toString());
            paths.put(dev, path_mapping);

        }
    }


    public Map<DeviceId, NodePath> getPaths(DeviceId srcDevice){
        return paths.get(srcDevice);
    }

    public ArrayList<Link> linksConfigured(int multicastGroup){
        return multicastGroup_linksConfigured.containsKey(multicastGroup) ? multicastGroup_linksConfigured.get(multicastGroup) : new ArrayList<>();
    }


    public void addLinkConfigured(int multicastGroup, Link l){
        if(multicastGroup_linksConfigured.containsKey(multicastGroup)){
            multicastGroup_linksConfigured.get(multicastGroup).add(l);
        }else{
            multicastGroup_linksConfigured.put(multicastGroup,new ArrayList<>());
            multicastGroup_linksConfigured.get(multicastGroup).add(l);

        }
    }
}
