import numpy as np
import sys
from matrix import Matrix
from p_matrix import PMatrix
import argparse
from pprint import pprint
def create_matrices(curr,parent):
	curr = curr.remainder(q)
	if parent is not None:
		
		parent = parent.remainder(q)
		#parentTup = tuple(np.array(parent).flatten())
		edges[parent] = edges.get(parent,set())
		edges[parent].add(curr)
		edges[curr] = edges.get(curr,set())
		edges[curr].add(parent)
	if curr in SLZ3:
		return
	SLZ3.append(curr)
	
	create_matrices(curr.dot(A),curr)
	create_matrices(curr.dot(B),curr)
	create_matrices(curr.dot(AI),curr)
	create_matrices(curr.dot(BI),curr)

def make_graph():
	
	print(len(SLZ3))
	indices = {}
	index = 0
	for u in edges:
		indices[u] = index
		index += 1
	pprint(edges)
	d = 4 if not args.psl else 3
	with open("SLZ_n" + str(len(edges)) + "_d" + str(d) + ".topology", "w") as out:
		for v in edges:
			assert len(edges[v])==d
			for u in edges[v]:
				assert v in edges[u]
				out.write(str(indices[v]) + " " + str(indices[u]) + "\n")
		out.close()

def create_matrix(M, psl):
	return Matrix(M) if not psl else PMatrix(M)

parser = argparse.ArgumentParser()
parser.add_argument("--q", type=int, dest="q", required=True)
parser.add_argument("--psl", type=bool, dest="psl", default=False)
args = parser.parse_args()

A = np.matrix([[0,-1],[1,0]])
AI = create_matrix(A.I, args.psl)
A = create_matrix(A, args.psl)

B = np.matrix([[1,0],[1,1]])
BI = create_matrix(B.I, args.psl)
B = create_matrix(B, args.psl)

print(A,AI,B,BI)

q = args.q

SLZ3 = []
i = 0
I = Matrix(np.matrix([[1,0],[0,1]])) if not args.psl else PMatrix(np.matrix([[1,0],[0,1]]))
edges = {}
#SLZ3.append((1,0,1,0))
create_matrices(I,None)
make_graph()
