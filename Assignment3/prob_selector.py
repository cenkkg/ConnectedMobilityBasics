import csv
import json
import random

import jmespath

probe_dict = {
    'wifi_home': {
        'query': "contains(tags, 'home') && "
                 "(contains(tags, 'wi-fi') || contains(tags, 'wifi') || "
                 "contains(tags, 'wlan') || contains(tags, 'wireless') || "
                 "contains(tags, 'wireless-link'))",
        'file_json': "./wifi_home_probes.json",
        'file_100_json': "./wifi_home_probes_100.json",
        'file_100_csv': "./wifi_home_probes_ids_100.csv"
    },
    'lte': {
        'query': "contains(tags, 'lte') || contains(tags, '5g') || "
                 "contains(tags, '4g') || contains(tags, '3g') || "
                 "contains(tags, 'cellular')",
        'file_json': "./lte_probes.json",
        'file_100_json': "./lte_probes_100.json",
        'file_100_csv': "./lte_probes_ids_100.csv"
    },
    'sat': {
        'query': "asn_v4 == to_number('14593')",
        'file_json': "./sat_probes.json",
        'file_100_json': "./sat_probes_100.json",
        'file_100_csv': "./sat_probes_ids_100.csv"
    },
    'ethernet': {
        'query': "contains(tags, 'home') && "
                 "(contains(tags, 'dsl') || contains(tags, 'adsl') || "
                 "contains(tags, 'fibre') || contains(tags, 'fiber') || "
                 "contains(tags, 'cable'))",
        'file_json': "./ethernet_probes.json",
        'file_100_json': "./ethernet_probes_100.json",
        'file_100_csv': "./ethernet_probes_ids_100.csv"
    },
}


def probe_selector():
    with open('./probes.json', 'r') as r_file:
        probe_data = json.load(r_file)
        for k, v in probe_dict.items():
            filtered_probes = jmespath.search(f"objects[?({v['query']}) "
                                              f"&& status_name == 'Connected' && status == to_number('1')]",
                                              probe_data)
            with open(v['file_json'], 'w') as w_file:
                w_file.write(json.dumps(filtered_probes))
            n_no_probes = []
            n_no_probes_ids = []
            rand_no = random.sample(range(0, len(filtered_probes)), min(100, len(filtered_probes)))
            for el in rand_no:
                n_no_probes.append(filtered_probes[el])
                n_no_probes_ids.append(str(filtered_probes[el]['id']))
            with open(v['file_100_json'], 'w') as w_file:
                w_file.write(json.dumps(n_no_probes))
            with open(v['file_100_csv'], 'w') as w_file:
                w_file.write(",".join(n_no_probes_ids))


if __name__ == "__main__":
    probe_selector()
