/*
 * Copyright 2019-present Open Networking Foundation
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

//import org.onosproject.pipelines.zenoh_fabric.impl.ZenohService;
import org.onlab.packet.MacAddress;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.mastership.MastershipService;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Device;
import org.onosproject.net.Host;
import org.onosproject.net.PortNumber;
import org.onosproject.net.config.NetworkConfigService;
import org.onosproject.net.device.DeviceEvent;
import org.onosproject.net.device.DeviceListener;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.criteria.PiCriterion;
import org.onosproject.net.group.GroupDescription;
import org.onosproject.net.group.GroupService;
import org.onosproject.net.host.HostEvent;
import org.onosproject.net.host.HostListener;
import org.onosproject.net.host.HostService;
import org.onosproject.net.intf.Interface;
import org.onosproject.net.intf.InterfaceService;
import org.onosproject.net.pi.model.PiActionId;
import org.onosproject.net.pi.model.PiActionParamId;
import org.onosproject.net.pi.model.PiMatchFieldId;
import org.onosproject.net.pi.runtime.PiAction;
import org.onosproject.net.pi.runtime.PiActionParam;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.onosproject.rest.AbstractWebResource;
import java.net.InetAddress;
import java.math.BigInteger;
import java.util.Set;
import java.util.stream.Collectors;
import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.ws.rs.POST;
import javax.ws.rs.GET;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.lang.StringBuffer;
import java.util.List;

/**
 * App component that configures firewall.
 */

@Path("/")
public class ZenohRestAPI extends AbstractWebResource{


    private final Logger log = LoggerFactory.getLogger(getClass());

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected FlowRuleService flowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceService deviceService;


    //--------------------------------------------------------------------------
    // ONOS CORE SERVICE BINDING
    //
    // These variables are set by the Karaf runtime environment before calling
    // the activate() method.
    //--------------------------------------------------------------------------

    @GET
    @Path("/191/ok")
    public Response deleteIntent(@PathParam("appId") String appId,
                                 @PathParam("key") String keyString) {

        log.info("REST API CALL OK");

        return Response.status(200).build();
    }


    /*
    @POST
    @Path("/firewall")
    @Produces(MediaType.TEXT_PLAIN)
    public Response addRule(InputStream stream) throws IOException {

        String option="";
        InputStreamReader isReader = new InputStreamReader(stream);
        //Creating a BufferedReader object
        BufferedReader reader = new BufferedReader(isReader);
        StringBuffer sb = new StringBuffer();
        String str;
        while((str = reader.readLine())!= null){
            sb.append(str);
        }
        JSONObject jsonObject = new JSONObject(sb.toString());


        log.info(jsonObject.toString());

        boolean added =  get(FirewallService.class).add_delete_Rule(
                                                            jsonObject.has("srcAddr")   ? jsonObject.get("srcAddr").toString()     : "*",
                                                            jsonObject.has("dstAddr")   ? jsonObject.get("dstAddr").toString()     : "*",
                                                            jsonObject.has("protocol")  ? jsonObject.get("protocol").toString()    : "any",
                                                            jsonObject.has("srcPort")   ? jsonObject.get("srcPort").toString()     : "*",
                                                            jsonObject.has("dstPort")   ? jsonObject.get("dstPort").toString()     : "*",
                                                            jsonObject.get("deviceId").toString(), jsonObject.get("dir").toString(), 0);

        if(added){
            return Response.status(200).build();
        }else{
            return Response.status(500).build();
        }

    }

    @DELETE
    @Path("/firewall")
    @Produces(MediaType.TEXT_PLAIN)
    public Response deleteRule(InputStream stream) throws IOException {

        String option="";
        InputStreamReader isReader = new InputStreamReader(stream);
        //Creating a BufferedReader object
        BufferedReader reader = new BufferedReader(isReader);
        StringBuffer sb = new StringBuffer();
        String str;
        while((str = reader.readLine())!= null){
            sb.append(str);
        }
        JSONObject jsonObject = new JSONObject(sb.toString());


        log.info(jsonObject.toString());

        boolean added =  get(FirewallService.class).add_delete_Rule(jsonObject.has("srcAddr")       ? jsonObject.get("srcAddr").toString()        : "*",
                                                                    jsonObject.has("dstAddr")       ? jsonObject.get("dstAddr").toString()        : "*",
                                                                    jsonObject.has("protocol")  ? jsonObject.get("protocol").toString()   : "*",
                                                                    jsonObject.has("srcPort")   ? jsonObject.get("srcPort").toString()    : "*",
                                                                    jsonObject.has("dstPort")   ? jsonObject.get("dstPort").toString()    : "*",
                                                                    jsonObject.get("deviceId").toString(), jsonObject.get("dir").toString(), 1);

        if(added){
            return Response.status(200).build();
        }else{
            return Response.status(500).build();
        }

    }

    @GET
    @Path("/firewall/rules")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getRules() throws IOException {

        List<FirewallRule> listRules = get(FirewallService.class).getRules();

        String jsonString = "{ \"rules\" : [ ";

        for(FirewallRule fr : listRules){
            JSONObject jsonObject = new JSONObject(fr);
            String jsonRule = jsonObject.toString();

            log.info(jsonRule);

            jsonString += jsonRule + ",";
        }


        jsonString =  jsonString.substring(0, jsonString.length() - 1) +  "]}";
        log.info(jsonString);

        return Response.ok(jsonString).build();
    }


    @GET
    @Path("/firewall/devices")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getDevices(){

        List<String> listDevices = get(FirewallService.class).getPipelineDevices();

        String jsonString = "{ \"devices\" : [ \"";

        for(String dev : listDevices){


            jsonString += dev + "\",";
        }


        jsonString =  jsonString.substring(0, jsonString.length() - 1) +  "]}";
        log.info(jsonString);

        return Response.ok(jsonString).build();
    }
    */


}
