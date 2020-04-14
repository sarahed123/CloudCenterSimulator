import sys
from numpy import linalg as LA
sys.path.insert(1, '../')
import argparse
from to_mat import *
from create_graph import createGraphFromFile
from pprint import pprint

parser = argparse.ArgumentParser()
parser.add_argument("--graph", dest="graph", help="the graph file")
parser.add_argument("--max_len", type=int, dest="max_len", help="the max len of a path")

def find_shortest_paths(s,t):
	final_paths = []
	paths = []
	curr = [s]
	paths.append(curr)
	while len(curr) <= args.max_len:
		curr = paths.pop(0)
		v = curr[-1]
		if v==t:
			final_paths.append(curr)
			continue
		for e in edges[v]:
			cp = list(curr)
			cp.append(e)
			paths.append(cp)
	return final_paths

args = parser.parse_args()
vertices, edges = createGraphFromFile(args.graph)
total = 0
count = 0
for v in vertices:
	for u in vertices:
		if u==v:
			continue
		shortest_paths = find_shortest_paths(v,u)
		if len(shortest_paths) == 0:
			continue
		min_len = len(shortest_paths[0])
		shortest_paths = (list(filter(lambda l: len(l) == min_len, shortest_paths)))
		total+=len(shortest_paths)
		count+=1

print(total)
print(total/count)


