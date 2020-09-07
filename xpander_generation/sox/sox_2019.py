""" 
This implements the 2019 sox path db calculation.
We calculate 10 path in the following way:
For each source target pair,
First do a random walk of 2 steps from the source
Then continue with the shortest path to the target.
"""
import argparse
import os
import random
import sys
sys.path.insert(1, '../../graph_generators')
from create_graph import createGraphFromFile

def get_shortest_path(source, target, edges):
    q = [[source]]
    while True:
        p = q.pop(0)
        lastNode = p[-1]
        nextNodes = edges[lastNode]
        if target in nextNodes:
            p.append(target)
            assert p[0] == source
            return p
        for n in nextNodes:
            if n in p:
                continue
            nextPath = p[:]
            nextPath.append(n)
            q.append(nextPath)

def calc_shortest_paths(N,edges):
    shortest_paths = {}
    for s in range(N):
        shortest_paths[s] = {}
        for t in range(N):
            if s == t:
                shortest_paths[s][t] = []
                continue
            shortest_paths[s][t] = get_shortest_path(s,t,edges)
    return shortest_paths 



def get_random_walk(node, edges, distance):
    path = [node]
    while distance:
        nextNode = random.choice(list(edges[path[-1]]))
        if nextNode in path:
            continue
        path.append(nextNode)
        distance-=1
    return path

def write_paths(N, out_dir, paths):
    for s in paths:
        with open(out_dir + "/" + str(s), "w") as f:
            for t in paths[s]:
                for p in paths[s][t]:
                    for i in range(len(p) - 1):
                        assert p[i+1] in edges[p[i]]
                    f.write(", ".join(map(str,p)) + "\n")

parser = argparse.ArgumentParser()
parser.add_argument("--N", help="Num nodes", dest="N", type=int, required=True)
parser.add_argument("--rw-distance", help="Random walk distance", dest="distance", type=int, default=2)
parser.add_argument("--num_paths", help="Num paths to randomize", type=int, default=10, dest="num_paths")
parser.add_argument("--seed", help="seed", dest="seed", type=int, required=True)
parser.add_argument("--graph", help="The graph", dest="graph", required=True)
parser.add_argument("--out-dir", help="Output dir", dest="out_dir", required=True)
args = parser.parse_args()
distance = args.distance
vertices, edges = createGraphFromFile(args.graph)
random.seed(args.seed)
paths = {}
N = args.N
shortest_paths = calc_shortest_paths(N, edges)
for s in range(N):
    paths[s] = {}
    print(s)
    for t in range(N):
        if s==t:
            continue
        paths[s][t] = []
        walks = [get_random_walk(s, edges, distance) for i in range(args.num_paths)]
        paths[s][t] = [walk[:-1] + shortest_paths[walk[-1]][t] for walk in walks]

os.makedirs(args.out_dir, exist_ok=True)
write_paths(N, args.out_dir, paths)
