import sys
import requests


all_measurements = {}
url = 'https://atlas.ripe.net/api/v2/measurements/'
headers = {'Authorization': 'Key 0991e154-dc6e-4289-8cf1-8b266149779f'}

with open('./selected_probes_for_measurement/selected_probes_' + sys.argv[1] + '.txt', 'r') as file:
    selected_probes_data = file.readlines()[:10]

    target_data_center_list = selected_probes_data[1].strip().split(', ')
    for each_target_data_center in target_data_center_list:
        all_measurements[each_target_data_center] = {}

        if selected_probes_data[3] != "x":
            all_measurements[each_target_data_center]['ethernet'] = selected_probes_data[3].strip().replace(' ', '')
        else:
            all_measurements[each_target_data_center]['ethernet'] = ""
        if selected_probes_data[5] != "x":
            all_measurements[each_target_data_center]['lte'] = selected_probes_data[5].strip().replace(' ', '')
        else:
            all_measurements[each_target_data_center]['lte'] = ""
        if selected_probes_data[7] != "x":
            all_measurements[each_target_data_center]['sat'] = selected_probes_data[7].strip().replace(' ', '')
        else:
            all_measurements[each_target_data_center]['sat'] = ""
        if selected_probes_data[9] != "x":
            all_measurements[each_target_data_center]['wifi'] = selected_probes_data[9].strip().replace(' ', '')
        else:
            all_measurements[each_target_data_center]['wifi'] = ""


for each_target_data_center in all_measurements:
    ethernet_probes = all_measurements[each_target_data_center]['ethernet']
    lte_probes = all_measurements[each_target_data_center]['lte']
    sat_probes = all_measurements[each_target_data_center]['sat']
    wifi_probes = all_measurements[each_target_data_center]['wifi']

    if ethernet_probes:
        data_ethernet = {
        "definitions": [
            {
                "target": each_target_data_center,
                "description": each_target_data_center + " (ETHERNET)",
                "type": "ping",
                "af": 4,
                "interval": 14400
            }
        ],
        "probes": [
            {
                "requested": 1,
                "type": "probes",
                "value": ethernet_probes
            }
        ],
        "stop_time": "2024-02-18T10:00:00Z"
    }
        response_ethernet = requests.post(url, headers=headers, json=data_ethernet)
    if lte_probes:
        data_lte = {
        "definitions": [
            {
                "target": each_target_data_center,
                "description": each_target_data_center + " (LTE)",
                "type": "ping",
                "af": 4,
                "interval": 14400
            }
        ],
        "probes": [
            {
                "requested": 1,
                "type": "probes",
                "value": lte_probes
            }
        ],
        "stop_time": "2024-02-18T10:00:00Z"
    }
        response_lte = requests.post(url, headers=headers, json=data_lte)
    if sat_probes:
        data_sat = {
        "definitions": [
            {
                "target": each_target_data_center,
                "description": each_target_data_center + " (SAT)",
                "type": "ping",
                "af": 4,
                "interval": 14400
            }
        ],
        "probes": [
            {
                "requested": 1,
                "type": "probes",
                "value": sat_probes
            }
        ],
        "stop_time": "2024-02-18T10:00:00Z"
    }
        response_sat = requests.post(url, headers=headers, json=data_sat)
    if wifi_probes:
        data_wifi = {
        "definitions": [
            {
                "target": each_target_data_center,
                "description": each_target_data_center + " (WIFI)",
                "type": "ping",
                "af": 4,
                "interval": 14400
            }
        ],
        "probes": [
            {
                "requested": 1,
                "type": "probes",
                "value": wifi_probes
            }
        ],
        "stop_time": "2024-02-18T10:00:00Z"
    }
        response_wifi = requests.post(url, headers=headers, json=data_wifi)

    print('Status Code:', response_ethernet.status_code)
    print('Status Code:', response_lte.status_code)
    print('Status Code:', response_sat.status_code)
    print('Status Code:', response_wifi.status_code)
