import sys
sys.path.insert(0, '/cs/usr/inonkp/netbench/graph_generators/')
from create_graph import createGraphFromFile
from shortest_paths import get_paths

graph_path = sys.argv[1]
num_paths = int(sys.argv[2])

vertices, edges = createGraphFromFile(graph_path)
paths_db = get_paths(vertices,edges,num_paths)
for paths in paths_db:
	for path in paths:
		for v in path:
			print(v, end=" ")
		print()

