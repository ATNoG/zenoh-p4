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
from datetime import datetime
import logging
import argparse
import time
import zenoh
from zenoh.net import config, SubInfo, Reliability, SubMode




# --- Command line argument parsing --- --- --- --- --- ---
parser = argparse.ArgumentParser(
    prog='zn_sub',
    description='zenoh-net sub example')
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
parser.add_argument('--selector', '-s', dest='selector',
                    default='/myhome/kitchen/temp',
                    type=str,
                    help='The selection of resources to subscribe.')
parser.add_argument('--config', '-c', dest='config',
                    metavar='FILE',
                    type=str,
                    help='A configuration file.')
parser.add_argument('--host', type=str, dest='host', required=False)
parser.add_argument('--topic', type=str, dest='topic', required=False)
parser.add_argument('--topics_to_listen', type=str, dest='listen', required=False)
        

args = parser.parse_args()
conf = zenoh.config_from_file(args.config) if args.config is not None else {}
if args.mode is None:
    conf["mode"] = 'client'
if args.peer is None:
    conf["peer"] = 'udp/10.0.3.10:7447'
if args.listener is not None:
    conf["listener"] = ",".join(args.listener)
selector = args.selector


idx=0
overall_data1 = ''
overall_data2 = ''
overall_data3 = ''
overall_data4 = ''
overall_data5 = ''
started = False

def listener1(sample):
    
        global started
        started = True
        data = ">> [Subscription listener] Received ('{}': '{}') ".format(sample.res_name, sample.payload.decode("utf-8"))
        #print(data)
        global idx
        idx += 1 #sample.payload.decode("utf-8").strip('[]').split(' ')[0].strip()
        global overall_data1
        overall_data1 += '{},{}\n'.format(str(sample.payload.decode("utf-8").split(']')[0].strip().strip('[')), time.time())


def listener2(sample):
    
        global started
        started = True
        data = ">> [Subscription listener] Received ('{}': '{}') ".format(sample.res_name, sample.payload.decode("utf-8"))
        #print(data)
        global idx
        idx += 1 #sample.payload.decode("utf-8").strip('[]').split(' ')[0].strip()
        global overall_data2 
        overall_data2 += '{},{}\n'.format(str(sample.payload.decode("utf-8").split(']')[0].strip().strip('[')), time.time())
        

def listener3(sample):
    
        global started
        started = True
        data = ">> [Subscription listener] Received ('{}': '{}') ".format(sample.res_name, sample.payload.decode("utf-8"))
        #print(data)
        global idx
        idx += 1 #sample.payload.decode("utf-8").strip('[]').split(' ')[0].strip()
        global overall_data3 
        overall_data3 += '{},{}\n'.format(str(sample.payload.decode("utf-8").split(']')[0].strip().strip('[')), time.time())
        

def listener4(sample):
    
        global started
        started = True
        data = ">> [Subscription listener] Received ('{}': '{}') ".format(sample.res_name, sample.payload.decode("utf-8"))
        #print(data)
        global idx
        idx += 1 #sample.payload.decode("utf-8").strip('[]').split(' ')[0].strip()
        global overall_data4 
        overall_data4 += '{},{}\n'.format(str(sample.payload.decode("utf-8").split(']')[0].strip().strip('[')), time.time())
        

def listener5(sample):
    
        global started
        started = True
        data = ">> [Subscription listener] Received ('{}': '{}') ".format(sample.res_name, sample.payload.decode("utf-8"))
        #print(data)
        global idx
        idx += 1 #sample.payload.decode("utf-8").strip('[]').split(' ')[0].strip()
        global overall_data5 
        overall_data5 += '{},{}\n'.format(str(sample.payload.decode("utf-8").split(']')[0].strip().strip('[')), time.time())
        


try:
    # zenoh-net code  --- --- --- --- --- --- --- --- --- --- ---
        

    # initiate logging
    zenoh.init_logger()

    time_init = time.time()
    
    print("Openning session...")
    session = zenoh.net.open(conf)
    
    listeners = {'1': listener1, '2': listener2, '3': listener3, '4': listener4, '5': listener5 }
    print("Declaring Subscriber on '{}'...".format(selector))
    sub_info = SubInfo(Reliability.BestEffort, SubMode.Push)
    
    
    sub1 = session.declare_subscriber(str(selector) + str(args.topic), sub_info, listener1)
    sub2 = session.declare_subscriber(selector + "2", sub_info, listener2)
    sub3 = session.declare_subscriber(selector + "3", sub_info, listener3)
    sub4 = session.declare_subscriber(selector + "4", sub_info, listener4)
    sub5 = session.declare_subscriber(selector + "5", sub_info, listener5)
    
    print("Press q to stop...")
    c = '\0'
    #while c != 'q':
    #    c = sys.stdin.read(1)
    while True:
        if(started):
            idx_val_in = idx
            time.sleep(10)
            idx_val_out = idx
            
            if(idx_val_in == idx_val_out):
                break
        #idx+=1
    #    with open("/tmp/sub_idx.txt", "a") as f_idx:
    #        if(idx != 0):
     #           f_idx.write(str(idx))
        
        
        
        pass
    sub1.undeclare()
    sub2.undeclare()
    sub3.undeclare()
    sub4.undeclare()
    sub5.undeclare()
    
    session.close()
    with open("/tmp/sub_" + str(args.host) + "_topic" + str(args.topic)+ ".txt", "w") as f_out:
        #f_out.write(str(time_init) + '\n')
        f_out.write(str(overall_data1))
        #f_out.write("last packet: " +  str(last_packet))
        #f_out.write("packets received: " +  str(idx))
    
    with open("/tmp/sub_" + str(args.host) + "_topic2" + ".txt", "w") as f_out:
        #f_out.write(str(time_init) + '\n')
        f_out.write(str(overall_data2))
        #f_out.write("last packet: " +  str(last_packet))
        #f_out.write("packets received: " +  str(idx))
    with open("/tmp/sub_" + str(args.host) + "_topic3" + ".txt", "w") as f_out:
        #f_out.write(str(time_init) + '\n')
        f_out.write(str(overall_data3))
        #f_out.write("last packet: " +  str(last_packet))
        #f_out.write("packets received: " +  str(idx))
    with open("/tmp/sub_" + str(args.host) + "_topic4" +".txt", "w") as f_out:
        #f_out.write(str(time_init) + '\n')
        f_out.write(str(overall_data4))
        #f_out.write("last packet: " +  str(last_packet))
        #f_out.write("packets received: " +  str(idx))
    
    with open("/tmp/sub_" + str(args.host) + "_topic5" + ".txt", "w") as f_out:
        #f_out.write(str(time_init) + '\n')
        f_out.write(str(overall_data5))
        #f_out.write("last packet: " +  str(last_packet))
        #f_out.write("packets received: " +  str(idx))
    
        
except Exception as e:
    with open("/tmp/suberror.txt", "a") as file_err:
        file_err.write(str(e) + '\n')
    
