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



@group("Open")
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

        pkt = create_OpenPacket()


        # Send packet...
        testutils.send_packet(self, HOST1_PHYSICAL_PORT, str(pkt))
        print_inline("Open Packet Sent... ")

        testutils.verify_no_other_packets(self)




@group("Open")
class Case2(P4RuntimeTest):
    """Tests
    """

    def runTest(self):

        configure = ConfigureFabric()
        configure.configFabricPipe(self)



        self.runCase2()



    @autocleanup
    def runCase2(self):

        print('\n\nOpen Packet Test')

        pkt = create_OpenPacket()

        pkt2 = create_OpenPacket(udp_sport = 8080)

        # Send packet...
        testutils.send_packet(self, HOST1_PHYSICAL_PORT, str(pkt))
        print_inline("Open Packet Sent... ")

        # Send packet...
        testutils.send_packet(self, HOST1_PHYSICAL_PORT, str(pkt2))
        print_inline("Open Packet 2 Sent... ")

        testutils.verify_no_other_packets(self)





@group("Open")
class Case3(P4RuntimeTest):


    def runTest(self):

        configure = ConfigureFabric()
        configure.configFabricPipe(self)



        self.runCase3()



    @autocleanup
    def runCase3(self):

        print('\n\nOpen Ack Packet Test')

        pkt = create_OpenAckPacket()

        packet_out_msg = self.helper.build_packet_out(
                payload=str(pkt),
                metadata={
                    "egress_port": HOST1_PHYSICAL_PORT,
                    "_pad": 0
                })
            # ---- END SOLUTION ----

        # Send message and expect packet on the given data plane port.
        self.send_packet_out(packet_out_msg)
        testutils.verify_packet(self, pkt, HOST1_PHYSICAL_PORT)




@group("Open")
class Case4(P4RuntimeTest):


    def runTest(self):

        configure = ConfigureFabric()
        configure.configFabricPipe(self)



        self.runCase4()



    @autocleanup
    def runCase4(self):

        print('\n\nOpen Ack Packet Test')

        pkt = create_OpenAckPacket()

        pkt2 = create_OpenAckPacket(udp_dport=5232, udp_payload = "\x64\x0a\xbb\xcc\xaa\x7f")

        packet_out_msg = self.helper.build_packet_out(
                payload=str(pkt),
                metadata={
                    "egress_port": HOST1_PHYSICAL_PORT,
                    "_pad": 0
                })


        packet_out_msg2 = self.helper.build_packet_out(
                payload=str(pkt2),
                metadata={
                    "egress_port": HOST2_PHYSICAL_PORT,
                    "_pad": 0
                })

        # Send message and expect packet on the given data plane port.
        self.send_packet_out(packet_out_msg)

        testutils.verify_packet(self, pkt, HOST1_PHYSICAL_PORT)

        self.send_packet_out(packet_out_msg2)

        testutils.verify_packet(self, pkt2, HOST2_PHYSICAL_PORT)





@group("Open")
class Case5(P4RuntimeTest):
    """Tests
    """

    def runTest(self):

        configure = ConfigureFabric()
        configure.configFabricPipe(self)



        self.runCase5()



    @autocleanup
    def runCase5(self):

        print('\n\nOpen Packet Test')


        pktOpen = create_OpenPacket()
        # Send packet...
        testutils.send_packet(self, HOST1_PHYSICAL_PORT, str(pktOpen))
        print_inline("Open Packet Sent... ")


        pktOpenAck = create_OpenAckPacket()


        packet_out_msg = self.helper.build_packet_out(
                payload=str(pktOpenAck),
                metadata={
                    "egress_port": HOST1_PHYSICAL_PORT,
                    "_pad": 0
                })

        # Send message and expect packet on the given data plane port.
        self.send_packet_out(packet_out_msg)


        testutils.verify_packet(self, pktOpenAck, HOST1_PHYSICAL_PORT)

        pkt = create_DeclareResourcePacket()


        pkt2 = create_DeclareSubscriberPacket()


        # Send packet...
        testutils.send_packet(self, HOST1_PHYSICAL_PORT, str(pkt))
        print_inline("Declare Packet Sent... ")

        # Send packet...
        testutils.send_packet(self, HOST1_PHYSICAL_PORT, str(pkt2))
        print_inline("Declare Packet 2 Sent... ")

        testutils.verify_no_other_packets(self)




@group("Open")
class Case6(P4RuntimeTest):
    """Tests
    """

    def runTest(self):

        configure = ConfigureFabric()
        configure.configFabricPipe(self)



        self.runCase6()



    @autocleanup
    def runCase6(self):

        print('\n\nOpen Packet Test')


        pktOpen = create_OpenPacket()
        # Send packet...
        testutils.send_packet(self, HOST1_PHYSICAL_PORT, str(pktOpen))
        print_inline("Open Packet Sent... ")


        pktOpenAck = create_OpenAckPacket()


        packet_out_msg = self.helper.build_packet_out(
                payload=str(pktOpenAck),
                metadata={
                    "egress_port": HOST1_PHYSICAL_PORT,
                    "_pad": 0
                })

        # Send message and expect packet on the given data plane port.
        self.send_packet_out(packet_out_msg)


        testutils.verify_packet(self, pktOpenAck, HOST1_PHYSICAL_PORT)

        pkt = create_DeclareResourcePacket(eth_src=SWITCH1_MAC, eth_dst=HOST1_MAC, ip_src=HOST1_GW, ip_dst=HOST1_IP,udp_sport= ZENOH_UDP_PORT, udp_dport=HOST1_UDP_PORT, udp_payload = "\x2a\xbe\xca\xac\x70\x0b" \
"\x01\x01\x01\x00\x14\x2f\x6d\x79\x68\x6f\x6d\x65\x2f\x6b\x69\x74" \
"\x63\x68\x65\x6e\x2f\x74\x65\x6d\x70")


        pkt2 = create_DeclareSubscriberPacket(eth_src=SWITCH1_MAC, eth_dst=HOST1_MAC, ip_src=HOST1_GW, ip_dst=HOST1_IP,udp_sport= ZENOH_UDP_PORT, udp_dport=HOST1_UDP_PORT, udp_payload = "\x2a\xbf\xca\xac\x70\x0b\x01\xa3\x01")


        packet_out_msg = self.helper.build_packet_out(
                payload=str(pkt),
                metadata={
                    "egress_port": HOST1_PHYSICAL_PORT,
                    "_pad": 0
                })

        # Send message and expect packet on the given data plane port.
        self.send_packet_out(packet_out_msg)


        packet_out_msg2 = self.helper.build_packet_out(
                payload=str(pkt2),
                metadata={
                    "egress_port": HOST1_PHYSICAL_PORT,
                    "_pad": 0
                })

        # Send message and expect packet on the given data plane port.
        self.send_packet_out(packet_out_msg2)

        testutils.verify_packet(self, pkt, HOST1_PHYSICAL_PORT)
        testutils.verify_packet(self, pkt2, HOST1_PHYSICAL_PORT)

        testutils.verify_no_other_packets(self)






@group("Open")
class Case7(P4RuntimeTest):
    """Tests
    """

    def runTest(self):

        configure = ConfigureFabric()
        configure.configFabricPipe(self)



        self.runCase7()



    @autocleanup
    def runCase7(self):

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
        # initial sn \xbe\xca\xac\x70
        pktOpenAckS = create_OpenAckPacket()


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


        #Publisher

        #Publisher Open Packet
        # initial sn \xc6\xb1\x88\x4e
        pktOpenP = create_OpenPacket(eth_src=HOST2_MAC, ip_src=HOST2_IP, ip_dst=HOST2_GW, udp_sport= HOST2_UDP_PORT, udp_dport=ZENOH_UDP_PORT)

        # Send packet...
        testutils.send_packet(self, HOST2_PHYSICAL_PORT, str(pktOpenP))
        print_inline("Open Packet Sent... ")




        #Controller OpenAck Packet
        #Initial sn \xcf\xcf\xcf\x7f
        pktOpenAckP = create_OpenAckPacket( eth_src=SWITCH1_MAC, ip_src=HOST2_GW, ip_dst=HOST2_IP,udp_sport= ZENOH_UDP_PORT, udp_dport=HOST2_UDP_PORT, udp_payload="\x64\x0a\xcf\xcf\xcf\x7f")


        packet_out_msg = self.helper.build_packet_out(
                payload=str(pktOpenAckP),
                metadata={
                    "egress_port": HOST2_PHYSICAL_PORT,
                    "_pad": 0
                })

        # Send message and expect packet on the given data plane port.
        self.send_packet_out(packet_out_msg)


        testutils.verify_packet(self, pktOpenAckP, HOST2_PHYSICAL_PORT)



        # sn \xcf\xcf\xcf\x7f

        pktSubDeclareResourceP = create_DeclareResourcePacket(eth_src=SWITCH1_MAC, ip_src=HOST2_GW, ip_dst=HOST2_IP, udp_sport= ZENOH_UDP_PORT, udp_dport=HOST2_UDP_PORT, udp_payload = "\x2a\xcf\xcf\xcf\x7f\x0b" \
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




        # sn \xd0\xcf\xcf\x7f

        pktSubDeclareSubscriberP = create_DeclareSubscriberPacket(eth_src=SWITCH1_MAC, ip_src=HOST2_GW, ip_dst=HOST2_IP,udp_sport= ZENOH_UDP_PORT, udp_dport=HOST2_UDP_PORT, udp_payload = "\x2a\xd0\xcf\xcf\x7f\x0b\x01\xa3\x01")

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
        packetData = create_DataPacket(eth_dst= SWITCH1_MAC ,eth_src=HOST2_MAC, ip_src=HOST2_IP, ip_dst=HOST2_GW, udp_dport= ZENOH_UDP_PORT, udp_sport=HOST2_UDP_PORT, udp_payload="\x2a\xc6\xb1\x88\x4e\xec\x01\x60\x00\x06\x02\x33\x30")

        out_packetData = create_DataPacket(eth_dst= HOST1_MAC ,eth_src=SWITCH1_MAC, ip_src=HOST1_GW, ip_dst=HOST1_IP, udp_dport= HOST1_UDP_PORT, udp_sport=ZENOH_UDP_PORT, udp_payload="\x2a\xbe\xca\xac\x70\xec\x01\x60\x00\x06\x02\x33\x30")

        maskedP = mask.Mask(out_packetData)
        maskedP.set_do_not_care_scapy(p.IP, 'ihl')
        maskedP.set_do_not_care_scapy(p.IP, 'len')

        maskedP.set_do_not_care_scapy(p.IP, 'ttl')
        maskedP.set_do_not_care_scapy(p.IP, 'chksum')

        maskedP.set_do_not_care_scapy(p.UDP, 'dataofs')
        maskedP.set_do_not_care_scapy(p.UDP, 'chksum')



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



@group("Open")
class Case8(P4RuntimeTest):
    """Tests
    """

    def runTest(self):

        configure = ConfigureFabric()
        configure.configFabricPipe(self)



        self.runCase8()



    @autocleanup
    def runCase8(self):

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
        # initial sn \xbe\xca\xac\x70

        pktOpenAckS = create_OpenAckPacket()


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

        out_packetData = create_DataPacket(eth_dst= HOST1_MAC ,eth_src=SWITCH1_MAC, ip_src=HOST1_GW, ip_dst=HOST1_IP, udp_dport= HOST1_UDP_PORT, udp_sport=ZENOH_UDP_PORT, udp_payload="\x2a\xbe\xca\xac\x70\xec\x01\x60\x00\x06\x02\x33\x30")

        maskedP = mask.Mask(out_packetData)
        maskedP.set_do_not_care_scapy(p.IP, 'ihl')
        maskedP.set_do_not_care_scapy(p.IP, 'len')

        maskedP.set_do_not_care_scapy(p.IP, 'ttl')
        maskedP.set_do_not_care_scapy(p.IP, 'chksum')

        maskedP.set_do_not_care_scapy(p.UDP, 'dataofs')
        maskedP.set_do_not_care_scapy(p.UDP, 'chksum')



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

        out_packetData2 = create_DataPacket(eth_dst= HOST1_MAC ,eth_src=SWITCH1_MAC, ip_src=HOST1_GW, ip_dst=HOST1_IP, udp_dport= HOST1_UDP_PORT, udp_sport=ZENOH_UDP_PORT, udp_payload="\x2a\xbf\xca\xac\x70\xec\x01\x60\x00\x06\x02\x33\x30")

        maskedP2 = mask.Mask(out_packetData2)
        maskedP2.set_do_not_care_scapy(p.IP, 'ihl')
        maskedP2.set_do_not_care_scapy(p.IP, 'len')

        maskedP2.set_do_not_care_scapy(p.IP, 'ttl')
        maskedP2.set_do_not_care_scapy(p.IP, 'chksum')

        maskedP2.set_do_not_care_scapy(p.UDP, 'dataofs')
        maskedP2.set_do_not_care_scapy(p.UDP, 'chksum')



        # Send packet...
        testutils.send_packet(self, HOST2_PHYSICAL_PORT, str(packetData2))
        print_inline("Open Packet Sent... ")




        testutils.verify_packet(self, maskedP2, HOST1_PHYSICAL_PORT)


@group("Open")
class Case9(P4RuntimeTest):
    """Tests
    """

    def runTest(self):

        configure = ConfigureFabric()
        configure.configFabricPipe(self)



        self.runCase9()



    @autocleanup
    def runCase9(self):

        print('\n\nOpen Packet Test')



        mcast_group_id = 1
        mcast_ports = [HOST1_PHYSICAL_PORT]

        # Add multicast group.
        self.insert_pre_multicast_group(
            group_id=mcast_group_id,
            ports=mcast_ports)


        #Subscriber

        #Subscriber Open Packet

        # initial sn \xff\xff\xff\xff\x80\x04
        pktOpenS = create_OpenPacket(udp_payload="\x44\x0a\xff\xff\xff\xff\x80\x04\x20\x07\x0c\x2a\xc4\xe4\xa8\x1e\x33\xea\xd0\x0e\xfc\x78\xb2\xd7\x20\xdf\xc7\x76\x76\x5c\x15\xda\x64\x9b\x19\x8a\x62\xa7\x74\x7d\xa1")

        # Send packet...
        testutils.send_packet(self, HOST1_PHYSICAL_PORT, str(pktOpenS))
        print_inline("Open Packet Sent... ")

        #Controller OpenAck Packet

        # initial sn \xff\xff\80\x70
        pktOpenAckS = create_OpenAckPacket(udp_payload="\x64\x0a\xff\xff\x80\x70")


        packet_out_msg = self.helper.build_packet_out(
                payload=str(pktOpenAckS),
                metadata={
                    "egress_port": HOST1_PHYSICAL_PORT,
                    "_pad": 0
                })

        # Send message and expect packet on the given data plane port.
        self.send_packet_out(packet_out_msg)



        testutils.verify_packet(self, pktOpenAckS, HOST1_PHYSICAL_PORT)

        # sn \xff\xff\xff\xff\x80\x04
        pktSubDeclareResource = create_DeclareResourcePacket(udp_payload="\x2a\xff\xff\xff\xff\x80\x04\x0b" \
"\x01\x01\x01\x00\x14\x2f\x6d\x79\x68\x6f\x6d\x65\x2f\x6b\x69\x74" \
"\x63\x68\x65\x6e\x2f\x74\x65\x6d\x70")

        # Send packet...
        testutils.send_packet(self, HOST1_PHYSICAL_PORT, str(pktSubDeclareResource))
        print_inline("Resource Packet Sent... ")


        # sn \x80\x80\x80\x80\x81\x04
        pktSubDeclareSubscriber = create_DeclareSubscriberPacket(udp_payload="\x2a\x80\x80\x80\x80\x81\x04\x0b\x01\xa3\x01")

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


        #Publisher

        #Publisher Open Packet

        # initial sn \xc6\xb1\x88\x4e
        pktOpenP = create_OpenPacket(eth_src=HOST2_MAC, ip_src=HOST2_IP, ip_dst=HOST2_GW,udp_sport= HOST2_UDP_PORT, udp_dport=ZENOH_UDP_PORT)

        # Send packet...
        testutils.send_packet(self, HOST2_PHYSICAL_PORT, str(pktOpenP))
        print_inline("Open Packet Sent... ")




        # Controller OpenAck Packet
        # Initial sn \xcf\xcf\xcf\x7f
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



        # sn \xcf\xcf\xcf\x7f
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



        # sn \xd0\xcf\xcf\x7f
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
        packetData = create_DataPacket(eth_dst= SWITCH1_MAC ,eth_src=HOST2_MAC, ip_src=HOST2_IP, ip_dst=HOST2_GW, udp_dport=ZENOH_UDP_PORT , udp_sport=HOST2_UDP_PORT, udp_payload="\x2a\xc6\xb1\x88\x4e\xec\x01\x60\x00\x06\x02\x33\x30")

        out_packetData = create_DataPacket(eth_dst= HOST1_MAC ,eth_src=SWITCH1_MAC, ip_src=HOST1_GW, ip_dst=HOST1_IP, udp_dport= HOST1_UDP_PORT, udp_sport=ZENOH_UDP_PORT, udp_payload="\x2a\xff\xff\x80\x70\xec\x01\x60\x00\x06\x02\x33\x30")

        maskedP = mask.Mask(out_packetData)
        maskedP.set_do_not_care_scapy(p.IP, 'ihl')
        maskedP.set_do_not_care_scapy(p.IP, 'len')

        maskedP.set_do_not_care_scapy(p.IP, 'ttl')
        maskedP.set_do_not_care_scapy(p.IP, 'chksum')

        maskedP.set_do_not_care_scapy(p.UDP, 'dataofs')
        maskedP.set_do_not_care_scapy(p.UDP, 'chksum')



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
        packetData2 = create_DataPacket(eth_dst= SWITCH1_MAC ,eth_src=HOST2_MAC, ip_src=HOST2_IP, ip_dst=HOST2_GW, udp_dport=ZENOH_UDP_PORT , udp_sport=HOST2_UDP_PORT, udp_payload="\x2a\xc7\xb1\x88\x4e\xec\x01\x60\x00\x06\x02\x33\x30")

        out_packetData2 = create_DataPacket(eth_dst= HOST1_MAC ,eth_src=SWITCH1_MAC, ip_src=HOST1_GW, ip_dst=HOST1_IP, udp_dport= HOST1_UDP_PORT, udp_sport=ZENOH_UDP_PORT, udp_payload="\x2a\x80\x80\x81\x70\xec\x01\x60\x00\x06\x02\x33\x30")

        maskedP2 = mask.Mask(out_packetData2)
        maskedP2.set_do_not_care_scapy(p.IP, 'ihl')
        maskedP2.set_do_not_care_scapy(p.IP, 'len')

        maskedP2.set_do_not_care_scapy(p.IP, 'ttl')
        maskedP2.set_do_not_care_scapy(p.IP, 'chksum')

        maskedP2.set_do_not_care_scapy(p.UDP, 'dataofs')
        maskedP2.set_do_not_care_scapy(p.UDP, 'chksum')



        # Send packet...
        testutils.send_packet(self, HOST2_PHYSICAL_PORT, str(packetData2))
        print_inline("Open Packet Sent... ")


        testutils.verify_packet(self, maskedP2, HOST1_PHYSICAL_PORT)


@group("Open")
class Case10(P4RuntimeTest):
    """Tests
    """

    def runTest(self):

        configure = ConfigureFabric()
        configure.configFabricPipe(self)



        self.runCase10()



    @autocleanup
    def runCase10(self):

        print('\n\nOpen Packet Test')



        mcast_group_id = 1
        mcast_ports = [HOST1_PHYSICAL_PORT]

        # Add multicast group.
        self.insert_pre_multicast_group(
            group_id=mcast_group_id,
            ports=mcast_ports)


        #Subscriber

        #Subscriber Open Packet

        # initial sn \xff\xff\xff\xff\x80\x04
        pktOpenS = create_OpenPacket(udp_payload="\x44\x0a\xff\xff\xff\xff\x80\x04\x20\x07\x0c\x2a\xc4\xe4\xa8\x1e\x33\xea\xd0\x0e\xfc\x78\xb2\xd7\x20\xdf\xc7\x76\x76\x5c\x15\xda\x64\x9b\x19\x8a\x62\xa7\x74\x7d\xa1")

        # Send packet...
        testutils.send_packet(self, HOST1_PHYSICAL_PORT, str(pktOpenS))
        print_inline("Open Packet Sent... ")

        #Controller OpenAck Packet

        # initial sn \xff\xff\80\x70
        pktOpenAckS = create_OpenAckPacket(udp_payload="\x64\x0a\xff\xff\x80\x70")


        packet_out_msg = self.helper.build_packet_out(
                payload=str(pktOpenAckS),
                metadata={
                    "egress_port": HOST1_PHYSICAL_PORT,
                    "_pad": 0
                })

        # Send message and expect packet on the given data plane port.
        self.send_packet_out(packet_out_msg)



        testutils.verify_packet(self, pktOpenAckS, HOST1_PHYSICAL_PORT)

        # sn \xff\xff\xff\xff\x80\x04
        pktSubDeclareResource = create_DeclareResourcePacket(udp_payload="\x2a\xff\xff\xff\xff\x80\x04\x0b" \
"\x01\x01\x01\x00\x14\x2f\x6d\x79\x68\x6f\x6d\x65\x2f\x6b\x69\x74" \
"\x63\x68\x65\x6e\x2f\x74\x65\x6d\x70")

        # Send packet...
        testutils.send_packet(self, HOST1_PHYSICAL_PORT, str(pktSubDeclareResource))
        print_inline("Resource Packet Sent... ")


        # sn \x80\x80\x80\x80\x81\x04
        pktSubDeclareSubscriber = create_DeclareSubscriberPacket(udp_payload="\x2a\x80\x80\x80\x80\x81\x04\x0b\x01\xa3\x01")

        # Send packet...
        testutils.send_packet(self, HOST1_PHYSICAL_PORT, str(pktSubDeclareSubscriber))
        print_inline("Subscriber Packet Sent... ")


        pktClose = create_ClosePacket()

        # Send packet...
        testutils.send_packet(self, HOST1_PHYSICAL_PORT, str(pktClose))

        pktCloseController = create_ClosePacket(eth_dst= SWITCH1_MAC ,eth_src=HOST1_MAC, ip_src=HOST1_IP, ip_dst=HOST1_GW, udp_dport= ZENOH_UDP_PORT, udp_sport=HOST1_UDP_PORT )


        packet_out_msg = self.helper.build_packet_out(
                payload=str(pktCloseController),
                metadata={
                    "egress_port": HOST1_PHYSICAL_PORT,
                    "_pad": 0
                })

        # Send message and expect packet on the given data plane port.
        self.send_packet_out(packet_out_msg)



        testutils.verify_no_other_packets(self)


