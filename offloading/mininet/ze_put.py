# Copyright (c) 2017, 2020 ADLINK Technology Inc.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License 2.0 which is available at
# http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
# which is available at https://www.apache.org/licenses/LICENSE-2.0.
#
# SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
#
# Contributors:
#   ADLINK zenoh team, <zenoh@adlink-labs.tech>

import sys
import time
import argparse
import itertools
import zenoh
from zenoh.net import config
import logging



# --- Command line argument parsing --- --- --- --- --- ---
parser = argparse.ArgumentParser(
    prog='zn_pub',
    description='zenoh-net pub example')
parser.add_argument('--mode', '-m', dest='mode',
                    choices=['peer', 'client'],
                    type=str,
                    help='The zenoh session mode.')
parser.add_argument('--peer', '-e', dest='peer',
                    metavar='LOCATOR',
                    action='append',
                    type=str,
                    help='Peer locators used to initiate the zenoh session.')
parser.add_argument('--listener', '-l', dest='listener',
                    metavar='LOCATOR',
                    action='append',
                    type=str,
                    help='Locators to listen on.')
parser.add_argument('--path', '-p', dest='path',
                    default='/myhome/kitchen/temp',
                    type=str,
                    help='The name of the resource to publish.')
parser.add_argument('--value', '-v', dest='value',
                    default='25',
                    type=str,
                    help='The value of the resource to publish.')
parser.add_argument('--config', '-c', dest='config',
                    metavar='FILE',
                    type=str,
                    help='A configuration file.')
parser.add_argument('--host', type=str, dest='host', required=False)
    
parser.add_argument('--topic', type=str, dest='topic', required=False)
    
args = parser.parse_args()
conf = zenoh.config_from_file(args.config) if args.config is not None else {}
if args.mode is None:
    conf["mode"] = 'client'
if args.peer is None:
    conf["peer"] = 'udp/192.168.1.1:7447'
if args.listener is not None:
    conf["listener"] = ",".join(args.listener)
path = args.path
value = args.value

# zenoh-net code  --- --- --- --- --- --- --- --- --- --- ---

# initiate logging
l=zenoh.init_logger()
logging.debug(l)

time_init = time.time()

print("Openning session...")
session = zenoh.net.open(conf)

print("Declaring Resource " + path)
rid = session.declare_resource(path + args.topic)
print(" => RId {}".format(rid))

print("Declaring Publisher on {}".format(rid))
publisher = session.declare_publisher(rid)

time.sleep(10)
overall_data = ''
for idx in range(0,1000):
    buf = "[{:4d}] {}".format(idx, value)
    #print("Writing Data ('{}': '{}')...".format(rid, buf))
    session.write(rid, bytes(buf, encoding='utf8'))
    #file_o.write(buf)
    #file_o.write('\n')
    overall_data += '{},{}'.format(idx, time.time()) + '\n'
    time.sleep(0.1)
    

with open("/tmp/pub_"+ str(args.host)+ ".txt", "w") as file_o:
    file_o.write(str(overall_data))
        


publisher.undeclare()
session.close()
