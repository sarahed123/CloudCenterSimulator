import sys
from create_graph import createGraphFromFile

def longestDistance(vertex,visited,D):
	visited.append(vertex)
	for u in edges[vertex]:
		if u not in visited:
			longestDistance(u,visited,D+1)

graph_path = sys.argv[1]
vertices, edges = createGraphFromFile(graph_path)
distances = {}
Diam = 0
for v in vertices:
	visited = []
	queue = []
	queue.append(v)
	distances[v] = 0
	visited.append(v)
	while len(queue):
		u = queue.pop(0)
		for w in edges[u]:
			if w not in visited:
				visited.append(w)
				queue.append(w)
				distances[w] = distances[u] + 1
	#print str(v) + " " + str(distances.values())
	D = max(distances.values())
	if D > Diam:
		Diam = D

print(Diam)		
