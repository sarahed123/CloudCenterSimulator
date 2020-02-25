import sys
from create_graph import createGraphFromFile

def longestDistance(vertex,visited,D):
	visited.append(vertex)
	for u in edges[vertex]:
		if u not in visited:
			longestDistance(u,visited,D+1)

def get_shortest_paths(v,u,edges,max_num_paths):
	paths = []
	curr = [v]
	queue = [curr]
	while True:	
		curr = queue.pop(0)
		if curr[-1] == u:
			paths.append(curr)
			if len(paths) == max_num_paths:
				return paths
		for neigh in edges[curr[-1]]:
			if neigh not in curr:
				queue.append(curr + [neigh])

def get_paths(vertices, edges, max_num_paths):
	paths_db = []
	for v in vertices:
		for u in vertices:
			if u==v:
				continue
			paths_db.append(get_shortest_paths(v,u,edges,max_num_paths))
	return paths_db

if __name__ == "__main__":
	
	graph_path = sys.argv[1]
	num_paths = int(sys.argv[2])

	vertices, edges = createGraphFromFile(graph_path)
	print(vertices, edges)
	paths_db = get_paths(vertices,edges,num_paths)
	for paths in paths_db:
		for p in paths:
			if len(p) >= 4:
				print(p)

