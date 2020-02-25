import numpy as np
import sys

def create_matrices(curr,parent):
	curr = np.remainder(curr,q)
        tup = tuple(np.array(curr).flatten())
	if parent is not None:
		
		parent = np.remainder(parent,q)
		parentTup = tuple(np.array(parent).flatten())
		edges[parentTup] = edges.get(parentTup,set())
		edges[parentTup].add(tup)
		edges[tup] = edges.get(tup,set())
		edges[tup].add(parentTup)
	if tup in SLZ3:
		return
	SLZ3.append(tup)
	
	create_matrices(curr.dot(A),curr)
        create_matrices(curr.dot(B),curr)
        create_matrices(curr.dot(AI),curr)
        create_matrices(curr.dot(BI),curr)
A = np.matrix([[0,-1],[1,0]])
AI = A.I

B = np.matrix([[1,0],[1,1]])
BI = B.I
q = int(sys.argv[1])

SLZ3 = []
i = 0
I = np.matrix([[1,0],[0,1]])
edges = {}
#SLZ3.append((1,0,1,0))
create_matrices(I,None)
print(len(SLZ3))
indices = {}
index = 0
for u in edges:
	indices[u] = index
	index += 1
print(edges)
with open("SLZ_n" + str(len(edges)) + "_d" + str(4) + ".topology", "w") as out:
	for v in edges:
		for u in edges[v]:
			out.write(str(indices[v]) + " " + str(indices[u]) + "\n")
out.close()
