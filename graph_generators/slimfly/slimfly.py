import sys
sys.path.insert(0, './Galois/')
from galois import *

def findPrimitiveElemnt(field):
	for elem in field:
		curr = elem
		visited = [curr]
		while len(visited) < q - 1:
			curr = curr * elem
			if curr in visited:
				break
			visited.append(curr)
		if len(visited)==q - 1:
			return elem

def createX(elem,start,stop):
	curr = start
	X = []
	X.append(elementToPower(elem,curr))
	while True:	
		curr= (curr + 2)%q
		X.append(elementToPower(elem,curr))
		if curr == stop:
			break
	return X

def elementToPower(elem,power):
	if power == 0:
		return field[1]
	curr = elem
	while power > 1:
		curr = curr*elem
		power-=1
	return curr

q = int(sys.argv[1])

r = q%4

if r == 0:
	field= GF(q)
else:
	field = Zmod(q)
prim = findPrimitiveElemnt(field)
primSquared = prim*prim
if r==1:
	X = createX(prim,0,q-3)
	Xt = createX(prim,1,q-2)
elif r==3:
	X = createX(prim,0,q-2)
	Xt = createX(prim,1,q-1)
elif r==0:
	X = createX(prim,0,q-2)
	Xt = createX(prim,1,q-1)
print(X)
print(Xt)
edges = {}

print "calc local edges"
for i in range(q):
	x = 0
	print "first 0"
	for y in range(q):
		for yt in range(q):
			v = (x,i,y)
                        u = (x,i,yt)
                        if not v in edges:
                                edges[v] = set()
                        if not u in edges:
                                edges[u] = set()
                        if  (field[y] - field[yt]) in X:
				print str(v) + " " + str(u)
                                edges[v].add(u)

	x = 1
	print "then 1"
        for y in range(q):
                for yt in range(q):
                        v = (x,i,y)
			u = (x,i,yt)
                        if  (field[y] - field[yt]) in Xt:
				if not v in edges:
                                	edges[v] = set()
                        	if not u in edges:
                                	edges[u] = set()
       	                        edges[v].add(u)
				print str(v) + " " + str(u)

print "now remote edges"
for x in [0]:
	for y in range(q):
                for yt in range(q):
			v = (x,y,yt)
			xt = 1
			for c in range(q):
                		for ct in range(q):
					u = (xt,c,ct)
					if field[yt] == (field[c]*field[y] + field[ct]):
						if not v in edges:
                                        		edges[v] = set()
                                		if not u in edges:
                                        		edges[u] = set()
                                		edges[v].add(u)
						edges[u].add(v)
						print str(v) + " " + str(u)

indices = {}
print len(edges)
index = 0
for k in edges:
	print len(edges[k])
	indices[k] = index
	#print str(index) + " " + str(k) + " " + str(edges[k])
	index+=1

with open("topologies/slimfly_n" + str(len(edges)) + "_d" + str(len(edges[(0,0,0)])) + ".topology", "w") as f:
	for k in edges:
		for v in edges[k]:
			f.write(str(indices[k]) + " " + str(indices[v]) + "\n")

