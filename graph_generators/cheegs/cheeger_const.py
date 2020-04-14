import sys
from numpy import linalg as LA
sys.path.insert(1, '../')
import argparse
from to_mat import *
from create_graph import createGraphFromFile 
from pprint import pprint

def calc_cheeg_val(curr):
	outEdges = []
	totalPaths = 0
	for v in curr:
		for e in edges[v]:
			outEdges.append(e)
			if not e in curr:
				totalPaths+=1
	for v in outEdges:
		for e in edges[v]:
			if not e in curr:
				totalPaths+=1
	
	return totalPaths

def calc_cheeg(curr, cheeg, N, i=0, attempts = 0):
	if len(curr) > N/2:
		if attempts%100000 == 0:
			print(attempts)
		return cheeg, attempts
	attempts+=1
	outEdges = 0
	for v in curr:
		for e in edges[v]:
			if not e in curr:
				outEdges+=1
	if len(curr) > 0 and tuple(curr) not in tested:
		curr_cheeg = outEdges/len(curr)
		num = cheeg.get(curr_cheeg,0)
		num+=1
		cheeg[curr_cheeg] = num
		tested.add(tuple(curr))
	if i == N:
		 return cheeg, attempts
	v = vertices[i]
	curr.append(v)
	cheeg, attempts = calc_cheeg(curr, cheeg, N, i+1, attempts)
	curr.pop()
	cheeg, attempts = calc_cheeg(curr, cheeg, N, i+1, attempts)
	return cheeg, attempts

def update_cheeg(curr, curr_cheeg):
	global cheeg, cheeg_sets, set_size
	if set_size != len(curr) and not set_size==-1:
		return
	cheeg[curr_cheeg] = cheeg.get(curr_cheeg,0) + 1
	cheeg_sets[curr_cheeg] = cheeg_sets.get(curr_cheeg,0) + len(curr)

def calc_cheeg2(curr, cheeg, v = 0):
	global attempts
	attempts+=1
	#assert tuple(curr) not in tested
	#tested.add(tuple(curr))
	assert len(curr) <= N/2
	#if attempts%100000 == 0:
	#	print(attempts)
	if len(curr) > 0:
		curr_cheeg = calc_cheeg_val(curr)
		update_cheeg(curr, curr_cheeg)
	if len(curr)==int(N/2) or v==N:
		return
	if set_size <= len(curr) and not set_size==-1:
		return
	
	for u in range(v, N):
		calc_cheeg2(curr + [u], cheeg, u+1)
		

parser = argparse.ArgumentParser()
parser.add_argument("--graph", help="graph file path", dest="graph")	
parser.add_argument("--N", help="veritces num", dest="N", type=int)
parser.add_argument("--set_size", help="limit to set size", dest="set_size", default=-1, type=int)
args = parser.parse_args()

vertices, edges = createGraphFromFile(args.graph)
vertices = list(vertices)
N = args.N
set_size = args.set_size
attempts = 0
tested = set()
cheeg = dict()
cheeg_sets = dict()
calc_cheeg2([], cheeg)
#print(f"attempts {attempts}")
for c in sorted(cheeg):
	print(f"{c}: {cheeg[c]}")
