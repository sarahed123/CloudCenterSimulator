import sys
import random

N = int(sys.argv[1])
d = int(sys.argv[2])

edges = {}
for i in range(N):
	edges[i] = set()

def availableSwitch(edges):
	for i in edges:
		if len(edges[i]) <= d - 2:
			return i	

	return False

while True:
	possibleSwitches = [i for i in range(N) if (len(edges[i])<d)]
	if len(possibleSwitches) <= 1:
		break
	s1 = random.randint(0,len(possibleSwitches)-1)
	s2 = s1
	while s1==s2:
		s2 = random.randint(0,len(possibleSwitches)-1)
	edges[possibleSwitches[s1]].add(possibleSwitches[s2])
	edges[possibleSwitches[s2]].add(possibleSwitches[s1])

leftover = availableSwitch(edges)
print "first stage"
for i in edges:
        print str(i) + " " + str(len(edges[i]))

while leftover:
	x = leftover
	y = leftover
	while x==leftover or x in edges[leftover]:
		x = random.randint(0,N-1)
	while (y==leftover or y==x) or y in edges[leftover]:
		y = random.randint(0,N-1)
	#sample an edge from y
	#sample an edge from x
	#remove them and attach them to leftover
	edges[x].remove(y)
	edges[y].remove(x)
	edges[leftover].add(x)
	edges[x].add(leftover)
	edges[leftover].add(y)
	edges[y].add(leftover)
	leftover = availableSwitch(edges)

print "second stage"
for i in edges:
	print str(i) + " " + str(len(edges[i]))

indices = {}
index = 0
for k in edges:
	indices[k] = index
	#print str(index) + " " + str(k) + " " + str(edges[k])
	index+=1

with open("topologies/jellyfish_n" + str(len(edges)) + "_d" + str(d) + ".topology", "w") as f:
	for k in edges:
		for v in edges[k]:
			f.write(str(indices[k]) + " " + str(indices[v]) + "\n")
