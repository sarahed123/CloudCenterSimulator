from replacement import *
import sys

def getCloud(k):
	offset = int(k/d) * d
        cloud = range(offset, (offset+d))
	return cloud

print("small graph first!")
small_graph = sys.argv[1]
big_graph = sys.argv[2]

replacement = replacement(small_graph, big_graph)
d = len(replacement[0])
print(replacement)
edges = {}
neighbours = {}
for v in replacement:
	edges[v] = set()
	cloud = getCloud(v)
	#print(v)
	#print(cloud)
	for u in cloud:
		if u==v:
			continue
		far_neighbour = [x for x in replacement[u] if x not in cloud][0]
		#print(u)
		#print(far_neighbour)
		farCloud = getCloud(far_neighbour)

		for c in farCloud:
			if c==far_neighbour:
				continue
			edges[v].add(c)
	#print(local_neighbours)
	#print(far_neighbour)

print(edges)

with open("zigzag_n" + str(len(edges)) + "_d" + str(pow(d-1,2)) + ".topology", "w") as out:
	for v in edges:
		for u in edges[v]:
			out.write(str(v) + " " + str(u) + "\n")
out.close()

#with open("zigzag_n" + str(len(edges)) + "_d" + str(d) + ".mat.topology", "w") as out:

