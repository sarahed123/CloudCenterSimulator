import sys
import numpy as np
sys.path.insert(1, '/cs/usr/inonkp/netbench/graph_generators')
import argparse
from to_mat import *
from create_graph import createGraphFromFile
from pprint import pprint
import os
import math


def find_shortest_paths(s,t,edges):
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
					#add+=1
	return total

def update_edges_weights_equal(res, shortest_paths):


	for p in shortest_paths:
		for i in range(len(p)-1):
			j = i + 1
			pair = (p[i],p[j])
			val = res.get(pair, 0)
			res[pair]  = val + (math.log(len(p)))/(len(shortest_paths)*len(p))

def update_edges_weights(res, shortest_paths):


        for p in shortest_paths:
                pair = (p[0],p[-1])
                #res[pair]  = (2**(len(p)-1)/sum([1/2**v for v in range(0,len(shortest_paths))]))
                res[pair] = len(p)/(len(shortest_paths))
                break

def compute(graphs, args):
	max_len = args.max_len
	ret = {}
	for graph in graphs:
		res2 = {}
		for min_len in range(2,max_len+1):
			args.max_len = min_len
			args.min_len = min_len
			all_shortest_paths = []
			edges_count = 0
			vertices, edges = createGraphFromFile(graph)
			vertices = list(vertices)
			shortest_paths = []
			totalNumPath = 0
			totalShortesLen = 0
			count = 0
			pairNum = 0
			res= {}
			for i in range(len(vertices)):
				v = vertices[i]
				if v<args.min_v or v>args.max_v:
					continue
				for j in range(len(vertices)):
					if i==j:
						continue
					u = vertices[j]
					if u<args.min_v or u>args.max_v:
						continue
					if u==v:
						continue
					pairNum+=1
					shortest_paths = find_shortest_paths(v,u,edges)
					if len(shortest_paths) == 0:
						continue
					count+=1.0
					min_len = len(shortest_paths[0])
					shortest_paths = list(filter(lambda p: len(p)==min_len, shortest_paths))
					edges_count+=min_len*len(shortest_paths)
					all_shortest_paths+=(shortest_paths)
					totalNumPath+=len(shortest_paths)/min_len
					totalShortesLen+=len(shortest_paths[0])
					val = res.get(len(shortest_paths),0)
					res[len(shortest_paths)] = val + 1
					update_edges_weights_equal(res2, shortest_paths)
			print(os.path.basename(graph))
			print(min_len)
			pprint(res)
		print(os.path.basename(graph))
		ret[os.path.basename(graph)] = sum([2**v for v in res2.values()])/100
		print(ret[os.path.basename(graph)])
	return ret

def get_parser():
	parser = argparse.ArgumentParser()
	parser.add_argument("--graphs", dest="graphs", help="the graph(s) file(s)", nargs="+")
	parser.add_argument("--max_len", type=int, dest="max_len", help="the max len of a path", default=8)
	parser.add_argument("--min_len", type=int, dest="min_len", help="the min len of a path", default=1)
	parser.add_argument("--min_v", type=int, dest="min_v", help="the min node range", default=0)
	parser.add_argument("--max_v", type=int, dest="max_v", help="the max node range", default=999999)
	parser.add_argument("--max_path_num", type=int, dest="max_path_num", help="the max number of a path", default=99999)
	return parser

if __name__ =="__main__":
	args = get_parser().parse_args()
	compute(args.graphs, args)
