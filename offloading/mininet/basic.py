#!/usr/bin/python

#  Copyright 2019-present Open Networking Foundation
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

import argparse


from mininet.log import setLogLevel, info, debug
from mininet.net import Mininet
from mininet.node import Host
from mininet.topo import Topo
from mininet.cli import CLI
from stratum import StratumBmv2Switch
from time import sleep
import json
import os
import subprocess
import sys
from mininet.net import Mininet
from mininet.node import Controller, Host


CPU_PORT = 255
class IPv4Host(Host):
    """Host that can be configured with an IPv4 gateway (default route).
    """

    def config(self, mac=None, ip=None, defaultRoute=None, lo='up', gw=None,
               **_params):
        super(IPv4Host, self).config(mac, ip, defaultRoute, lo, **_params)
        self.cmd('ip -4 addr flush dev %s' % self.defaultIntf())
        self.cmd('ip -6 addr flush dev %s' % self.defaultIntf())
        self.cmd('ip -4 link set up %s' % self.defaultIntf())
        self.cmd('ip -4 addr add %s dev %s' % (ip, self.defaultIntf()))
        if gw:
            self.cmd('ip -4 route add default via %s' % gw)
        # Disable offload
        for attr in ["rx", "tx", "sg"]:
            cmd = "/sbin/ethtool --offload %s %s off" % (
                self.defaultIntf(), attr)
            self.cmd(cmd)

        def updateIP():
            return ip.split('/')[0]

        self.defaultIntf().updateIP = updateIP

class IPv6Host(Host):
    """Host that can be configured with an IPv6 gateway (default route).
    """

    def config(self, ipv6, ipv6_gw=None, **params):
        super(IPv6Host, self).config(**params)
        self.cmd('ip -4 addr flush dev %s' % self.defaultIntf())
        self.cmd('ip -6 addr flush dev %s' % self.defaultIntf())
        self.cmd('ip -6 addr add %s dev %s' % (ipv6, self.defaultIntf()))
        if ipv6_gw:
            self.cmd('ip -6 route add default via %s' % ipv6_gw)
        # Disable offload
        for attr in ["rx", "tx", "sg"]:
            cmd = "/sbin/ethtool --offload %s %s off" % (self.defaultIntf(), attr)
            self.cmd(cmd)

        def updateIP():
            return ipv6.split('/')[0]

        self.defaultIntf().updateIP = updateIP

    def terminate(self):
        super(IPv6Host, self).terminate()

class TaggedIPv4Host(Host):
    """VLAN-tagged host that can be configured with an IPv4 gateway
    (default route).
    """
    vlanIntf = None

    def config(self, mac=None, ip=None, defaultRoute=None, lo='up', gw=None,
               vlan=None, **_params):
        super(TaggedIPv4Host, self).config(mac, ip, defaultRoute, lo, **_params)
        self.vlanIntf = "%s.%s" % (self.defaultIntf(), vlan)
        # Replace default interface with a tagged one
        self.cmd('ip -4 addr flush dev %s' % self.defaultIntf())
        self.cmd('ip -6 addr flush dev %s' % self.defaultIntf())
        self.cmd('ip -4 link add link %s name %s type vlan id %s' % (
            self.defaultIntf(), self.vlanIntf, vlan))
        self.cmd('ip -4 link set up %s' % self.vlanIntf)
        self.cmd('ip -4 addr add %s dev %s' % (ip, self.vlanIntf))
        if gw:
            self.cmd('ip -4 route add default via %s' % gw)

        self.defaultIntf().name = self.vlanIntf
        self.nameToIntf[self.vlanIntf] = self.defaultIntf()

        # Disable offload
        for attr in ["rx", "tx", "sg"]:
            cmd = "/sbin/ethtool --offload %s %s off" % (
                self.defaultIntf(), attr)
            self.cmd(cmd)

        def updateIP():
            return ip.split('/')[0]

        self.defaultIntf().updateIP = updateIP

    def terminate(self):
        self.cmd('ip -4 link remove link %s' % self.vlanIntf)
        super(TaggedIPv4Host, self).terminate()


class DemoTopo(Topo):
    """2x2 fabric topology with IPv6 hosts"""

    def __init__(self, *args, **kwargs):
        Topo.__init__(self, *args, **kwargs)

        loglevel = "debug"

        topo_file = json.load(open(os.path.join(sys.path[0], 'topology/topologyLinear1.json')))

        equipments = {}
        for sw_name in topo_file['switches']:
            sw_info = topo_file['switches'][sw_name]
            sw = self.addSwitch(sw_name, cls=StratumBmv2Switch, cpuport=CPU_PORT, loglevel=loglevel, listenPort=int(sw_info['grpc']))
            equipments[sw_name] = sw

        for host_name in topo_file['hosts']:
            host_info = topo_file['hosts'][host_name]
            host = self.addHost(host_name, cls=IPv4Host, mac=host_info['mac'], ip=host_info['ip'], gw=host_info['gateway'])
            equipments[host_name] = host

        for link in topo_file['links']:
            node1, node2 = link.split('-')
            link_info = topo_file['links'][link]
            self.addLink(equipments[node1], equipments[node2], port1 = int(link_info['port1']) , port2 = int(link_info['port2']))
def main():
    net = Mininet(topo=DemoTopo(), controller=None)
    net.start()

    # Temporary static arp table
    


    #CLI(net)
    
    sleep(50*3)
    print "Starting zenoh..."
    
    
    h1 = net.getNodeByName("h1")
    h1.popen("python3.9 -u /mininet/ze_sub.py --host h1 --topic 1 --topics_to_listen 5")
    sleep(5)
    h2 = net.getNodeByName("h2")
    h2.popen("python3.9 -u /mininet/ze_sub.py --host h2 --topic 2 --topics_to_listen 1")
    sleep(5)
    h3 = net.getNodeByName("h3")
    h3.popen("python3.9 -u /mininet/ze_sub.py --host h3 --topic 3 --topics_to_listen 1")
    sleep(5)
    h4 = net.getNodeByName("h4")
    h4.popen("python3.9 -u /mininet/ze_sub.py --host h4 --topic 4 --topics_to_listen 1")
    sleep(5)
    h5 = net.getNodeByName("h5")
    h5.popen("python3.9 -u /mininet/ze_sub.py --host h5 --topic 5 --topics_to_listen 1")
    sleep(5)
    
    h11 = net.getNodeByName("h11")
    h11.popen("python3.9 -u /mininet/ze_put.py --host h11 --topic 1")
    h12 = net.getNodeByName("h12")
    h12.popen("python3.9 -u /mininet/ze_put.py --host h12 --topic 2")
    h13 = net.getNodeByName("h13")
    h13.popen("python3.9 -u /mininet/ze_put.py --host h13 --topic 3")
    h14 = net.getNodeByName("h14")
    h14.popen("python3.9 -u /mininet/ze_put.py --host h14 --topic 4")
    h15 = net.getNodeByName("h15")
    h15.popen("python3.9 -u /mininet/ze_put.py --host h15 --topic 5")

    sleep(420)
    
    net.stop()
    
    
    print ('#' * 80)
    print ('ATTENTION: Mininet was stopped! Perhaps accidentally?')
    print ('No worries, it will restart automatically in a few seconds...')
    print ('To access again the Mininet CLI, use `make mn-cli`')
    print ('To detach from the CLI (without stopping), press Ctrl-D')
    print ('To permanently quit Mininet, use `make stop`')
    print ('#' * 80)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description='Mininet topology script for 2x2 fabric with stratum_bmv2 and IPv6 hosts')

    args = parser.parse_args()

    setLogLevel('info')

    main()
