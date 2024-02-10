import csv
import json
import random
import matplotlib.pyplot as plt
from sklearn.cluster import KMeans
import jmespath

probe_dict = {
    'wifi_home': {
        'query': "contains(tags, 'home') && "
                 "(contains(tags, 'wi-fi') || contains(tags, 'wifi') || "
                 "contains(tags, 'wlan') || contains(tags, 'wireless') || "
                 "contains(tags, 'wireless-link'))",
        'file_json': "./wifi_home_probes.json",
        'file_100_json': "./wifi_home_probes_{}.json",
        'file_100_csv': "./wifi_home_probes_ids_{}.csv"
    },
    'lte': {
        'query': "contains(tags, 'lte') || contains(tags, '5g') || "
                 "contains(tags, '4g') || contains(tags, '3g') || "
                 "contains(tags, 'cellular')",
        'file_json': "./lte_probes.json",
        'file_100_json': "./lte_probes_{}.json",
        'file_100_csv': "./lte_probes_ids_{}.csv"
    },
    'sat': {
        'query': "asn_v4 == to_number('14593')",
        'file_json': "./sat_probes.json",
        'file_100_json': "./sat_probes_{}.json",
        'file_100_csv': "./sat_probes_ids_{}.csv"
    },
    'ethernet': {
        'query': "contains(tags, 'home') && "
                 "(contains(tags, 'dsl') || contains(tags, 'adsl') || "
                 "contains(tags, 'fibre') || contains(tags, 'fiber') || "
                 "contains(tags, 'cable'))",
        'file_json': "./ethernet_probes.json",
        'file_100_json': "./ethernet_probes_{}.json",
        'file_100_csv': "./ethernet_probes_ids_{}.csv"
    },
}


def probe_selector():
    with open('./probes.json', 'r') as r_file:
        probe_data = json.load(r_file)
        for probe_dict_k, probe_dict_v in probe_dict.items():
            # query probes with type
            filtered_probes = jmespath.search(f"objects[?({probe_dict_v['query']}) "
                                              f"&& status_name == 'Connected' && status == to_number('1')]",
                                              probe_data)
            # generate clusters of probes
            filtered_probes_long = [p['longitude'] for p in filtered_probes]
            filtered_probes_lat = [p['latitude'] for p in filtered_probes]
            cord = list(zip(filtered_probes_long, filtered_probes_lat))
            kmeans = KMeans(n_clusters=25)
            kmeans.fit(cord)
            # display cluster plots
            plt.scatter(filtered_probes_long, filtered_probes_lat, c=kmeans.labels_)
            plt.title(probe_dict_k)
            plt.show()
            # create probe records
            with open(probe_dict_v['file_json'], 'w') as w_file:
                w_file.write(json.dumps(filtered_probes))
            # select probes
            n_no_probes = []
            n_no_probes_ids = []
            if len(filtered_probes) <= 100:
                n_no_probes = filtered_probes
                n_no_probes_ids = [str(p['id']) for p in filtered_probes]
            else:
                # select from clusters
                print(probe_dict_k)
                cluster_dict = {}
                len_probes = len(filtered_probes)
                for i in range(len_probes):
                    if kmeans.labels_[i] not in cluster_dict:
                        cluster_dict[kmeans.labels_[i]] = []
                    cluster_dict[kmeans.labels_[i]].append(filtered_probes[i])
                miss_count = 0
                for _, cluster_dict_v in cluster_dict.items():
                    if miss_count > 0 and len(cluster_dict_v) > 4:
                        tmp_fetch = min(len(cluster_dict_v), 4+miss_count)
                    else:
                        tmp_fetch = min(len(cluster_dict_v), 4)
                    miss_count += 4 - tmp_fetch
                    tmp = cluster_dict_v[:tmp_fetch]
                    for el in tmp:
                        n_no_probes.append(el)
                        n_no_probes_ids.append(str(el['id']))
                print(miss_count)
            # create selected probes json
            with open(probe_dict_v['file_100_json'].format(len(n_no_probes)), 'w') as w_file:
                w_file.write(json.dumps(n_no_probes))
            # create selected probes ids csv
            with open(probe_dict_v['file_100_csv'].format(len(n_no_probes)), 'w') as w_file:
                w_file.write(",".join(n_no_probes_ids))
            # display selected probes plots
            selected_probes_long = [pr['longitude'] for pr in n_no_probes]
            selected_probes_lat = [pr['latitude'] for pr in n_no_probes]
            plt.scatter(selected_probes_long, selected_probes_lat)
            plt.title("selected " + probe_dict_k)
            plt.show()


if __name__ == "__main__":
    probe_selector()
