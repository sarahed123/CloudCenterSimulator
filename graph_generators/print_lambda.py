import sys
from to_mat import *
from numpy import linalg as LA
import json

graph_file = sys.argv[1]
N = int(sys.argv[2])
try:
	with open(graph_file, 'r') as f:
   		 list_graph = json.load(f)
	print(list_graph)
	graph = list_graph_to_mat(list_graph)
except Exception as e:
	print(e)
	graph = to_mat(graph_file,N)
eig,vecs = LA.eig(graph)
print(eig)
eig = np.abs(eig)
eig.sort()
print(eig[-2])

