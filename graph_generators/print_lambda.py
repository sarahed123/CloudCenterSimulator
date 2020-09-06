import sys
from to_mat import *
from numpy import linalg as LA
import json
import argparse
import os

def calc_eigs(graph_file, N, self_loops = False):
	try:
		with open(graph_file, 'r') as f:
   			list_graph = json.load(f)
		print(list_graph)
		graph = list_graph_to_mat(list_graph)
	except Exception as e:
		#print("error " + str(e))
		graph = to_mat(graph_file, N, self_loops)
		#exit()
	eig,vecs = LA.eig(graph)
	eig = np.abs(eig)
	eig.sort()
	print(os.path.basename(graph_file) ,eig[-2],eig[-1]-eig[-2], sep=",")

