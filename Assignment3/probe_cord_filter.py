import json

import jmespath

json_files = ['./ethernet_probes_100.json', './lte_probes_74.json',
              './sat_probes_39.json', './wifi_home_probes_19.json']


def probe_cord_filter(low_long, up_long, low_lat, up_lat):
    for f in json_files:
        with open(f, 'r') as r_file:
            probe_data = json.load(r_file)
            filtered_probes = jmespath.search(f"[?latitude >= to_number('{low_lat}') &&"
                                              f" latitude < to_number('{up_lat}') && "
                                              f"longitude >= to_number('{low_long}') && "
                                              f"longitude < to_number('{up_long}')].id"
                                              , probe_data)
            str_filtered_probes = [str(el) for el in filtered_probes]
            print(f)
            print(', '.join(str_filtered_probes))

    def is_in_range_cords(el):
        return low_long <= el[0] < up_long and low_lat <= el[1] < up_lat

    datacenter_locations = [[-78, 39, '52.46.142.78'], [-121, 37, '176.32.118.30'], [-121, 44, '52.94.214.88'],
                            [114.1577, 22.2855, '13.248.36.100'], [72.8774, 19.0760, '52.95.84.21'],
                            [135.5022, 34.6942, '13.248.4.70'], [18.4229, -33.9256, '99.78.128.100'],
                            [-46.6372, -23.5471, '177.72.245.178'], [-6.2672, 53.3442, '52.95.127.226'],
                            [151.2070, -33.8678, '103.8.174.238'], [-0.1257, 51.5085, '8.208.40.164'],
                            [55.3047, 25.2585, '47.91.99.18'], [151.2070, -33.8678, '47.74.79.155'],
                            [103.8503, 1.2900, '161.117.155.93'],
                            [8.6820, 50.1109, '47.254.186.91'], [-118.2439, 34.0526, '47.88.111.29'],
                            [-122.2348, 47.3809, '108.61.194.105'], [-80.2364, 25.8118, '104.156.244.232'],
                            [-46.6372, -23.5471, '216.238.98.118'], [2.3843, 48.9165, '108.61.209.127'],
                            [103.8503, 1.2900, '45.32.100.168'], [151.2003, -33.9022, '108.61.212.117'],
                            [-84.3875, 33.7488, '192.155.94.157'], [-96.7298, 32.9483, ',50.116.25.154'],
                            [8.6820, 50.1109, '139.162.130.8'], [151.2070, -33.8678, '172.105.174.7'],
                            [121.5636, 25.0382, '152.32.231.6'], [3.3958, 6.4530, '23.248.185.157'],
                            [55.3047, 25.2585, '23.248.184.34'], [121.4689, 31.2243, '117.50.123.23'],
                            [151.2070, -33.8678, '134.70.92.3'], [-46.6372, -23.5471, '134.70.84.3'],
                            [139.6923, 35.6895, '134.70.80.3'], [8.5498, 47.3668, '134.70.88.3'],
                            [-73.5878, 45.5088, '134.70.116.1'], [28.0436, -26.2022, '134.70.160.1']]
    filtered_datacenters = filter(is_in_range_cords, datacenter_locations)
    filtered_data_center_targets = [el[2] for el in filtered_datacenters]
    print("Target IPs")
    print(', '.join(filtered_data_center_targets))


if __name__ == "__main__":
    probe_cord_filter(-10, 10, 45, 60)
