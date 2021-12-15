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

#include <core.p4>
#include <v1model.p4>



#include "../header.p4"


#define register_entries (1 << 16)-1

control IngressZenoh (inout parsed_headers_t hdr,
              inout fabric_metadata_t fabric_metadata,
              inout standard_metadata_t standard_metadata) {


    /**
    *
    *
    *   REGISTERS WHERE ZENOH IN SERIAL NUMBERS WILL BE STORED, SINCE SN CAN RANGE FROM 1 TO 10 BYTES, 10 REGISTERS NEED TO BE CREATED, WHERE EACH ONE WILL STORE A BYTE
    *
    */
    register<bit<8>>(register_entries) serial_number_byte0;
    register<bit<8>>(register_entries) serial_number_byte1;
    register<bit<8>>(register_entries) serial_number_byte2;
    register<bit<8>>(register_entries) serial_number_byte3;
    register<bit<8>>(register_entries) serial_number_byte4;
    register<bit<8>>(register_entries) serial_number_byte5;
    register<bit<8>>(register_entries) serial_number_byte6;
    register<bit<8>>(register_entries) serial_number_byte7;
    register<bit<8>>(register_entries) serial_number_byte8;
    register<bit<8>>(register_entries) serial_number_byte9;


    /**
    *
    *
    *   REGISTER USED TO CHECK IF THE TOPIC HAS BEEN CONFIGURED OR NOT
    *
    */
    register<bit<1>>(register_entries) topic_configured;


    /**
    *
    *
    *   TEMPORARY BYTE VARIABLES USED TO STORE REGISTER VALUES
    *
    */
    bit<8> tmp_read0;
    bit<8> tmp_read1;
    bit<8> tmp_read2;
    bit<8> tmp_read3;
    bit<8> tmp_read4;
    bit<8> tmp_read5;
    bit<8> tmp_read6;
    bit<8> tmp_read7;
    bit<8> tmp_read8;
    bit<8> tmp_read9;


    // variable used to store index
    bit<32> index_reg = 0x00;

    bit<8> exists;
    bit<1> match = 0;
    bit<1> configured = 0;


    action computeHash(bit<32> ipv4_address, bit<16> udp_port){

        hash(index_reg, HashAlgorithm.crc32, (bit<32>)0, {ipv4_address, udp_port}, (bit<32>)register_entries);

    }


    action storeSN(){
        serial_number_byte0.read(tmp_read0, index_reg);
        serial_number_byte1.read(tmp_read1, index_reg);
        serial_number_byte2.read(tmp_read2, index_reg);
        serial_number_byte3.read(tmp_read3, index_reg);
        serial_number_byte4.read(tmp_read4, index_reg);
        serial_number_byte5.read(tmp_read5, index_reg);
        serial_number_byte6.read(tmp_read6, index_reg);
        serial_number_byte7.read(tmp_read7, index_reg);
        serial_number_byte8.read(tmp_read8, index_reg);
        serial_number_byte9.read(tmp_read9, index_reg);
        
    }


    action compareSN(){

        if(hdr.byte_stack[0].elem == tmp_read0 && hdr.byte_stack[1].elem == tmp_read1 && hdr.byte_stack[2].elem == tmp_read2
        && hdr.byte_stack[3].elem == tmp_read3 && hdr.byte_stack[4].elem == tmp_read4 && hdr.byte_stack[5].elem == tmp_read5
        && hdr.byte_stack[6].elem == tmp_read6 && hdr.byte_stack[7].elem == tmp_read7 && hdr.byte_stack[8].elem == tmp_read8
        && hdr.byte_stack[9].elem == tmp_read9){

            match = 1;

        }
    }



    action checkExists(){
        serial_number_byte0.read(exists, index_reg);
    }




    action set_multicast(bit<32> multicast_group_id){
        standard_metadata.mcast_grp = (bit<16>) multicast_group_id;
        fabric_metadata.is_multicast = true;
    }


    table TopicSubscriberGroup{
        key = {
            fabric_metadata.zenoh_resourceId : exact;
        }
        actions = {
            set_multicast;
        }

    }

    apply{

        /**
        *
        *   IN CASE IT IS A ZENOH CLOSE SENT FROM THE CONTROLLER THEN REGISTER MUST BE WIPED
        *
        */
        if(hdr.zenoh_header.msgId == CLOSE_PACKET && fabric_metadata.is_controller_packet_out){

            //controller did not alter package, only checks for peer id and closes session
            computeHash(hdr.ipv4.src_addr, hdr.udp.sport);
            serial_number_byte0.write(index_reg, 0);
            serial_number_byte1.write(index_reg, 0);
            serial_number_byte2.write(index_reg, 0);
            serial_number_byte3.write(index_reg, 0);
            serial_number_byte4.write(index_reg, 0);
            serial_number_byte5.write(index_reg, 0);
            serial_number_byte6.write(index_reg, 0);
            serial_number_byte7.write(index_reg, 0);
            serial_number_byte8.write(index_reg, 0);
            serial_number_byte9.write(index_reg, 0);

            // No further action is needed
            exit;


        /**
        *
        *
        *   OTHERWISE INDEX MUST BE COMPUTED
        *
        */
        }else if(hdr.zenoh_header.msgId == CLOSE_PACKET){

            //packet must be sent to controller to check for peer id and end session
            exit;


        }else{

            if(fabric_metadata.isZenohData){

                //fabric_metadata.applyACL = _FALSE;
                topic_configured.read(configured, (bit<32>) fabric_metadata.zenoh_resourceId);
            }

            computeHash(hdr.ipv4.src_addr, hdr.udp.sport);

            /**
            *   IF ZENOH OPEN MESSAGE STORE SERIAL NUMBER ON THE REGISTERS
            */
            if(fabric_metadata.isZenohOpen && (hdr.zenoh_header.flags & (bit<3>) 0x01 == 0x00)){
                serial_number_byte0.write(index_reg, hdr.byte_stack[0].elem);
                serial_number_byte1.write(index_reg, hdr.byte_stack[1].elem);
                serial_number_byte2.write(index_reg, hdr.byte_stack[2].elem);
                serial_number_byte3.write(index_reg, hdr.byte_stack[3].elem);
                serial_number_byte4.write(index_reg, hdr.byte_stack[4].elem);
                serial_number_byte5.write(index_reg, hdr.byte_stack[5].elem);
                serial_number_byte6.write(index_reg, hdr.byte_stack[6].elem);
                serial_number_byte7.write(index_reg, hdr.byte_stack[7].elem);
                serial_number_byte8.write(index_reg, hdr.byte_stack[8].elem);
                serial_number_byte9.write(index_reg, hdr.byte_stack[9].elem);

                if(!fabric_metadata.is_controller_packet_out){
                    fabric_metadata.send_to_cpu = 1;
                }else{
                    mark_to_drop(standard_metadata);
                    exit;
                }
                
            }
            
            if(fabric_metadata.isZenohData && configured == 0){
                if(fabric_metadata.is_controller_packet_out){
                    topic_configured.write((bit<32>) fabric_metadata.zenoh_resourceId, 1);
                    configured = 1;
                }else{

                    fabric_metadata.send_to_cpu = 1;

                    fabric_metadata.applyACL = _TRUE;
                }



            /**
            *    OTHERWISE CHECK FOR HOST EXISTANCE, MATCH OF SERIAL NUMBER AND CONSEQUENT UPDATE
            */
            }else{
                if(!fabric_metadata.is_controller_packet_out){
                    checkExists();
                    if(exists != 0x00){
                        storeSN();
                        compareSN();
                        /**
                         * 
                         *  IF NOT MATCH THEN SERIAL NEW SERIAL NUMBER IS STORED, MUST BE ONLY USED IN BEST EFFORT CASE 
                         * 
                         */
                        if(match != 1){
                            serial_number_byte0.write(index_reg, hdr.byte_stack[0].elem);
                            serial_number_byte1.write(index_reg, hdr.byte_stack[1].elem);
                            serial_number_byte2.write(index_reg, hdr.byte_stack[2].elem);
                            serial_number_byte3.write(index_reg, hdr.byte_stack[3].elem);
                            serial_number_byte4.write(index_reg, hdr.byte_stack[4].elem);
                            serial_number_byte5.write(index_reg, hdr.byte_stack[5].elem);
                            serial_number_byte6.write(index_reg, hdr.byte_stack[6].elem);
                            serial_number_byte7.write(index_reg, hdr.byte_stack[7].elem);
                            serial_number_byte8.write(index_reg, hdr.byte_stack[8].elem);
                            serial_number_byte9.write(index_reg, hdr.byte_stack[9].elem);
                            
                            storeSN();
                        }
                        /**
                        *    UPDATE OF SERIAL NUMBER
                        */
                        if( tmp_read0 != 0xFF ){
                            tmp_read0 = tmp_read0 + 1;
                            serial_number_byte0.write(index_reg, tmp_read0);

                        }else{
                            tmp_read0 = 0x80;
                            serial_number_byte0.write(index_reg, tmp_read0);
                            if( tmp_read1 != 0xFF ){
                                tmp_read1 = tmp_read1 + 1;
                                serial_number_byte1.write(index_reg, tmp_read1);
                            }else{
                                tmp_read1 = 0x80;
                                serial_number_byte1.write(index_reg, tmp_read1);
                                if( tmp_read2 != 0xFF ){
                                    tmp_read2 = tmp_read2 + 1;
                                    serial_number_byte2.write(index_reg, tmp_read2);
                                }else{
                                    tmp_read2 = 0x80;
                                    serial_number_byte2.write(index_reg, tmp_read2);
                                    if( tmp_read3 != 0xFF ){
                                        tmp_read3 = tmp_read3 + 1;
                                        serial_number_byte3.write(index_reg, tmp_read3);
                                    }else{
                                        tmp_read3 = 0x80;
                                        serial_number_byte3.write(index_reg, tmp_read3);
                                        if( tmp_read4 != 0xFF ){
                                            tmp_read4 = tmp_read4 + 1;
                                            serial_number_byte4.write(index_reg, tmp_read4);
                                        }else{
                                            tmp_read4 = 0x80;
                                            serial_number_byte4.write(index_reg, tmp_read4);
                                            if( tmp_read5 != 0xFF ){
                                                tmp_read5 = tmp_read5 + 1;
                                                serial_number_byte5.write(index_reg, tmp_read5);
                                            }else{
                                                tmp_read5 = 0x80;
                                                serial_number_byte5.write(index_reg, tmp_read5);
                                                if( tmp_read6 != 0xFF ){
                                                    tmp_read6 = tmp_read6 + 1;
                                                    serial_number_byte6.write(index_reg, tmp_read6);
                                                }else{
                                                    tmp_read6 = 0x80;
                                                    serial_number_byte6.write(index_reg, tmp_read6);
                                                    if( tmp_read7 != 0xFF ){
                                                        tmp_read7 = tmp_read7 + 1;
                                                        serial_number_byte7.write(index_reg, tmp_read7);
                                                    }else{
                                                        tmp_read7 = 0x80;
                                                        serial_number_byte7.write(index_reg, tmp_read7);
                                                        if( tmp_read8 != 0xFF ){
                                                            tmp_read8 = tmp_read8 + 1;
                                                            serial_number_byte8.write(index_reg, tmp_read8);
                                                        }else{
                                                            tmp_read8 = 0x80;
                                                            serial_number_byte8.write(index_reg, tmp_read8);
                                                            if( tmp_read9 != 0xFF ){
                                                                tmp_read9 = tmp_read9 + 1;
                                                                serial_number_byte9.write(index_reg, tmp_read9);
                                                            }else{
                                                                tmp_read9 = 0x80;
                                                                serial_number_byte9.write(index_reg, tmp_read9);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            }
            if(configured == 1){
                TopicSubscriberGroup.apply();
                fabric_metadata.applyACL = _FALSE;
            }
        }

    }
}




control EgressZenoh (inout parsed_headers_t hdr,
              inout fabric_metadata_t fabric_metadata,
              inout standard_metadata_t standard_metadata) {



    /**
    *
    *
    *   REGISTERS WHERE ZENOH OUT SERIAL NUMBERS WILL BE STORED, SINCE SN CAN RANGE FROM 1 TO 10 BYTES, 10 REGISTERS NEED TO BE CREATED, WHERE EACH ONE WILL STORE A BYTE
    *
    */

    register<bit<8>>(register_entries) serial_number_byte0;
    register<bit<8>>(register_entries) serial_number_byte1;
    register<bit<8>>(register_entries) serial_number_byte2;
    register<bit<8>>(register_entries) serial_number_byte3;
    register<bit<8>>(register_entries) serial_number_byte4;
    register<bit<8>>(register_entries) serial_number_byte5;
    register<bit<8>>(register_entries) serial_number_byte6;
    register<bit<8>>(register_entries) serial_number_byte7;
    register<bit<8>>(register_entries) serial_number_byte8;
    register<bit<8>>(register_entries) serial_number_byte9;


    /**
    *
    *
    *   TEMPORARY BYTE VARIABLES USED TO STORE REGISTER VALUES
    *
    */
    bit<8> tmp_read0;
    bit<8> tmp_read1;
    bit<8> tmp_read2;
    bit<8> tmp_read3;
    bit<8> tmp_read4;
    bit<8> tmp_read5;
    bit<8> tmp_read6;
    bit<8> tmp_read7;
    bit<8> tmp_read8;
    bit<8> tmp_read9;


    bit<32> index_reg = 0x00;
    bit<8> exists;
    bit<1> match = 0;


    action computeHash(bit<32> ipv4_address, bit<16> udp_port){

        hash(index_reg, HashAlgorithm.crc32, (bit<32>)0, {ipv4_address, udp_port}, (bit<32>)register_entries);

    }


    action extractSN(){

        serial_number_byte0.read(tmp_read0, index_reg);
        serial_number_byte1.read(tmp_read1, index_reg);
        serial_number_byte2.read(tmp_read2, index_reg);
        serial_number_byte3.read(tmp_read3, index_reg);
        serial_number_byte4.read(tmp_read4, index_reg);
        serial_number_byte5.read(tmp_read5, index_reg);
        serial_number_byte6.read(tmp_read6, index_reg);
        serial_number_byte7.read(tmp_read7, index_reg);
        serial_number_byte8.read(tmp_read8, index_reg);
        serial_number_byte9.read(tmp_read9, index_reg);
    }





    //router eth, sub eth, router ip, sub ip, sub udp port
    action sendDatatoSub(bit<48> src_eth, bit<48> dst_eth, bit<32> src_ip, bit<32> dst_ip, bit<16> udp_dport){
        hdr.ethernet.dst_addr = dst_eth;
        hdr.ethernet.src_addr = src_eth;
        hdr.ipv4.src_addr = src_ip;
        hdr.ipv4.dst_addr = dst_ip;
        hdr.udp.sport = 7447;
        hdr.ipv4.ttl = 64;
        hdr.udp.dport = udp_dport;

    }

    table SendToSubscriber{
        key = {
            standard_metadata.egress_port: exact;
            standard_metadata.mcast_grp: exact;
        }

        actions = {
            sendDatatoSub;
        }


    }



    apply{


        if(hdr.zenoh_header.msgId == KEEPALIVE_PACKET ){
            exit;
        }

        /**
        *
        *   IN CASE IT IS A ZENOH CLOSE SENT FROM THE CONTROLLER THEN REGISTERS MUST BE WIPED AND PACKET DROPED
        *
        */
        if(hdr.zenoh_header.msgId == CLOSE_PACKET && fabric_metadata.is_controller_packet_out){


            //controller did not alter package, only checks for peer id and closes session
            computeHash(hdr.ipv4.src_addr, hdr.udp.sport);
            serial_number_byte0.write(index_reg, 0);
            serial_number_byte1.write(index_reg, 0);
            serial_number_byte2.write(index_reg, 0);
            serial_number_byte3.write(index_reg, 0);
            serial_number_byte4.write(index_reg, 0);
            serial_number_byte5.write(index_reg, 0);
            serial_number_byte6.write(index_reg, 0);
            serial_number_byte7.write(index_reg, 0);
            serial_number_byte8.write(index_reg, 0);
            serial_number_byte9.write(index_reg, 0);

            mark_to_drop(standard_metadata);

            // No further action is needed
            exit;


        /**
        *
        * IF IS ZENOH OPEN ACK PACKET, THE SWITCH MUST STORE THE SERIAL NUMBER THAT MUST BE SENT TO THE CLIENT ON FURTHER PACKETS
        *
        */
        }else if(hdr.zenoh_header.flags & (bit<3>) 0x01 == 0x01 && fabric_metadata.isZenohOpen){
            computeHash(hdr.ipv4.dst_addr, hdr.udp.dport);

            serial_number_byte0.write(index_reg, hdr.byte_stack[0].elem);
            serial_number_byte1.write(index_reg, hdr.byte_stack[1].elem);
            serial_number_byte2.write(index_reg, hdr.byte_stack[2].elem);
            serial_number_byte3.write(index_reg, hdr.byte_stack[3].elem);
            serial_number_byte4.write(index_reg, hdr.byte_stack[4].elem);
            serial_number_byte5.write(index_reg, hdr.byte_stack[5].elem);
            serial_number_byte6.write(index_reg, hdr.byte_stack[6].elem);
            serial_number_byte7.write(index_reg, hdr.byte_stack[7].elem);
            serial_number_byte8.write(index_reg, hdr.byte_stack[8].elem);
            serial_number_byte9.write(index_reg, hdr.byte_stack[9].elem);
            
            if(fabric_metadata.l4_sport == 7447 && fabric_metadata.l4_dport == 7447 ){
                mark_to_drop(standard_metadata);
            }


        }else if ( (fabric_metadata.is_controller_packet_out && (hdr.zenoh_inner_header.msgId == DECLARE )) || (fabric_metadata.isZenohData && (fabric_metadata.send_to_cpu != 1 ) ) ) {

            if(fabric_metadata.is_multicast && fabric_metadata.isZenohData){
                SendToSubscriber.apply();

            }

            // can only compute hash after alteration header fields
            computeHash(hdr.ipv4.dst_addr, hdr.udp.dport);

            // save register value to temporary variables
            extractSN();

            // change header serial number to match with the one stored on registers
            hdr.byte_stack[0].elem = tmp_read0;
            hdr.byte_stack[1].elem = tmp_read1;
            hdr.byte_stack[2].elem = tmp_read2;
            hdr.byte_stack[3].elem = tmp_read3;
            hdr.byte_stack[4].elem = tmp_read4;
            hdr.byte_stack[5].elem = tmp_read5;
            hdr.byte_stack[6].elem = tmp_read6;
            hdr.byte_stack[7].elem = tmp_read7;
            hdr.byte_stack[8].elem = tmp_read8;
            hdr.byte_stack[9].elem = tmp_read9;

            /**
            *    UPDATE OF SERIAL NUMBER (MUST BE UPDATED AT EVERY PACKET)
            */
            if( tmp_read0 != 0xFF ){
                tmp_read0 = tmp_read0 + 1;
                serial_number_byte0.write(index_reg, tmp_read0);

            }else{
                tmp_read0 = 0x80;
                serial_number_byte0.write(index_reg, tmp_read0);
                if( tmp_read1 != 0xFF ){
                    tmp_read1 = tmp_read1 + 1;
                    serial_number_byte1.write(index_reg, tmp_read1);
                }else{
                    tmp_read1 = 0x80;
                    serial_number_byte1.write(index_reg, tmp_read1);
                    if( tmp_read2 != 0xFF ){
                        tmp_read2 = tmp_read2 + 1;
                        serial_number_byte2.write(index_reg, tmp_read2);
                    }else{
                        tmp_read2 = 0x80;
                        serial_number_byte2.write(index_reg, tmp_read2);
                        if( tmp_read3 != 0xFF ){
                            tmp_read3 = tmp_read3 + 1;
                            serial_number_byte3.write(index_reg, tmp_read3);
                        }else{
                            tmp_read3 = 0x80;
                            serial_number_byte3.write(index_reg, tmp_read3);
                            if( tmp_read4 != 0xFF ){
                                tmp_read4 = tmp_read4 + 1;
                                serial_number_byte4.write(index_reg, tmp_read4);
                            }else{
                                tmp_read4 = 0x80;
                                serial_number_byte4.write(index_reg, tmp_read4);
                                if( tmp_read5 != 0xFF ){
                                    tmp_read5 = tmp_read5 + 1;
                                    serial_number_byte5.write(index_reg, tmp_read5);
                                }else{
                                    tmp_read5 = 0x80;
                                    serial_number_byte5.write(index_reg, tmp_read5);
                                    if( tmp_read6 != 0xFF ){
                                        tmp_read6 = tmp_read6 + 1;
                                        serial_number_byte6.write(index_reg, tmp_read6);
                                    }else{
                                        tmp_read6 = 0x80;
                                        serial_number_byte6.write(index_reg, tmp_read6);
                                        if( tmp_read7 != 0xFF ){
                                            tmp_read7 = tmp_read7 + 1;
                                            serial_number_byte7.write(index_reg, tmp_read7);
                                        }else{
                                            tmp_read7 = 0x80;
                                            serial_number_byte7.write(index_reg, tmp_read7);
                                            if( tmp_read8 != 0xFF ){
                                                tmp_read8 = tmp_read8 + 1;
                                                serial_number_byte8.write(index_reg, tmp_read8);
                                            }else{
                                                tmp_read8 = 0x80;
                                                serial_number_byte8.write(index_reg, tmp_read8);
                                                if( tmp_read9 != 0xFF ){
                                                    tmp_read9 = tmp_read9 + 1;
                                                    serial_number_byte9.write(index_reg, tmp_read9);
                                                }else{
                                                    tmp_read9 = 0x80;
                                                    serial_number_byte9.write(index_reg, tmp_read9);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }

    }
}
