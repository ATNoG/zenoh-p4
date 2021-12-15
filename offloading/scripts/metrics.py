
import time

import os

def main():


    cwd = os.getcwd()
    jitter = 0
    last_time_packet = 0

    overall_jitter =0
    overall_latency  =0
    total_packets_lost =0


    for i in range(1,6):
        pub_dict = {}
        sub_dict = {}
        jitter=0

        try:
            with open(cwd + '/tmp/pub_h' +str(10+i)+'.txt', 'r') as pub_file:
                allLines = pub_file.readlines()

                for line in allLines:
                    try:
                        id, time_id = line.split(',')
                        id = id.strip()
                        time_id = time_id.strip()

                        pub_dict[int(id)] = float(time_id)

                    except Exception as e:
                        print(str(e))


            with open(cwd + '/tmp/sub_h' +str(i)+'_topic' +str(i)+'.txt', 'r') as sub_file:
                allLines = sub_file.readlines()

                last_time_packet = float(allLines[0].split(',')[1].strip())
                last_time_id = int(allLines[0].split(',')[0].strip())
                sub_dict[int(last_time_id)] = float(last_time_packet)

                for line in allLines[1:]:
                    try:
                        id, time_id = line.split(',')
                        id = id.strip()
                        time_id = time_id.strip()

                        jitter += abs((pub_dict[int(id)] - float(time_id))  - (pub_dict[last_time_id] - last_time_packet))
                        last_time_packet = float(time_id)
                        last_time_id = int(id)

                        sub_dict[int(id)] = float(time_id)

                    except Exception as e:
                        print(e)
                        pass

        except Exception as e:
            print(str(e))
            print('Overall latency: ' + str(overall_latency/(i-1)))
            print('Overall jitter: ' + str(overall_jitter/(i-1)))
            print('Total packets Lost: ' + str(total_packets_lost))
            return




        sum_times = 0
        for key in sub_dict.keys():
            sum_times += abs(sub_dict[key] - pub_dict[key])
            if(pub_dict[key] > sub_dict[key]):
                    print(str(i))
                    print(str(key))
            



        overall_latency += (sum_times/len(sub_dict.keys()))
        overall_jitter += (jitter/((len(sub_dict.keys()) -1 )))
        total_packets_lost +=( 1000 - len(sub_dict.keys()))

    print('Overall latency: ' + str(overall_latency/5))
    print('Overall jitter: ' + str(overall_jitter/5))
    print('Total packets Lost: ' + str(total_packets_lost))


if __name__ == "__main__":
    main()
