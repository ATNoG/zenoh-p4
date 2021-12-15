
import subprocess


def start_wireshark(infra):

    for key in infra:
        for eth_num in infra[key]:
            command = subprocess.run(['docker', 'exec', '-it', '-d', 'mininet', '../mininet/host-cmd', str(key), 'tcpdump', '-i', str(key) + '-eth' + str(eth_num), '-U', '-w', '../tmp/' + str(key) + '-eth' + str(eth_num) + '.pcap' ], stdout=subprocess.PIPE, text=True)
            if command.returncode == 0:
                print('Capture started on ' + str(key) + '-eth' + str(eth_num))



if __name__ == "__main__":

    infra = { 'h1' : [0], 'h2' : [0],'h3' : [0],'h4' : [0],'h5' : [0],'h6' : [0],'h7' : [0],'h8' : [0],'h9' : [0],'h10' : [0], 'h11' : [0],'h12' : [0],'h13' : [0],'h14' : [0],'h15' : [0],'h16' : [0],'h17' : [0],'h18' : [0],'h19' : [0],'h20' : [0], 'sw1' : [1,2,3,4,5,6,7,8,9,10], 'sw2' : [1,2,3], 'sw3' : [1,2,3,4,5,6,7,8,9,10], 'sw4' : [1,2],'sw5' : [1,2],'sw6' : [1,2], 'sw7' : [1,2],'sw8' : [1,2],'sw9' : [1,2],'sw10' : [1,2],'sw11' : [1,2],'sw12' : [1,2],'sw13' : [1,2], 'sw14' : [1,2],'sw15' : [1,2],'sw16' : [1,2], 'sw17' : [1,2],'sw18' : [1,2],'sw19' : [1,2],'sw20' : [1,2] }

    start_wireshark(infra)
