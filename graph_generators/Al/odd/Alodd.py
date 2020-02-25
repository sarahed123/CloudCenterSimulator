import sys

#usage Alodd.py N (odd)

def rotate(node,last,direction):
	order = range(0,last)[::direction]
	firstVal = node[order[0]]
	for i in order:
		node[i] = node[(i+direction) % last]
	node[order[-1]] = firstVal
	return tuple(node)	

def test(node):
	print(rotate(list(I),3,1))
	print(rotate(list(I),3,-1))
	print(rotate(list(I),N,1))
	print(rotate(list(I),N,-1))

def alternate(nodes, node,parent=None):
	if parent is not None:
		edges[parent].append(node)
	if node in nodes:
		return
	nodes.append(node)
	edges[node] = []
	alternate(nodes,rotate(list(node),3,1),node)
	alternate(nodes,rotate(list(node),3,-1),node)
	alternate(nodes,rotate(list(node),N,1),node)
	alternate(nodes,rotate(list(node),N,-1),node)

N = int(sys.argv[1])

I = tuple(range(N))
nodes = []
edges = {}
alternate(nodes,I)
print("node num " + str(len(nodes)))
indices = {}
for counter, value in enumerate(nodes):
	indices[value] = counter
with open("Al_odd_N" + str(N) + ".topology","w") as f:
	for node1 in nodes:
		for node2 in edges[node1]:
			f.write(str(indices[node1]) + " " + str(indices[node2]) + "\n")
