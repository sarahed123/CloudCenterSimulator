import sys
from to_mat import *
from numpy import linalg as LA
import json
import argparse
import os

parser = argparse.ArgumentParser()
parser.add_argument("--gs", help="graphs", nargs="+", required=True, dest="graphs")
parser.add_argument("-N", help="vertex num", type=int, required=True, dest="N")
args = parser.parse_args()

#graph_file = sys.argv[1]
N = args.N
for graph_file in args.graphs:
	try:
		with open(graph_file, 'r') as f:
   			list_graph = json.load(f)
		print(list_graph)
		graph = list_graph_to_mat(list_graph)
	except Exception as e:
		#print("error " + str(e))
		graph = to_mat(graph_file,N)
		#exit()
	eig,vecs = LA.eig(graph)
	eig = np.abs(eig)
	eig.sort()
	print(os.path.basename(graph_file) ,eig[-2],eig[-1]-eig[-2], sep=",")

