import argparse
import random
import math
import sys
import os
sys.path.insert(1, '..\..\graph_generators')
from print_lambda import calc_eigs
from create_graph import createGraphFromFile

def get_nodes_from(node, edges, distance):
    visited = set()
    q1 = [node]

    while distance:
        q2 = []
        while q1:
            v = q1.pop()
            for u in edges[v]:
                visited.add(u)
                q2.append(u)
        q1 = q2
        distance-=1

    return visited

def no_circles(path):
    no_circle = []
    for n in path:
        if not n in no_circle:
            no_circle.append(n)
        else:
            while no_circle[-1] != n:
                no_circle.pop()
    return no_circle

def do_random_walk_from(node, edges, distance):
    path = [node]
    while distance:
        while node in path:
            node = random.choice(list(edges[node]))
        path.append(node)
        distance-=1
    return no_circles(path)

def write_walks_to_file(file_name, walks):
    with open(f"{file_name}", "w") as f:
        for walk in walks:
            f.write(", ".join(map(str, walk)) + "\n")


parser = argparse.ArgumentParser()

parser.add_argument("--D", help="Degree of graph", dest="D", type=int)
parser.add_argument("--graph", help="Path to the graph", dest="graph", required=True)
parser.add_argument("--N", help="Num nodes", dest="N", type=int, required=True)
parser.add_argument("--k", help="path len", dest="k", type=int, default=0)
parser.add_argument("--m", help="Num paths", dest="m", type=int, default=0)
parser.add_argument("--seed", help="seed", dest="seed", type=int, default=0)
parser.add_argument("--graph-name", help="Graph name", dest="graph_name", required=True)


args = parser.parse_args()

seed = args.seed if args.seed else random.randint()
random.seed(seed)

eig_without_self_loops = calc_eigs(args.graph, args.N, False)[-2]
eig_with_self_loops = calc_eigs(args.graph, args.N, True)[-2]
D = args.D
N = args.N

self_loops = True if eig_with_self_loops <= eig_without_self_loops else False
vertices, edges = createGraphFromFile(args.graph, self_loops)
eig = eig_with_self_loops if self_loops else eig_with_self_loops

eig /= D
k = math.log(1/(2*N), eig) if not args.k else args.k
m = 24*math.log(N)*N*N  if not args.m else args.m
print("m = ", m, " k = ", k)
folder = f"db/{args.graph_name}/main/k{k}/{seed}/"
os.makedirs(folder, exist_ok=True)
for i in range(N):
    walks = []
    print(f"seed {seed}. starting node {i}")
    for j in range(int(m)):
        walks.append(do_random_walk_from(i,edges,int(k)))
    write_walks_to_file(f"{folder}/{i}", walks)
    