import sys
import json
import pandas
import matplotlib.pyplot as plt
import subprocess
import numpy as np
if __name__ == '__main__':
    subprocess.check_call(["python", '-m', 'pip', 'install', 'matplotlib'])

    args = sys.argv
    if(len(args)!=3):
        print("Usage: <json> <column_name>")
        exit()
    column_name = args[2]
    final = {}
    with open(args[1]) as json_file:
        json_obj = json.load(json_file)
        files_obj = json_obj['files']
        for f_obj in files_obj:
            with open(f_obj['file_name']) as f:
                parsedCsv = pandas.read_csv(f,usecols=[column_name])
                values = [x[0] for  x in parsedCsv.values]
                avg = sum(values)/len(values)

            final.setdefault(f_obj['main_variant'],[]).append({'secondaries' : f_obj['secondaries'], 'value' : avg})
    ax = plt.subplot(111)
    x = np.array(range(final.keys()))
    plt.ylabel(column_name)
    plt.xticks(range(3), final.keys())
    ax.bar(x - 0.2, final['lla'], width=0.2, color='b', align='center', label="least loaded")
    ax.bar(x, final['greedy'], width=0.2, color='g', align='center', label="greedy")
    ax.bar(x + 0.2, final['mla'], width=0.2, color='r', align='center', label="most loaded")
    plt.legend()
    plt.show()

