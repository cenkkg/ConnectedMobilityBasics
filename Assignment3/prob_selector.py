import csv
import json
import random

import jmespath

probe_dict = {
    'home': ['./home_probes.json', './home_probes_100.json', './home_probes_ids_100.csv'],
    'lte': ['./lte_probes.json', './lte_probes_100.json', './lte_probes_ids_100.csv'],
    'starlink': ['./starlink_probes.json', './starlink_probes_100.json', './starlink_probes_ids_100.csv'],
    'wlan': ['./wlan_probes.json', './wlan_probes_100.json', './wlan_probes_ids_100.csv']
}


def probe_selector():
    with open('./probes.json', 'r') as r_file:
        probe_data = json.load(r_file)
        for k, v in probe_dict.items():
            filtered_probes = jmespath.search(f"objects[?contains(tags, '{k}') && status_name == 'Connected']",
                                              probe_data)
            with open(v[0], 'w') as w_file:
                w_file.write(json.dumps(filtered_probes))
            n_no_probes = []
            n_no_probes_ids = []
            rand_no = random.sample(range(0, len(filtered_probes)), min(100, len(filtered_probes)))
            for el in rand_no:
                n_no_probes.append(filtered_probes[el])
                n_no_probes_ids.append(str(filtered_probes[el]['id']))
            with open(v[1], 'w') as w_file:
                w_file.write(json.dumps(n_no_probes))
            with open(v[2], 'w') as w_file:
                w_file.write(",".join(n_no_probes_ids))


if __name__ == "__main__":
    probe_selector()
