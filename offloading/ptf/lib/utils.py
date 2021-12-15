from base_test import *
import random

import codecs
import ptf.packet as pk
from scapy.all import Raw

def create_OpenPacket(eth_dst = "00:aa:00:00:00:01", eth_src="00:00:00:00:00:01", ip_src="10.0.1.10", ip_dst="10.0.1.100",udp_sport= 5321, udp_dport=7447, udp_payload="\x44\x0a\xc6\xb1\x88\x4e\x20\x07\x0c\x2a\xc4\xe4\xa8\x1e\x33\xea\xd0\x0e\xfc\x78\xb2\xd7\x20\xdf\xc7\x76\x76\x5c\x15\xda\x64\x9b\x19\x8a\x62\xa7\x74\x7d\xa1"):




    p = testutils.simple_udp_packet(eth_dst= eth_dst, eth_src= eth_src, ip_src=ip_src, ip_dst=ip_dst, udp_sport= udp_sport, udp_dport=udp_dport, udp_payload=udp_payload )

    return p


def create_OpenAckPacket(eth_dst = "00:00:00:00:00:01", eth_src="00:aa:00:00:00:01", ip_src="10.0.1.100", ip_dst="10.0.1.10",udp_sport= 7447, udp_dport=5321, udp_payload="\x64\x0a\xbe\xca\xac\x70"):

    p = testutils.simple_udp_packet(eth_dst= eth_dst, eth_src= eth_src, ip_src=ip_src, ip_dst=ip_dst, udp_sport= udp_sport, udp_dport=udp_dport, udp_payload=udp_payload )

    return p





def create_DeclareResourcePacket(eth_dst = "00:aa:00:00:00:01", eth_src="00:00:00:00:00:01", ip_src="10.0.1.10", ip_dst="10.0.1.100",udp_sport= 5321, udp_dport=7447, udp_payload="\x2a\xc6\xb1\x88\x4e\x0b" \
"\x01\x01\x01\x00\x14\x2f\x6d\x79\x68\x6f\x6d\x65\x2f\x6b\x69\x74" \
"\x63\x68\x65\x6e\x2f\x74\x65\x6d\x70"):

    p = testutils.simple_udp_packet(eth_dst= eth_dst, eth_src= eth_src, ip_src=ip_src, ip_dst=ip_dst, udp_sport= udp_sport, udp_dport=udp_dport, udp_payload=udp_payload )

    return p




def create_DeclareSubscriberPacket(eth_dst = "00:aa:00:00:00:01", eth_src="00:00:00:00:00:01", ip_src="10.0.1.10", ip_dst="10.0.1.100",udp_sport= 5321, udp_dport=7447, udp_payload="\x2a\xc7\xb1\x88\x4e\x0b\x01\xa3\x01"):

    p = testutils.simple_udp_packet(eth_dst= eth_dst, eth_src= eth_src, ip_src=ip_src, ip_dst=ip_dst, udp_sport= udp_sport, udp_dport=udp_dport, udp_payload=udp_payload )

    return p



def create_DataPacket(eth_dst = "00:aa:00:00:00:01", eth_src="00:00:00:00:00:01", ip_src="10.0.1.10", ip_dst="10.0.1.100",udp_sport= 5321, udp_dport=7447, udp_payload="\x2a\xc8\xb1\x88\x4e\xec\x01\x60\x00\x06\x02\x33\x30"):


    p = testutils.simple_udp_packet(eth_dst= eth_dst, eth_src= eth_src, ip_src=ip_src, ip_dst=ip_dst, udp_sport= udp_sport, udp_dport=udp_dport, udp_payload=udp_payload )

    return p


def create_ClosePacket(eth_dst = "00:aa:00:00:00:01", eth_src="00:00:00:00:00:01", ip_src="10.0.1.10", ip_dst="10.0.1.100",udp_sport= 5321, udp_dport=7447, udp_payload="\x65\x10\xd6\x95\x7a\x53\xf6\x47\x42\x14\xb4\x0f\xe1\x9d\xb7\x7a" \
"\x1d\x45\x00"):


    p = testutils.simple_udp_packet(eth_dst= eth_dst, eth_src= eth_src, ip_src=ip_src, ip_dst=ip_dst, udp_sport= udp_sport, udp_dport=udp_dport, udp_payload=udp_payload )

    return p






def create_tcp_packet(eth_d = SWITCH1_MAC, eth_s = HOST2_MAC, ip_s = HOST2_IP, ip_d = HOST1_IP, sport = None, dport = None):
    return testutils.simple_tcp_packet(eth_dst=eth_d,
                      eth_src= eth_s,
                      dl_vlan_enable=False,
                      vlan_vid=0,
                      vlan_pcp=0,
                      dl_vlan_cfi=0,
                      ip_src=ip_s,
                      ip_dst=ip_d,
                      ip_tos=0,
                      ip_ecn=None,
                      ip_dscp=None,
                      ip_ttl=64,
                      ip_id=0x0001,
                      ip_frag=0,
                      tcp_sport=random.randint(ConfigureFabric.minPort, ConfigureFabric.maxPort) if sport == None else sport,
                      tcp_dport=random.randint(ConfigureFabric.minPort, ConfigureFabric.maxPort) if dport == None else dport ,
                      tcp_flags="S",
                      ip_ihl=None,
                      ip_options=False,
                      with_tcp_chksum=True)


def create_udp_packet(eth_d = SWITCH1_MAC, eth_s = HOST2_MAC, ip_s = HOST2_IP, ip_d = HOST1_IP, sport = None, dport =None):
    return testutils.simple_udp_packet(eth_dst=eth_d,
                      eth_src= eth_s,
                      dl_vlan_enable=False,
                      vlan_vid=0,
                      vlan_pcp=0,
                      dl_vlan_cfi=0,
                      ip_src=ip_s,
                      ip_dst=ip_d,
                      udp_sport=random.randint(ConfigureFabric.minPort, ConfigureFabric.maxPort) if sport == None else sport,
                      udp_dport=random.randint(ConfigureFabric.minPort, ConfigureFabric.maxPort)if dport == None else dport )

def create_icmp_packet(eth_d = SWITCH1_MAC, eth_s = HOST2_MAC, ip_s = HOST2_IP, ip_d = HOST1_IP):
    return testutils.simple_icmp_packet(eth_dst=eth_d,
                      eth_src= eth_s,
                      ip_src=ip_s,
                      ip_dst=ip_d)



class ConfigureFabric():
    portH1  = random.randint(1,5)
    portH2 = random.randint(5,8)




    minPort = 0x0000
    maxPort = 0xffff


    def configFabricPipe(self, runtimeTest):

        '''
        # ---- Classifier for both hosts ----
        runtimeTest.insert(runtimeTest.helper.build_table_entry(
            table_name="FabricIngress.filtering.fwd_classifier",
            match_fields={
                # Exact match.
                "ig_port": ConfigureFabric.portH1,
                "ip_eth_type": 0x0800
            },
            action_name="FabricIngress.filtering.set_forwarding_type",
            action_params={
                "fwd_type": 2
            },
            priority=DEFAULT_PRIORITY
        ))

        runtimeTest.insert(runtimeTest.helper.build_table_entry(
            table_name="FabricIngress.filtering.fwd_classifier",
            match_fields={
                # Exact match.
                "ig_port": ConfigureFabric.portH2,
                "ip_eth_type": 0x0800
            },
            action_name="FabricIngress.filtering.set_forwarding_type",
            action_params={
                "fwd_type": 2
            },
            priority=DEFAULT_PRIORITY
        ))
        #  ---------


        # ---- Ingress Vlan filter  ----
        runtimeTest.insert(runtimeTest.helper.build_table_entry(
            table_name="FabricIngress.filtering.ingress_port_vlan",
            match_fields={
                # Exact match.
                "ig_port": ConfigureFabric.portH1,
                "vlan_is_valid": 0x0
            },
            action_name="FabricIngress.filtering.permit",
            priority=DEFAULT_PRIORITY
        ))

        runtimeTest.insert(runtimeTest.helper.build_table_entry(
            table_name="FabricIngress.filtering.ingress_port_vlan",
            match_fields={
                # Exact match.
                "ig_port": ConfigureFabric.portH2,
                "vlan_is_valid": 0x0
            },
            action_name="FabricIngress.filtering.permit",
            priority=DEFAULT_PRIORITY
        ))
        # --------


        # ---- Forwarding ----
        runtimeTest.insert(runtimeTest.helper.build_table_entry(
            table_name="FabricIngress.forwarding.routing_v4",
            match_fields={
                # LPM match.
                "ipv4_dst": (HOST1_IP,32)
            },
            action_name="FabricIngress.forwarding.set_next_id_routing_v4",
            action_params={
                "next_id": 0x00000001
            }
        ))

        runtimeTest.insert(runtimeTest.helper.build_table_entry(
            table_name="FabricIngress.forwarding.routing_v4",
            match_fields={
                # LPM match.
                "ipv4_dst": (HOST2_IP,32)
            },
            action_name="FabricIngress.forwarding.set_next_id_routing_v4",
            action_params={
                "next_id": 0x00000002
            }
        ))
        # --------


        # ---- Next ----
        runtimeTest.insert(runtimeTest.helper.build_table_entry(
            table_name="FabricIngress.next.simple",
            match_fields={
                # Exact match.
                "next_id" : 0x00000001
            },
            action_name="FabricIngress.next.routing_simple",
            action_params={
                "port_num": ConfigureFabric.portH1,
                "smac" : SWITCH1_MAC,
                "dmac" : HOST1_MAC
            }
        ))

        runtimeTest.insert(runtimeTest.helper.build_table_entry(
            table_name="FabricIngress.next.simple",
            match_fields={
                # Exact match.
                "next_id" : 0x00000002
            },
            action_name="FabricIngress.next.routing_simple",
            action_params={
                "port_num": ConfigureFabric.portH2,
                "smac" : SWITCH2_MAC,
                "dmac" : HOST2_MAC
            }
        ))
        # --------



        # ---- Next Egress ----
        runtimeTest.insert(runtimeTest.helper.build_table_entry(
            table_name="FabricEgress.egress_next.egress_vlan",
            match_fields={
                # Exact match.
                "vlan_id" : 0x0ffe,
                "eg_port" : ConfigureFabric.portH1
            },
            action_name="FabricEgress.egress_next.pop_vlan"
        ))

        runtimeTest.insert(runtimeTest.helper.build_table_entry(
            table_name="FabricEgress.egress_next.egress_vlan",
            match_fields={
                # Exact match.
                "vlan_id" : 0x0ffe,
                "eg_port" : ConfigureFabric.portH2
            },
            action_name="FabricEgress.egress_next.pop_vlan"
        ))
        # ---- END SOLUTION ----

        '''
        # ---- Acl ----
        runtimeTest.insert(runtimeTest.helper.build_table_entry(
            table_name="FabricIngress.acl.acl",
            match_fields={
                # ternary match.
                "send": ( 1, 1)
            },
            action_name="FabricIngress.acl.punt_to_cpu",
            priority=DEFAULT_PRIORITY
        ))



