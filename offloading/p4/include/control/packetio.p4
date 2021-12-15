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

#include "../header.p4"

control PacketIoIngress(inout parsed_headers_t hdr,
                        inout fabric_metadata_t fabric_metadata,
                        inout standard_metadata_t standard_metadata) {

    apply {

        if (hdr.packet_out.isValid()) {
            standard_metadata.egress_spec = hdr.packet_out.egress_port;
            hdr.packet_out.setInvalid();
            fabric_metadata.is_controller_packet_out = _TRUE;

            if(fabric_metadata.isZenoh){
                fabric_metadata.applyACL = _FALSE;
            }
            //zenoh close packets, even the ones sent from controller need to be processed
            if(hdr.zenoh_header.msgId != CLOSE_PACKET && fabric_metadata.isZenohData == _FALSE && !(fabric_metadata.isZenohOpen && (hdr.zenoh_header.flags & (bit<3>) 0x01 == 0x00)) ){
                // No need for ingress processing, straight to egress.
                exit;
            }
        
        }

    }
}

control PacketIoEgress(inout parsed_headers_t hdr,
                       inout fabric_metadata_t fabric_metadata,
                       inout standard_metadata_t standard_metadata) {

    apply {
        if (fabric_metadata.is_controller_packet_out == _TRUE) {
            // Transmit right away.
            exit;
        }
        if (standard_metadata.egress_port == CPU_PORT) {
            hdr.packet_in.setValid();
            hdr.packet_in.ingress_port = standard_metadata.ingress_port;
            // No need to process through the rest of the pipeline.
            exit;
        }
    }
}
