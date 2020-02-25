import sys
sys.path.insert(0, '/cs/usr/inonkp/netbench/graph_generators/')
from create_graph import createGraphFromFile
from shortest_paths import get_paths
from random import shuffle

def assign_circuit(path,edges):
	for i in range(len(path) - 1):
		if path[i+1] in edges[path[i]]:
			continue
		else:
			return False
	for i in range(len(path) - 1):
		edges[path[i]].remove(path[i+1])
	print(f"assigned path {path}") 
	return True

if __name__ == "__main__":

	graph_path = sys.argv[1]
	num_paths = int(sys.argv[2])

	vertices, edges = createGraphFromFile(graph_path)
	paths_db = get_paths(vertices,edges,num_paths)
	print(paths_db)
	sources = dict()
	for paths in paths_db:
			if len(paths[0]) >= 5:
				sources[(paths[0][0], paths[0][-1])] = paths
	
	dropped_paths = dict()
	keys = list(sources.keys())
	shuffle(keys)
	i = 0
	for source in keys:
		path = sources[source].pop(0)
		while not assign_circuit(path,edges):
			if not source in dropped_paths:
				dropped_paths[source] = []
			dropped_paths[source].append(path)
			if len(sources[source]) > 0:
				path = sources[source].pop(0)
			else:
				print("failed on for pair " + str(source))
				print(dropped_paths[source])
				break
		i+=1
		if i > 10:
			break
