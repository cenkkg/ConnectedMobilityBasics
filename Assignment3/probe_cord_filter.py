import json

import jmespath

json_files = ['./ethernet_probes_100.json', './lte_probes_100.json',
              './sat_probes_39.json', './wifi_home_probes_62.json']


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


if __name__ == "__main__":
    probe_cord_filter(140, 160, -35, -30)
