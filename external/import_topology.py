import sys
import os
from shutil import copyfile
import errno
sys.path.insert(0, '/cs/labs/schapiram/inonkp/netbench_runs/python/')
from import_topology import import_topology
print("usage: python import_topology.py topology_path N d server_num? lanes?\n")



N = int(sys.argv[2])
d = int(sys.argv[3])
s = int(sys.argv[4]) if len(sys.argv) > 4 else 0
lanes = int(sys.argv[5]) if len(sys.argv) > 5 else 1

#logs_dir = "n" + str(N) + "/d" + str(d)
properties_dir = "/cs/labs/schapiram/inonkp/small_graphs/runs/" + f"n{N}/d{d}" + "/netbench/"
import_topology(properties_dir,sys.argv[1],N,d,s,lanes)

