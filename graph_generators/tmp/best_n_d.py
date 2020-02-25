from numpy import linalg as LA
import sys
import numpy as np

def add_edge(u,v):
	if u is v:
		return False
	if len(edges.get(u,[]))>=d:
		return False
	if v in edges.get(u,[]):
		return False
	edges[u] = edges.get(u,[])
	edges[u].append(v)
	#edges[v] = edges.get(v,[])
	#edges[v].append(u)
	return True

def remove_edge(u,v):
	if v in edges.get(u,[]):
		edges.get(u,[]).remove(v)
	#if u in edges.get(v,[]):
	#        edges.get(v,[]).remove(u)

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
def calcAllGraphs(node,parent, index):
	#print(node)
        global maxA, maxEig,edges,n,numNodes
        #print("entered " + str(node))
        #print("parent " + str(parent))
        #print(edges)
        print(index)
        #add_edge(node,parent)
        #index+=1
        if index is numNodes*d:
		A = to_mat()
		eig,vecs = LA.eig(A)
                isBipartite = -d in[int(e) for e in eig]
                #print(eig)
		eig = np.abs(eig)
		eig.sort()
                print(A)
		#if abs(eig[-2]) < maxEig:
                eig2 = abs(eig[-2])
                if isBipartite:
                    eig2 = eig[-3]
                if 2*np.sqrt(d-1) - 0.5 > abs(eig2):
			maxEig = abs(eig2)
			maxA = A
                        #print(eig)
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
        ramanjuan = False
        existingEdges = list(edges.get(node,[]))
        possiblities = []
	for i in [j for j in range(numNodes) if j not in existingEdges]:
            added_1 = add_edge(node,i)
            #added_2 = add_edge(i,node)
            if added_1:
                increment = 0
                if node in edges.get(i,[]):
                    increment = 2
                if not ramanjuan:
                    ramanjuan = calcAllGraphs(i,node,index+increment)
                    if ramanjuan:
                        return True
             
                #return calcAllGraphs(parent,node,index)
            if added_1:
                remove_edge(node,i)
            #if added_2:
                #remove_edge(i,node)
    
        return False 
      
  
	
edges = {}
add_edge(1,0)
add_edge(0,1)
add_edge(1,2)
add_edge(2,1)
calcAllGraphs(0,1,4)
print(maxA)
print(maxEig)
with open("tmp.topology", 'w') as f:
    for i in range(len(maxA)):
        for j in range(len(maxA[i])):
            if(maxA[i][j]==1):
                f.write(str(i) + " " + str(j) + "\n")
    f.close()
