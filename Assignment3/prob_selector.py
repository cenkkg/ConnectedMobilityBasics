import json
import jmespath

PROBES = {'home': [], 'lte': [], 'starlink': [], 'wlan': []}
PROBE_TYPES = ['home', 'lte', 'starlink', 'wlan']

def probe_selector(probe_type):
    with open('probes.json', 'r') as file:
        probe_data = json.load(file)
        jmespath.search('objects[*][].age', probe_data)







        # number_of_probes = len(all_probes)
        # while True:
        #     probe_index = random.randint(0, number_of_probes)
        #     if all_probes[probe_index]['status_name'] == 'Connected' and  all_probes[probe_index]['status_name']
