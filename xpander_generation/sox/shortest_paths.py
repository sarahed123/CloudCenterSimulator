import argparse
import random
import sys
import os
sys.path.insert(1, os.path.dirname(__file__) + '\..\..\graph_generators')
from create_graph import createGraphFromFile


def get_shortest_paths(source, target, edges, max_num_paths):
    q = [[source]]
    paths = []
    shortestPath = 999999
    while q:
        p = q.pop(0)
        if len(p) > shortestPath:
            return paths
        
        lastNode = p[-1]
        if target==lastNode:
            shortestPath = len(p)
            paths.append(p)
            assert p[0] == source
            if len(paths) == max_num_paths:
                return paths

        if len(p) == shortestPath:
            continue

        nextNodes = edges[lastNode]
        for n in nextNodes:
            if n in p:
                continue
            nextPath = p[:]
            nextPath.append(n)
            q.append(nextPath)
    return paths

def write_paths(source, paths, out_dir, edges):        
    with open(out_dir + "/" + str(source), "w") as f:
        for t in paths:
            for p in paths[t]:
                for i in range(len(p) - 1):
                    assert p[i+1] in edges[p[i]]
                f.write(", ".join(map(str,p)) + "\n")   

parser = argparse.ArgumentParser()
parser.add_argument("--N", help="Num nodes", dest="N", type=int, required=True)
parser.add_argument("--graph", help="The graph", dest="graph", required=True)
parser.add_argument("--out-dir", help="Output dir", dest="out_dir", required=True)
parser.add_argument("--max-paths", help="Take this many number of paths max", dest="max_num_paths", default=10000, type=int)
args = parser.parse_args()
vertices, edges = createGraphFromFile(args.graph)
os.makedirs(args.out_dir, exist_ok=True)

N = args.N
for i in range(N):
    paths = {}
    for j in range(N):
        if i==j:
            continue
        paths[j] = get_shortest_paths(i,j,edges,args.max_num_paths)
    write_paths(i, paths, args.out_dir, edges)
    print("done with " + str(i))