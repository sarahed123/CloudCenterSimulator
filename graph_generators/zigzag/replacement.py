import sys

from create_graph import *

def replacement(small_graph_file, big_graph_file):
	vertices_s, edges_s = createGraphFromFile(small_graph_file)
	vertices_b, edges_b  = createGraphFromFile(big_graph_file)

	#print(edges_s)
	#print(edges_b)

	edges = {}
	vertices = range(len(vertices_s) * len(vertices_b))
	for v in vertices:
		offset = int(v/len(vertices_s)) * len(vertices_s)
		edges[v] = edges.get(v,set())
		for i in edges_s[v % len(vertices_s)]:
			edges[v].add(offset+i)
	for j in vertices_b:
		edges_from_source = list(edges_b[j])
		for dest_index in range(len(edges_from_source)):
			edges_from_dest = list(edges_b[edges_from_source[dest_index]])
			found = False
			for k in range(len(edges_from_dest)):
				if edges_from_dest[k]==j:
					edges[j*len(vertices_s) + dest_index].add(len(vertices_s)*edges_from_source[dest_index] + k)
					found = True
			if not found:
				raise Exception()
	
	return edges
