/*
 * Copyright 2017-present Open Networking Foundation
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

#ifndef __PARSER__
#define __PARSER__

#include "define.p4"

parser FabricParser (packet_in packet,
                     out parsed_headers_t hdr,
                     inout fabric_metadata_t fabric_metadata,
                     inout standard_metadata_t standard_metadata) {

    bit<6> last_ipv4_dscp = 0;

    state start {
        
        
        
        fabric_metadata.applyACL = _TRUE;
        transition select(standard_metadata.ingress_port) {
            CPU_PORT: parse_packet_out;
            default: parse_ethernet;
        }
    }

    state parse_packet_out {
        packet.extract(hdr.packet_out);
        transition parse_ethernet;
    }

    state parse_ethernet {
        packet.extract(hdr.ethernet);
        fabric_metadata.vlan_id = DEFAULT_VLAN_ID;
        transition select(packet.lookahead<bit<16>>()){
            ETHERTYPE_QINQ: parse_vlan_tag;
            ETHERTYPE_QINQ_NON_STD: parse_vlan_tag;
            ETHERTYPE_VLAN: parse_vlan_tag;
            default: parse_eth_type;
        }
    }

    state parse_vlan_tag {
        packet.extract(hdr.vlan_tag);
#ifdef WITH_BNG
        fabric_metadata.bng.s_tag = hdr.vlan_tag.vlan_id;
#endif // WITH_BNG
        transition select(packet.lookahead<bit<16>>()){
#if defined(WITH_XCONNECT) || defined(WITH_DOUBLE_VLAN_TERMINATION)
            ETHERTYPE_VLAN: parse_inner_vlan_tag;
#endif // WITH_XCONNECT || WITH_DOUBLE_VLAN_TERMINATION
            default: parse_eth_type;
        }
    }

#if defined(WITH_XCONNECT) || defined(WITH_DOUBLE_VLAN_TERMINATION)
    state parse_inner_vlan_tag {
        packet.extract(hdr.inner_vlan_tag);
#ifdef WITH_BNG
        fabric_metadata.bng.c_tag = hdr.inner_vlan_tag.vlan_id;
#endif // WITH_BNG
        transition parse_eth_type;
    }
#endif // WITH_XCONNECT || WITH_DOUBLE_VLAN_TERMINATION

    state parse_eth_type {
        packet.extract(hdr.eth_type);
        transition select(hdr.eth_type.value) {
            ETHERTYPE_MPLS: parse_mpls;
            ETHERTYPE_IPV4: parse_ipv4;
#ifdef WITH_IPV6
            ETHERTYPE_IPV6: parse_ipv6;
#endif // WITH_IPV6
#ifdef WITH_BNG
            ETHERTYPE_PPPOED: parse_pppoe;
            ETHERTYPE_PPPOES: parse_pppoe;
#endif // WITH_BNG
            default: accept;
        }
    }

#ifdef WITH_BNG
    state parse_pppoe {
        packet.extract(hdr.pppoe);
        transition select(hdr.pppoe.protocol) {
            PPPOE_PROTOCOL_MPLS: parse_mpls;
            PPPOE_PROTOCOL_IP4: parse_ipv4;
#ifdef WITH_IPV6
            PPPOE_PROTOCOL_IP6: parse_ipv6;
#endif // WITH_IPV6
            default: accept;
        }
    }
#endif // WITH_BNG

    state parse_mpls {
        packet.extract(hdr.mpls);
        fabric_metadata.mpls_label = hdr.mpls.label;
        fabric_metadata.mpls_ttl = hdr.mpls.ttl;
        // There is only one MPLS label for this fabric.
        // Assume header after MPLS header is IPv4/IPv6
        // Lookup first 4 bits for version
        transition select(packet.lookahead<bit<IP_VER_LENGTH>>()) {
            // The packet should be either IPv4 or IPv6.
            // If we have MPLS, go directly to parsing state without
            // moving to pre_ states, the packet is considered MPLS
            IP_VERSION_4: parse_ipv4;
#ifdef WITH_IPV6
            IP_VERSION_6: parse_ipv6;
#endif // WITH_IPV6
            default: parse_ethernet;
        }
    }

    state parse_ipv4 {
        packet.extract(hdr.ipv4);
        fabric_metadata.ip_proto = hdr.ipv4.protocol;
        fabric_metadata.ip_eth_type = ETHERTYPE_IPV4;
        fabric_metadata.ipv4_src_addr = hdr.ipv4.src_addr;
        fabric_metadata.ipv4_dst_addr = hdr.ipv4.dst_addr;
        last_ipv4_dscp = hdr.ipv4.dscp;
        //Need header verification?
        transition select(hdr.ipv4.protocol) {
            PROTO_TCP: parse_tcp;
            PROTO_UDP: parse_udp;
            PROTO_ICMP: parse_icmp;
            default: accept;
        }
    }

#ifdef WITH_IPV6
    state parse_ipv6 {
        packet.extract(hdr.ipv6);
        fabric_metadata.ip_proto = hdr.ipv6.next_hdr;
        fabric_metadata.ip_eth_type = ETHERTYPE_IPV6;
        transition select(hdr.ipv6.next_hdr) {
            PROTO_TCP: parse_tcp;
            PROTO_UDP: parse_udp;
            PROTO_ICMPV6: parse_icmp;
            default: accept;
        }
    }
#endif // WITH_IPV6

    state parse_tcp {
        packet.extract(hdr.tcp);
        fabric_metadata.l4_sport = hdr.tcp.sport;
        fabric_metadata.l4_dport = hdr.tcp.dport;
#ifdef WITH_INT
        transition parse_int;
#else
        transition accept;
#endif // WITH_INT
    }



    /**
    *
    * ZENOH ROUTER UDP PORT IS ALWAYS 7447
    *
    */
    state parse_udp {
        packet.extract(hdr.udp);
        fabric_metadata.l4_sport = hdr.udp.sport;
        fabric_metadata.l4_dport = hdr.udp.dport;
        transition select(hdr.udp.dport) {
#ifdef WITH_SPGW
            UDP_PORT_GTPU: parse_gtpu;
#endif // WITH_SPGW
            ZENOH_PORT: parse_zenoh_packet_type;

#ifdef WITH_INT
            default: parse_int;
#else
            default: check_zenoh_packet;
#endif // WITH_INT
        }


    }

    /**
    *
    *   MUST ALSO CHECK FOR SOURCE PORT IN CASE THE PACKET COMES FROM CONTROLLER
    *
    */
    state check_zenoh_packet{

        transition select(hdr.udp.sport) {
            ZENOH_PORT: parse_zenoh_packet_type;
            default: accept;
        }

    }

    state parse_icmp {
        packet.extract(hdr.icmp);
        transition accept;
    }


    /**
    *
    *   SCOUT AND INIT PACKETS MUST BE JUST SENT TO CONTROLLER
    *   OPEN PACKETS ARE TO BE SENT TO CONTROLLER BUT MUST BE PARSED IN ORDER TO EXTRACT SERIAL NUMBERS
    *   FRAME PACKETS, JUST LIKE DATA PACKETS MUST BE PARSED IN ORDER TO EXTRACT SERIAL NUMBER AND OTHER NEEDED FIELDS
    *
    */
    state parse_zenoh_packet_type {
        
        
        fabric_metadata.isZenoh = true;
        
        // clear byte stack
        hdr.byte_stack[0].elem = 0;
        hdr.byte_stack[1].elem = 0;
        hdr.byte_stack[2].elem = 0;
        hdr.byte_stack[3].elem = 0;
        hdr.byte_stack[4].elem = 0;
        hdr.byte_stack[5].elem = 0;
        hdr.byte_stack[6].elem = 0;
        hdr.byte_stack[7].elem = 0;
        hdr.byte_stack[8].elem = 0;
        hdr.byte_stack[9].elem = 0;
        
        packet.extract(hdr.zenoh_header);
        transition select(hdr.zenoh_header.msgId) {
            SCOUT: sessionMessage;
            INIT_PACKET: sessionMessage;
            OPEN_PACKET: parseLease;
            FRAME: look_sn;
            ATTACHMENT: parse_zenoh_attachment;
            default: accept;
        }


    }
    

    state parseLease {
        fabric_metadata.isZenohOpen = true;
        packet.extract(hdr.zenoh_open);
        transition look_sn;
    }



    /**
    *
    * LOOK_SN IN ADDICTION WITH EXTRACT_SN_BYTE ACT AS A FOR LOOP IN ORDER TO EXTRACT EACH BYTE OF THE SERIAL NUMBER INDIVIDUALLY
    *
    *
    */
    state look_sn {
        fabric_metadata.zenoh_byte_counter = fabric_metadata.zenoh_byte_counter + 1;
        transition select((packet.lookahead<bit<8>>() >> 7)  & 0x01){
            0: choose_parsing_state;
            1: extract_sn_byte;
        }
    }


    state extract_sn_byte {

        packet.extract(hdr.byte_stack.next);

        //fabric_metadata.zenoh_serial_number = (fabric_metadata.zenoh_serial_number << 8) | (bit<80>)hdr.zenoh_byte_elem.elem;
        transition look_sn;
    }




    /**
    *
    *
    *   IN CASE THE PACKET IS AN OPEN PACKET, THEN MUST BE SENT TO THE CONTROLLER
    *   OTHERWISE THE PACKET MUST PASS TO ANOTHER STAGE FOR FURTHER INSPECTION
    *
    */
    state choose_parsing_state{
        packet.extract(hdr.byte_stack.next);
        transition select(hdr.zenoh_header.msgId){
            OPEN_PACKET : accept;
            default: parse_zenoh_inner_header;
        }
    }



    /**
    *
    *
    *   DECLARE PACKETS MUST BE SENT TO CONTROLLER FOR TOPIC MANAGEMENT
    *   DATA PACKETS MUST BE PARSED AND FOLLOW THE PIPELINE, TOPIC MUST BE EXTRACTED AND SENT TO KNOWN SUBSCRIBERS
    *
    *
    */
    state parse_zenoh_inner_header {


        //packet.extract(hdr.zenoh_byte_elem);


//        fabric_metadata.zenoh_byte_counter = fabric_metadata.zenoh_byte_counter + 1;
  //      fabric_metadata.zenoh_serial_number = (fabric_metadata.zenoh_serial_number << 8) | (bit<80>)hdr.zenoh_byte_elem.elem;
//    hdr.zenoh_serial_number.setValid();

  //      hdr.zenoh_serial_number.number = fabric_metadata.zenoh_serial_number;
        packet.extract(hdr.zenoh_inner_header);
        transition select(hdr.zenoh_inner_header.msgId) {
            DECLARE: sessionMessage;
            DATA: parse_zenoh_data_topic;
            default: accept;
        }


    }




    state parse_zenoh_attachment {

        //get length of buffer
        bit<8> len_byte =  packet.lookahead<bit<8>>();
        //cast to 32 bit to be able to perform arithmetic operations
        bit<32> len =  (bit<32>) len_byte;

        //extract length and buffer and store to struct
        packet.extract(hdr.zenoh_attachment, 8 + len* 8);


        //check for next header, if exists it discard flags and save packet type
        bit<5> msgId = (bit<5>) (packet.lookahead<bit<8>>() & 0x1f ) << 3;

        transition select(msgId) {
            INIT_PACKET: sessionMessage;
            OPEN_PACKET: parseLease;
            default: accept;

        }
    }



    /**
    *
    *
    *   DATA PACKET MUST NOT BE CHANGED, ONLY NEED TO FETCH RESOURCE ID WITHOUT EXTRACTING IT
    *
    */
    state parse_zenoh_data_topic {

        fabric_metadata.isZenohData = true;

        fabric_metadata.zenoh_resourceId = packet.lookahead<bit<8>>();

        transition accept;
    }

    state sessionMessage {
        fabric_metadata.send_to_cpu = 1;
        transition accept;
    }



    /*
    state parse_zenoh_attachment_payload{
        packet.extract(hdr.zenoh_attachment_payload, hdr.zenoh_attachment_len * 8);

        transition select()

    }

    */
#ifdef WITH_SPGW
    state parse_gtpu {
        packet.extract(hdr.gtpu);
        transition parse_inner_ipv4;
    }

    state parse_inner_ipv4 {
        packet.extract(hdr.inner_ipv4);
        last_ipv4_dscp = hdr.inner_ipv4.dscp;
        transition select(hdr.inner_ipv4.protocol) {
            PROTO_TCP: parse_tcp;
            PROTO_UDP: parse_inner_udp;
            PROTO_ICMP: parse_icmp;
            default: accept;
        }
    }

    state parse_inner_udp {
        packet.extract(hdr.inner_udp);
        fabric_metadata.inner_l4_sport = hdr.inner_udp.sport;
        fabric_metadata.inner_l4_dport = hdr.inner_udp.dport;
#ifdef WITH_INT
        transition parse_int;
#else
        transition accept;
#endif // WITH_INT
    }

        state parse_inner_tcp {
        packet.extract(hdr.inner_tcp);
        fabric_metadata.inner_l4_sport = hdr.inner_tcp.sport;
        fabric_metadata.inner_l4_dport = hdr.inner_tcp.dport;
        transition accept;
    }

        state parse_inner_icmp {
        packet.extract(hdr.inner_icmp);
        transition accept;
    }
#endif // WITH_SPGW

#ifdef WITH_INT
    state parse_int {
        transition select(last_ipv4_dscp) {
            INT_DSCP &&& INT_DSCP: parse_intl4_shim;
            default: accept;
        }
    }

    state parse_intl4_shim {
        packet.extract(hdr.intl4_shim);
        transition parse_int_header;
    }

    state parse_int_header {
        packet.extract(hdr.int_header);
        // If there is no INT metadata but the INT header (plus shim and tail)
        // exists, default value of length field in shim header should be
        // INT_HEADER_LEN_WORDS.
        transition select (hdr.intl4_shim.len_words) {
            INT_HEADER_LEN_WORDS: parse_intl4_tail;
            default: parse_int_data;
        }
    }

    state parse_int_data {
#ifdef WITH_INT_SINK
        // Parse INT metadata stack, but not tail
        packet.extract(hdr.int_data, (bit<32>) (hdr.intl4_shim.len_words - INT_HEADER_LEN_WORDS) << 5);
        transition parse_intl4_tail;
#else // not interested in INT data
        transition accept;
#endif // WITH_INT_SINK
    }

    state parse_intl4_tail {
        packet.extract(hdr.intl4_tail);
        transition accept;
    }
#endif // WITH_INT
}

control FabricDeparser(packet_out packet,in parsed_headers_t hdr) {

    apply {
        packet.emit(hdr.packet_in);
#ifdef WITH_INT_SINK
        packet.emit(hdr.report_ethernet);
        packet.emit(hdr.report_eth_type);
        packet.emit(hdr.report_ipv4);
        packet.emit(hdr.report_udp);
        packet.emit(hdr.report_fixed_header);
#endif // WITH_INT_SINK
        packet.emit(hdr.ethernet);
        packet.emit(hdr.vlan_tag);
#if defined(WITH_XCONNECT) || defined(WITH_DOUBLE_VLAN_TERMINATION)
        packet.emit(hdr.inner_vlan_tag);
#endif // WITH_XCONNECT || WITH_DOUBLE_VLAN_TERMINATION
        packet.emit(hdr.eth_type);
#ifdef WITH_BNG
        packet.emit(hdr.pppoe);
#endif // WITH_BNG
        packet.emit(hdr.mpls);
#ifdef WITH_SPGW
        packet.emit(hdr.gtpu_ipv4);
        packet.emit(hdr.gtpu_udp);
        packet.emit(hdr.outer_gtpu);
#endif // WITH_SPGW
        packet.emit(hdr.ipv4);
#ifdef WITH_IPV6
        packet.emit(hdr.ipv6);
#endif // WITH_IPV6
        packet.emit(hdr.tcp);
        packet.emit(hdr.udp);
        packet.emit(hdr.icmp);
        packet.emit(hdr.zenoh_header);
        packet.emit(hdr.zenoh_open);
        packet.emit(hdr.zenoh_attachment);
        //packet.emit(hdr.zenoh_serial_number);
        packet.emit(hdr.byte_stack);
        packet.emit(hdr.zenoh_inner_header);

#ifdef WITH_SPGW
        // if we parsed a GTPU packet but did not decap it
        packet.emit(hdr.gtpu);
        packet.emit(hdr.inner_ipv4);
        packet.emit(hdr.inner_tcp);
        packet.emit(hdr.inner_udp);
        packet.emit(hdr.inner_icmp);
#endif // WITH_SPGW
#ifdef WITH_INT
        packet.emit(hdr.intl4_shim);
        packet.emit(hdr.int_header);
#ifdef WITH_INT_TRANSIT
        packet.emit(hdr.int_switch_id);
        packet.emit(hdr.int_port_ids);
        packet.emit(hdr.int_hop_latency);
        packet.emit(hdr.int_q_occupancy);
        packet.emit(hdr.int_ingress_tstamp);
        packet.emit(hdr.int_egress_tstamp);
        packet.emit(hdr.int_q_congestion);
        packet.emit(hdr.int_egress_tx_util);
#endif // WITH_INT_TRANSIT
#ifdef WITH_INT_SINK
        packet.emit(hdr.int_data);
#endif // WITH_INT_SINK
        packet.emit(hdr.intl4_tail);
#endif // WITH_INT

    }
}

#endif
