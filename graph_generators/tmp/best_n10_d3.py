from numpy import linalg as LA
import sys
import numpy as np

def addEdge(u,v):
	if u is v:
		return False
	if len(edges.get(u,[]))>=d or len(edges.get(v,[]))>=d:
		return False
	if u in edges.get(v,[]) or v in edges.get(u,[]):
		return False
	edges[u] = edges.get(u,[])
	edges[u].append(v)
	edges[v] = edges.get(v,[])
	edges[v].append(u)
	return True

def remove_edge(u,v):
	if v in edges.get(u,[]):
		edges.get(u,[]).remove(v)
	if u in edges.get(v,[]):
	        edges.get(v,[]).remove(u)

def to_mat():
	A = np.zeros((numNodes,numNodes))
	for v in edges:
		for u in edges[v]:
			A[v][u] = 1
	return A

maxA = []
maxEig = 1000
n = 0
numNodes = int(sys.argv[1])
d = int(sys.argv[2])
def calcAllGraphs(node):
	#print(node)
        global maxA, maxEig,edges,n,numNodes
        if node is numNodes:
		A = to_mat()
		eig,vecs = LA.eig(A)
                isBipartite = -d in[int(e) for e in eig]
                print(eig)
		eig = np.abs(eig)
		eig.sort()
                #print(A)
		#if abs(eig[-2]) < maxEig:
                eig2 = abs(eig[-2])
                if isBipartite:
                    eig2 = eig[-3]
                if 2*np.sqrt(d-1) - 0.5 > abs(eig2):
			maxEig = abs(eig2)
			maxA = A
                        print(eig)
                        #print(A)
                        return True
		n += 1
		if n%1000 is 0:
			print(n)
                        print(eig2)
                        print(eig)
                return False
		#print(A)
		#print(edges)
		#print(node)
	startIndex = 0
	if len(edges.get(node,[])) > 0:
		startIndex = edges[node][-1]
	ramanjuan = False
        if len(edges.get(node,[])) is d: 
                ramanjuan = calcAllGraphs(node + 1)
                return ramanjuan

            
	for i in [x for x in range(startIndex,numNodes) if x not in edges.get(node,[])]:
		if i is node:
			continue
		added = addEdge(node,i)
                if not added:
                    continue
                #print(node)

		#print(i)
		#print(edges)
		increment = 0
		if len(edges.get(node,[])) is d:
			increment = 1
		#print(node)
                if(not ramanjuan):
                        ramanjuan = calcAllGraphs(node + increment)
		if added:
			remove_edge(node,i)
        return ramanjuan
edges = {}
calcAllGraphs(0)
print(maxA)
print(maxEig)
with open("tmp.topology", 'w') as f:
    for i in range(len(maxA)):
        for j in range(len(maxA[i])):
            if(maxA[i][j]==1):
                f.write(str(i) + " " + str(j) + "\n")
    f.close()
