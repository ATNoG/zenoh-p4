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

#ifndef __CHECKSUM__
#define __CHECKSUM__

#ifdef WITH_SPGW
#include "control/spgw.p4"
#endif // WITH_SPGW


control ComputeUDPCheckSum(inout parsed_headers_t hdr,
                              inout fabric_metadata_t meta){
    apply{
        update_checksum_with_payload(hdr.udp.isValid() && meta.isZenohData,
            {   
                hdr.ipv4.src_addr,              //ip pseudo header
                hdr.ipv4.dst_addr,              //*
                16w0x0011,                      //*
                hdr.udp.len,                    //*
                hdr.udp.sport,                  //udp header without checksum
                hdr.udp.dport,                  //*
                hdr.udp.len,                    //*
                hdr.zenoh_header,               //zenoh frame header
                hdr.byte_stack[0].elem,         //Serial number
                hdr.byte_stack[1].elem,         //*
                hdr.byte_stack[2].elem,         //*
                hdr.byte_stack[3].elem,         //*
                hdr.byte_stack[4].elem,         //*
                hdr.byte_stack[5].elem,         //*
                hdr.byte_stack[6].elem,         //*
                hdr.byte_stack[7].elem,         //*
                hdr.byte_stack[8].elem,         //*
                hdr.byte_stack[9].elem,         //*
                hdr.zenoh_inner_header          //zenoh data header
            },
            hdr.udp.checksum,
            HashAlgorithm.csum16
        );
        
    }
                                  
                                  
}

control FabricComputeChecksum(inout parsed_headers_t hdr,
                              inout fabric_metadata_t meta)
{
    apply {
        update_checksum(hdr.ipv4.isValid(),
            {
                hdr.ipv4.version,
                hdr.ipv4.ihl,
                hdr.ipv4.dscp,
                hdr.ipv4.ecn,
                hdr.ipv4.total_len,
                hdr.ipv4.identification,
                hdr.ipv4.flags,
                hdr.ipv4.frag_offset,
                hdr.ipv4.ttl,
                hdr.ipv4.protocol,
                hdr.ipv4.src_addr,
                hdr.ipv4.dst_addr
            },
            hdr.ipv4.hdr_checksum,
            HashAlgorithm.csum16
        );
        
        ComputeUDPCheckSum.apply(hdr, meta);
        
#ifdef WITH_SPGW
        update_gtpu_checksum.apply(hdr.gtpu_ipv4, hdr.gtpu_udp, hdr.gtpu,
                                   hdr.ipv4, hdr.udp);
#endif // WITH_SPGW
    }
}

control FabricVerifyChecksum(inout parsed_headers_t hdr,
                             inout fabric_metadata_t meta)
{
    apply {
        verify_checksum(hdr.ipv4.isValid(),
            {
                hdr.ipv4.version,
                hdr.ipv4.ihl,
                hdr.ipv4.dscp,
                hdr.ipv4.ecn,
                hdr.ipv4.total_len,
                hdr.ipv4.identification,
                hdr.ipv4.flags,
                hdr.ipv4.frag_offset,
                hdr.ipv4.ttl,
                hdr.ipv4.protocol,
                hdr.ipv4.src_addr,
                hdr.ipv4.dst_addr
            },
            hdr.ipv4.hdr_checksum,
            HashAlgorithm.csum16
        );
    }
}

#endif
