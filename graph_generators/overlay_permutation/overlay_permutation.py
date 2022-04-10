import argparse
import copy
import random
import re

class OverLappingEdges(Exception):
    pass

parser = argparse.ArgumentParser()

parser.add_argument("--graph", help="graph path", dest="graph", required=True)
parser.add_argument("--N", help="num nodes", dest="N", type=int, required=True)
parser.add_argument("--P", help="permutation num", dest="P", type=int, required=True)
parser.add_argument("--output", help="output graph name", dest="output_graph", required=True)


args = parser.parse_args()
output_graph = args.output_graph
P = args.P
graph = args.graph
N = args.N

edges = {}
for v in range(N):
    edges[v] = []

with open(graph) as f:
    for line in f.readlines():
        _rex = re.compile("\d+ \d+\n")
        if _rex.fullmatch(line):
            u, v = line.strip().split(" ")
            edges[int(v)].append(int(u))

original_edges = copy.deepcopy(edges)
for i in range(P):
    edges_overlpas = True
    while edges_overlpas:
        try:
            nodes = [v for v in range(N)]
            permuted_nodes = random.sample(nodes, len(nodes))
            permuted_edges = {}
            for i in range(N):
                permuted_edges[permuted_nodes[i]] = [permuted_nodes[u] for u in original_edges[i]]
            for v in edges:
                for u in permuted_edges[v]:
                    edges[v].append(u)
        except OverLappingEdges:
            pass # not doing anything at this point
        edges_overlpas = False

written = {}
for v in edges:
    written[v] = []

with open(f"permuted/{output_graph}", "w") as f:
    for v in edges:
        for u in edges[v]:
            if u not in written[v]:
                f.write(f"{v} {u} {edges[v].count(u)}\n")
                written[v].append(u)



