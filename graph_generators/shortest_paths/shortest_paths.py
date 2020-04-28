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
parser.add_argument("--min_len", type=int, dest="min_len", help="the min len of a path", default=1)
parser.add_argument("--max_path_num", type=int, dest="max_path_num", help="the max number of a path")

def find_shortest_paths(s,t):
	final_paths = []
	paths = []
	curr = [s]
	paths.append(curr)
	while len(curr) <= args.max_len:
		curr = paths.pop(0)
		v = curr[-1]
		if v==t:
			if len(curr) >= args.min_len and len(curr) <= args.max_len:
				final_paths.append(curr)
			else:
				return final_paths
			if len(final_paths) >= args.max_path_num:
				return final_paths
			continue
		for e in edges[v]:
			if e in curr:
				continue
			cp = list(curr)
			cp.append(e)
			paths.append(cp)
	return final_paths

def overlapping_edges(paths):
	total=0
	for i in range(len(paths)):
		for j in range(i+1, len(paths)):
			add=1
			edges = set()
			p = paths[i]
			for k in range(len(p)-1):
				edges.add((p[k],p[k+1]))
			p = paths[j]
			for k in range(len(p)-1):
				if (p[k],p[k+1]) in edges:
					total+=add
					add+=1
	return total

shortest_paths = []
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
		
		total+=overlapping_edges(shortest_paths)
		count+=1
		#min_len = len(shortest_paths[0])
		#shortest_paths = (list(filter(lambda l: len(l) == min_len, shortest_paths)))
		#shortest_five_total = sum([len(sh) for sh in shortest_paths])
		#total = total + len(shortest_paths)

print(total)
print(count)


