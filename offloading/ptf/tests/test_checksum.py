# Copyright 2013-present Barefoot Networks, Inc.
# Copyright 2018-present Open Networking Foundation
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# ------------------------------------------------------------------------------
# PROTOCOL TESTS
#
# To run all tests in this file:
#     make p4-test TEST=protocol
# ------------------------------------------------------------------------------

# ------------------------------------------------------------------------------
#
# When providing your solution, make sure to use the same names for P4Runtime
# entities as specified in your P4Info file.
#
# Test cases are based on the P4 program design suggested in the exercises
# README. Make sure to modify the test cases accordingly if you decide to
# implement the pipeline differently.
# ------------------------------------------------------------------------------

from ptf.testutils import group
import ptf.mask as mask
import ptf.packet as p
from base_test import *
from utils import *

# From the P4 program.
CPU_CLONE_SESSION_ID = 99


'''
@group("Checksum")
class Case1(P4RuntimeTest):
    """Tests
    """

    def runTest(self):

        configure = ConfigureFabric()
        configure.configFabricPipe(self)



        self.runCase1()



    @autocleanup
    def runCase1(self):

        print('\n\nOpen Packet Test')



        mcast_group_id = 1
        mcast_ports = [HOST1_PHYSICAL_PORT]

        # Add multicast group.
        self.insert_pre_multicast_group(
            group_id=mcast_group_id,
            ports=mcast_ports)


        #Subscriber

        #Subscriber Open Packet
        # initial sn \xc6\xb1\x88\x4e

        pktOpenS = create_OpenPacket()

        # Send packet...
        testutils.send_packet(self, HOST1_PHYSICAL_PORT, str(pktOpenS))
        print_inline("Open Packet Sent... ")

        #Controller OpenAck Packet
        # initial sn \xc4\xeb\xd3\x06

        pktOpenAckS = create_OpenAckPacket(udp_payload="\x64\x0a\xc4\xeb\xd3\x06")


        packet_out_msg = self.helper.build_packet_out(
                payload=str(pktOpenAckS),
                metadata={
                    "egress_port": HOST1_PHYSICAL_PORT,
                    "_pad": 0
                })

        # Send message and expect packet on the given data plane port.
        self.send_packet_out(packet_out_msg)



        testutils.verify_packet(self, pktOpenAckS, HOST1_PHYSICAL_PORT)

        # sn \xc6\xb1\x88\x4e
        pktSubDeclareResource = create_DeclareResourcePacket()

        # Send packet...
        testutils.send_packet(self, HOST1_PHYSICAL_PORT, str(pktSubDeclareResource))
        print_inline("Resource Packet Sent... ")


        # sn \xc7\xb1\x88\x4e

        pktSubDeclareSubscriber = create_DeclareSubscriberPacket()

        # Send packet...
        testutils.send_packet(self, HOST1_PHYSICAL_PORT, str(pktSubDeclareSubscriber))
        print_inline("Subscriber Packet Sent... ")



        self.insert(self.helper.build_table_entry(
            table_name="FabricIngress.zenoh_in.TopicSubscriberGroup",
            match_fields={
                # Exact match.
                "fabric_metadata.zenoh_resourceId": 1,
            },
            action_name="FabricIngress.zenoh_in.set_multicast",
            action_params={
                "multicast_group_id": 1
            }
        ))

        self.insert(self.helper.build_table_entry(
            table_name="FabricEgress.zenoh_out.SendToSubscriber",
            match_fields={
                # Exact match.
                "standard_metadata.egress_port" : HOST1_PHYSICAL_PORT,
                "standard_metadata.mcast_grp" : 1
            },
            action_name="FabricEgress.zenoh_out.sendDatatoSub",
            action_params={
                "src_eth": "00:aa:00:00:00:01",
                "dst_eth" : HOST1_MAC,
                "src_ip" : "10.0.1.100",
                "dst_ip" : HOST1_IP,
                "udp_dport" : HOST1_UDP_PORT
            }
        ))


        # Publisher

        # Publisher Open Packet
        # initial sn \xc6\xb1\x88\x4e

        pktOpenP = create_OpenPacket(eth_src=HOST2_MAC, ip_src=HOST2_IP, ip_dst=HOST2_GW,udp_sport= HOST2_UDP_PORT, udp_dport=ZENOH_UDP_PORT)

        # Send packet...
        testutils.send_packet(self, HOST2_PHYSICAL_PORT, str(pktOpenP))
        print_inline("Open Packet Sent... ")




        # Controller OpenAck Packet
        # Initial sn  \xcf\xcf\xcf\xcf
        pktOpenAckP = create_OpenAckPacket( eth_src=HOST2_MAC, ip_src=HOST2_IP, ip_dst=HOST2_GW,udp_sport= HOST2_UDP_PORT, udp_dport=ZENOH_UDP_PORT, udp_payload="\x64\x0a\xcf\xcf\xcf\x7f")


        packet_out_msg = self.helper.build_packet_out(
                payload=str(pktOpenAckP),
                metadata={
                    "egress_port": HOST2_PHYSICAL_PORT,
                    "_pad": 0
                })

        # Send message and expect packet on the given data plane port.
        self.send_packet_out(packet_out_msg)


        testutils.verify_packet(self, pktOpenAckP, HOST2_PHYSICAL_PORT)



        # sn  \xcf\xcf\xcf\xcf
        pktSubDeclareResourceP = create_DeclareResourcePacket(eth_src=HOST2_MAC, ip_src=HOST2_IP, ip_dst=HOST2_GW,udp_sport= HOST2_UDP_PORT, udp_dport=ZENOH_UDP_PORT, udp_payload = "\x2a\xcf\xcf\xcf\x7f\x0b" \
        "\x01\x01\x01\x00\x14\x2f\x6d\x79\x68\x6f\x6d\x65\x2f\x6b\x69\x74" \
        "\x63\x68\x65\x6e\x2f\x74\x65\x6d\x70")

        packet_out_msg = self.helper.build_packet_out(
        payload=str(pktSubDeclareResourceP),
        metadata={
            "egress_port": HOST2_PHYSICAL_PORT,
            "_pad": 0
        })

        # Send message and expect packet on the given data plane port.
        self.send_packet_out(packet_out_msg)


        testutils.verify_packet(self, pktSubDeclareResourceP, HOST2_PHYSICAL_PORT)




        # sn  \xd0\xcf\xcf\xcf
        pktSubDeclareSubscriberP = create_DeclareSubscriberPacket(eth_src=HOST2_MAC, ip_src=HOST2_IP, ip_dst=HOST2_GW,udp_sport= HOST2_UDP_PORT, udp_dport=ZENOH_UDP_PORT, udp_payload = "\x2a\xd0\xcf\xcf\x7f\x0b\x01\xa3\x01")

        packet_out_msg = self.helper.build_packet_out(
        payload=str(pktSubDeclareSubscriberP),
        metadata={
            "egress_port": HOST2_PHYSICAL_PORT,
            "_pad": 0
        })


        # Send message and expect packet on the given data plane port.
        self.send_packet_out(packet_out_msg)

        testutils.verify_packet(self, pktSubDeclareSubscriberP, HOST2_PHYSICAL_PORT)


        # sn \xc6\xb1\x88\x4e
        packetData = create_DataPacket(eth_dst= SWITCH1_MAC ,eth_src=HOST2_MAC, ip_src=HOST2_IP, ip_dst=HOST2_GW, udp_dport=ZENOH_UDP_PORT , udp_sport=HOST2_UDP_PORT, udp_payload= "\x2a\xc6\xb1\x88\x4e\xec\x01\x60\x00\x06\x02\x33\x30")
    
    
        # sn \xc4\xeb\xd3\x06
        out_packetData = create_DataPacket(eth_dst= HOST1_MAC ,eth_src=SWITCH1_MAC, ip_src=HOST1_GW, ip_dst=HOST1_IP, udp_dport= HOST1_UDP_PORT, udp_sport=ZENOH_UDP_PORT, udp_payload="\x2a\xc4\xeb\xd3\x06\xac\x02\x09\x5b\x20\x20\x31\x34\x5d\x20\x32\x35")
        
        maskedP = mask.Mask(out_packetData)
        maskedP.set_do_not_care_scapy(p.IP, 'ihl')
        maskedP.set_do_not_care_scapy(p.IP, 'len')

        maskedP.set_do_not_care_scapy(p.IP, 'ttl')
        maskedP.set_do_not_care_scapy(p.IP, 'chksum')

        maskedP.set_do_not_care_scapy(p.UDP, 'dataofs')
        


        # Send packet...
        testutils.send_packet(self, HOST2_PHYSICAL_PORT, str(packetData))
        
        print_inline("Open Packet Sent... ")

        packet_out_msg = self.helper.build_packet_out(
        payload=str(packetData),
        metadata={
            "egress_port": HOST2_PHYSICAL_PORT,
            "_pad": 0
        })


        # Send message and expect packet on the given data plane port.
        self.send_packet_out(packet_out_msg)
        testutils.verify_packet(self, maskedP, HOST1_PHYSICAL_PORT)

        # sn \xc7\xb1\x88\x4e
        packetData2 = create_DataPacket(eth_dst= SWITCH1_MAC ,eth_src=HOST2_MAC, ip_src=HOST2_IP, ip_dst=HOST2_GW, udp_dport=ZENOH_UDP_PORT, udp_sport=HOST2_UDP_PORT,udp_payload="\x2a\xc7\xb1\x88\x4e\xec\x01\x60\x00\x06\x02\x33\x30")

        out_packetData2 = create_DataPacket(eth_dst= HOST1_MAC ,eth_src=SWITCH1_MAC, ip_src=HOST1_GW, ip_dst=HOST1_IP, udp_dport= HOST1_UDP_PORT, udp_sport=ZENOH_UDP_PORT, udp_payload="\x2a\xc5\xeb\xd3\x06\xac\x02\x09\x5b\x20\x20\x31\x35\x5d\x20\x32\x35")

        maskedP2 = mask.Mask(out_packetData2)
        maskedP2.set_do_not_care_scapy(p.IP, 'ihl')
        maskedP2.set_do_not_care_scapy(p.IP, 'len')

        maskedP2.set_do_not_care_scapy(p.IP, 'ttl')
        maskedP2.set_do_not_care_scapy(p.IP, 'chksum')

        maskedP2.set_do_not_care_scapy(p.UDP, 'dataofs')



        # Send packet...
        testutils.send_packet(self, HOST2_PHYSICAL_PORT, str(packetData2))
        print_inline("Open Packet Sent... ")




        testutils.verify_packet(self, maskedP2, HOST1_PHYSICAL_PORT)

'''
