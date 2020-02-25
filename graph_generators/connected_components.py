import sys
from create_graph import *

def get_component(v,visited):
	if v in visited:
		return
	visited.append(v)
	print(v)
	print(edges[v])
	for u in edges[v]:
		get_component(u,visited)
	return visited

graph_file = sys.argv[1]

vertices, edges = createGraphFromFile(graph_file)
components = []
visited = []
for v in vertices:
	if v in visited:
		continue
	components.append(get_component(v,visited))
	print(visited)

