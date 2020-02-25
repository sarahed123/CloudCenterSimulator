import sys
sys.path.insert(0, './Galois/')
from galois import *
import random

N = int(sys.argv[1])

vertices = []

field = GF(N)
for i in field:
	for j in field:
		vertices.append((i,j,field[1]))

for j in field:
	vertices.append((j,field[1],field[0]))

vertices.append(tuple((field[1],field[0],field[0])))


edges = {}
for ind,v in enumerate(vertices):
	print v
	edges[ind] = []
	

for ind,v in enumerate(vertices):
	for ind2,u in enumerate(vertices):
		if u==v:
			continue
		if u[0]*v[0] + u[1]*v[1] + u[2]*v[2] == field[0]:
			print str(v) + "->" +  str(u)
			edges[ind].append(ind2)

while True:
	#now taking care of non-regular vertices
	nonRegular = [v for v in edges if len(edges[v]) < N+1]
	print nonRegular
	if len(nonRegular) <= 1:
		break
	v = nonRegular[0]
	u=v
	while u==v:
		u = random.choice(nonRegular)
	print str(u) + " to " + str(v)
	edges[u].append(v)
	edges[v].append(u)

print len(vertices)
for i in edges:
	print len(edges[i])

with open("topologies/polarity_n" + str(len(vertices)) + "_d" + str(N+1) + ".topolgy", "w") as f:
	for i in edges:
		for j in edges[i]:
			f.write(str(i) + " " + str(j) + "\n")
