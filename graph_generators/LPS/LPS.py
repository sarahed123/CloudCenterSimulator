import sys
import numpy as np
sys.setrecursionlimit(1500)
p = int(sys.argv[1])
q = int(sys.argv[2])
LPS = []
edges = {}
Identity = np.matrix([[1,0],[0,1]])

def inGraph(tup):
	for t in LPS:
		for j in range(q):
			if tup == tuple([(j*x)%q for x in t]):
				return True
	return False

def standartize(tup):
	i = 0
	for x in tup:
		if x>0:
			i = x
			break
	for j in range(q):
		if (i*j)%q == 1:
			i = j
			break
	return tuple([(x*i)%q for x in tup])
	

def create_graph(curr,parent,generators):
	curr = np.remainder(curr,q)
	tup = tuple(np.array(curr).flatten())
	tup = standartize(tup)
	if parent is not None:
		
		parent = np.remainder(parent,q)
		parentTup = tuple(np.array(parent).flatten())
		parentTup = standartize(parentTup)
		edges[parentTup] = edges.get(parentTup,set())
		edges[parentTup].add(tup)
		edges[tup] = edges.get(tup,set())
		edges[tup].add(parentTup)
	if inGraph(tup):
		return
	print("inserting: " + str(tup) + " size " + str(len(LPS)))
	LPS.append(tup)
	for gen in generators:
		create_graph(curr.dot(gen),curr,generators)


def load_I_J():
	tmpI = 0
	tmpJ = 0
	for i in range(q):
		for j in range(q):
			if (j**2 + i**2 + 1)%q == 0:
				tmpI = i
				tmpJ = j
				break
		if tmpJ!=0 and tmpI!=0:
			break
	return tmpI, tmpJ
def load_mod_3():
        matrices = []
        a = 1
        sol_range = range(-p+1,p)
        for xa in range(a,p):
                if xa%2 is not 0:
                        continue
                for xb in sol_range:
                        for xc in sol_range:
                                for xd in sol_range:
                                        if (xa**2 + xb**2 + xc**2 + xd**2) == p:
                                                matrices.append((xa,xb,xc,xd))

        xa = 0
        b = 1
        for xb in range(b,p):
                for xc in sol_range:
                        for xd in sol_range:
                                if (xa**2 + xb**2 + xc**2 + xd**2) == p:
                                        matrices.append((xa,xb,xc,xd))
        return matrices

def load_mod_1():
	matrices = []
	sol_range = range(-p+1,p)
	for xa in range(1,p):
		if xa%2 == 0:
			continue
		for xb in sol_range:
				for xc in sol_range:
					for xd in sol_range:
						if (xa**2 + xb**2 + xc**2 + xd**2) == p:
							matrices.append((xa,xb,xc,xd))
	return matrices

def load_generators():
	if p%4==3:
		return load_mod_3()
	if p%4==1:
		return load_mod_1()	

tmpGens = load_generators()
I, J = load_I_J()
print("I " + str(I))
print("J " + str(J))

generators = []

for matrix in tmpGens:
	a = matrix[0] 
	b = matrix[1] 
	c = matrix[2]
	d = matrix[3]
	generator = (a + b*I + d*J,-b*J + c + d*I,-b*J - c + d*I, a - b*I - d*J)
	conjugate = (a - b*I - d*J,b*J - c - d*I,b*J + c - d*I, a + b*I + d*J)
	print(generator)
	matrixA = np.matrix([[generator[0], generator[1]],[generator[2], generator[3]]])
	generators.append(matrixA)
	matrixB = np.matrix([[conjugate[0], conjugate[1]],[conjugate[2], conjugate[3]]])
	generators.append(matrixB)

print("generators " + str(generators))
#LPS.append(tuple(np.array(Identity).flatten()))
create_graph(Identity,None,generators)
for m in LPS:
	print(m)
print(len(LPS))

indices = {}
index = 0
for u in LPS:
	indices[u] = index
	index += 1
print(edges[(1,0,0,1)])
with open("topologies/LPS_n" + str(len(LPS)) + "_d" + str(p+1) + ".topology", "w") as out:
	for v in LPS:
		for u in edges[v]:
			out.write(str(indices[v]) + " " + str(indices[u]) + "\n")
out.close()
