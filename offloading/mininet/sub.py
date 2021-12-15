from zenoh import Zenoh, ChangeKind
import logging

import time

def listener(change):
    if change.kind == ChangeKind.PUT:
        data = 'Publication received: "{}" = "{}"'.format(change.path, change.value)

        logging.info(data)

if __name__ == "__main__":z

    logging.basicConfig(filename='log_file.log', level=logging.DEBUG)

    z = Zenoh({'mode':'client', 'peer':'udp/10.0.1.100:7447'})
    w = z.workspace('/')
    results = w.subscribe('/myhome/kitchen/temp', listener)
