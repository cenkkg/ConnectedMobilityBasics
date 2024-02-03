import json
import random

import jmespath

probe_dict = {
    'home': ['./home_probes.json', './home_probes_100.json'],
    'lte': ['./lte_probes.json', './lte_probes_100.json'],
    'starlink': ['./starlink_probes.json', './starlink_probes_100.json'],
    'wlan': ['./wlan_probes.json', './wlan_probes_100.json']
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
            rand_no = random.sample(range(0, len(filtered_probes)), min(100, len(filtered_probes)))
            for el in rand_no:
                n_no_probes.append(filtered_probes[el])
            with open(v[1], 'w') as w_file:
                w_file.write(json.dumps(n_no_probes))


if __name__ == "__main__":
    probe_selector()
