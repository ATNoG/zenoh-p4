import requests
import json

def change_config(sw, new_pipeconf, ip = None, sid = None):

    delete_req = requests.delete('http://localhost:8181/onos/v1/network/configuration/devices/' + str(sw) + '/basic', auth=('onos', 'rocks'))
    print(delete_req)
    if delete_req.status_code == 204:
        print(str(sw) + ' config deleted sucessfuly' )

    new_basic_config = {'managementAddress': 'grpc://mininet:50001?device_id=1',
        'driver': 'stratum-bmv2',
        'pipeconf': new_pipeconf,
        'locType': 'grid',
        'gridX': 600,
        'gridY': 600,
        'purgeOnDisconnection': True }


    post_req = requests.post('http://localhost:8181/onos/v1/network/configuration/devices/' + str(sw) + '/basic', auth=('onos', 'rocks'), data = json.dumps(new_basic_config))

    if post_req.status_code == 200:
        print(str(sw) + ' config posted sucessfuly' )


if __name__ == '__main__':

    sw = 'device:sw1'
    new_pipeconf = 'org.onosproject.new-pipelineV2.fabric'
    change_config(sw, new_pipeconf)