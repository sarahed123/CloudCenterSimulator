import sys
import json
import pandas
import random
import matplotlib.pyplot as plt
import subprocess
import numpy as np

def cmp_to_key(mycmp):
    'Convert a cmp= function into a key= function'
    class K(object):
        def __init__(self, obj, *args):
            self.obj = obj
        def __lt__(self, other):
            return mycmp(self.obj, other.obj) < 0
        def __gt__(self, other):
            return mycmp(self.obj, other.obj) > 0
        def __eq__(self, other):
            return mycmp(self.obj, other.obj) == 0
        def __le__(self, other):
            return mycmp(self.obj, other.obj) <= 0
        def __ge__(self, other):
            return mycmp(self.obj, other.obj) >= 0
        def __ne__(self, other):
            return mycmp(self.obj, other.obj) != 0
    return K

def files_sort(a,b):
    if(len(a['main_variant']) > len(b['main_variant'])):
        return -1
    if (len(a['main_variant']) < len(b['main_variant'])):
        return 1
    if(a['main_variant'] > b['main_variant']) :
        return -1
    return 1

if __name__ == '__main__':

    args = sys.argv
    if(len(args)!=3):
        print("Usage: <json> <column_name>")
        exit()
    column_name = args[2]
    final = {}
    secondaries = {}
    with open(args[1]) as json_file:
        json_obj = json.load(json_file)
        files_obj = json_obj['files']
        files_obj = sorted(files_obj,key = cmp_to_key(files_sort))
        for f_obj in files_obj:
            with open(f_obj['file_name']) as f:
                parsedCsv = pandas.read_csv(f,usecols=[column_name])
                values = [x[0] for  x in parsedCsv.values]
                avg = sum(values)/len(values)

            final.setdefault(f_obj['main_variant'],{}).setdefault(frozenset(f_obj['secondaries']),avg)
            secondaries.setdefault(frozenset(f_obj['secondaries']),[]).append(avg)
    r = lambda: random.randint(0, 255)
    colormap = {}
    ax = plt.subplot(111)
    x = np.array(range(len(final.keys())))
    plt.ylabel(column_name)
    plt.xticks(np.array(x)+0.2, final.keys())

    for j,secondary in enumerate(secondaries.keys()):
        ax.bar(x + j*0.2, secondaries[secondary], width=0.2,  align='center', label=str([s for s in secondary]).strip('[]').replace('\'',''),color=colormap.setdefault(secondary,'#%02X%02X%02X' % (r(),r(),r())))
    #ax.bar(x - 0.2, final['lla'], width=0.2, color='b', align='center', label="least loaded")
    #ax.bar(x, final['greedy'], width=0.2, color='g', align='center', label="greedy")
    #ax.bar(x + 0.2, final['mla'], width=0.2, color='r', align='center', label="most loaded")
    plt.legend()
    plt.show()

