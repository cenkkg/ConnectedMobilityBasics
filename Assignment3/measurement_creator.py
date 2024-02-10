import requests


all_measurements = []
url = 'https://atlas.ripe.net/api/v2/measurements/'
headers = {'Authorization': 'Key 0796c526-8023-441e-acd8-4bec0b273f23'}



for each_measurement in all_measurements:
    probes = ""
    for each_probe in each_measurement.probes:
        probes += str(each_probe) + ","
    probes = probes[:-1]

    data = {
        "definitions": [
            {
                "target": "ripe.net",
                "description": each_measurement.name,
                "type": "ping",
                "af": 4
            }
        ],
        "probes": [
            {
                "requested": 3,
                "type": "probes",
                "value": probes
            }
        ]
    }

    response = requests.post(url, headers=headers, json=data)

    print('Status Code:', response.status_code)
    print('Content:', response.text)
