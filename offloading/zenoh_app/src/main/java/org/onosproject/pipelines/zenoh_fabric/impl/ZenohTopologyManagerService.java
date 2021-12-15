package org.onosproject.pipelines.zenoh_fabric.impl;

import org.onosproject.net.DeviceId;
import org.onosproject.net.Link;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public interface ZenohTopologyManagerService{

    public Map<DeviceId, NodePath> getPaths(DeviceId srcDevice);

    public ArrayList<Link> linksConfigured(int multicastGroup);


    public void addLinkConfigured(int multicastGroup, Link l);


}
