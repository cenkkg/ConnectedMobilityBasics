import json
import jmespath


probe_dict = {
    'home': './home_probes.json',
    'lte': './lte_probes.json',
    'starlink': './starlink_probes.json',
    'wlan': './wlan_probes.json'
}


def probe_selector():
    with open('./probes.json', 'r') as r_file:
        probe_data = json.load(r_file)
        for k, v in probe_dict.items():
            filtered_probes = jmespath.search(f"objects[?contains(tags, '{k}') && status_name == 'Connected']", probe_data)
            with open(v, 'w') as w_file:
                w_file.write(json.dumps(filtered_probes))


if __name__ == "__main__":
    probe_selector()
