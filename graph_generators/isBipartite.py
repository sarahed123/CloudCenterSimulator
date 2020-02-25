import sys
from create_graph import *

def dfs(v,color):
	if v in colors:
		if colors[v] is not color:
			return False
		return True
	colors[v] = color
	for u in edges[v]:
		if not dfs(u,1 - color):
			return False
	return True

graph_file = sys.argv[1]

vertices, edges = createGraphFromFile(graph_file)
colors = {}

print(dfs(0,0))
#print([x for x in colors if colors[x] is 0])
#print([x for x in colors if colors[x] is 1])
#print(edges)
#print(colors)
