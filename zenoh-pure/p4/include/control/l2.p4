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

#include "../define.p4"
#include "../header.p4"

control L2 (inout parsed_headers_t hdr,
             inout fabric_metadata_t fabric_metadata,
             inout standard_metadata_t standard_metadata) {

   
    action bridging(bit<48> dstMac, bit<9> port) {
        hdr.ethernet.src_addr = hdr.ethernet.dst_addr;
        hdr.ethernet.dst_addr = dstMac;
        standard_metadata.egress_spec = port;
        fabric_metadata.skipAll = _TRUE;

    }
    
    

    table l2_bridging {
        key = {
            hdr.ipv4.dst_addr: exact @name("dstAddr");
        }

        actions = {
            bridging;
            
        }

    }

    apply {
        l2_bridging.apply();
    }
}
