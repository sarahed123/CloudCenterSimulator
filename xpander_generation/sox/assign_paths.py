from path_db import PathDB
import argparse
import random
import sys
import os
sys.path.insert(1, os.path.dirname(__file__) + '/../../graph_generators')
from create_graph import createGraphFromFile
import numpy.random as nprand

def permutation_traffic(N):
    nodes = [i for i in range(N)]
    perm = []
    while len(nodes) > 1:
        source = random.choice(nodes)
        nodes.remove(source)
        target = random.choice(nodes)
        nodes.remove(target)
        perm.append((source, target))
    return perm


def all_to_all(N):
    nodes = [i for i in range(N)]
    a2a = []
    for v in nodes:
        for u in nodes:
            if u==v:
                continue
            a2a.append((v,u))
    return a2a


def skew(N,  traffic_pairs = None, nodes_skew = 0.04, traffic_skew = 0.77):
    nodes = [i for i in range(N)]
    random.shuffle(nodes)
    nodesA = nodes[:int(nodes_skew*N)] if not traffic_pairs else traffic_pairs
    print(nodesA)
    nodesBCount = N - len(nodesA)
    probA = traffic_skew/len(nodesA)
    probB = (1 - traffic_skew)/(nodesBCount)

    pairs = []
    probabilities = []
    wastedProb = len(nodesA) * probA * probA
    wastedProb += nodesBCount * probB * probB
    for v in nodes:
        for u in nodes:
            if u == v:
                continue
            probV = probA if v in nodesA else probB
            probU = probA if u in nodesA else probB
            
            pairs.append((v,u))
            probabilities.append(probU*probV/(1-wastedProb)) 
    return pairs, probabilities

def get_traffic_matrix(N, traffic_type, random_traffic_pairs, traffic_pairs = None):
    pairs = []
    if traffic_type=="perm":
        pairs = permutation_traffic(N)
    elif traffic_type=="a2a":
        pairs = all_to_all(N)
    elif traffic_type=="skew":
        pairs, probablities = skew(N, traffic_pairs)
        return randomize_traffic(pairs, random_traffic_pairs, probablities)
    else:
        raise ValueError(traffic_type)
    return randomize_traffic(pairs, random_traffic_pairs)

def get_edges(path):
    edges = []
    for i in range(len(path) - 1):
        edge = (path[i], path[i+1])
        edges.append(edge)
    return edges

def mark_edge_used(edges, used_edges):
    for edge in edges:
        used_edges[edge] = True


def path_valid(path, used_edges):
    for edge in get_edges(path):
        if edge in used_edges:
            return False
    return True

def randomize_traffic(pairs, random_count, probablities = 0):
    if probablities:
        pairs_indices = nprand.choice([i for i in range(len(pairs))], p=probablities, size=random_count)
    else:
        pairs_indices = nprand.choice([i for i in range(len(pairs))], size=random_count)
    return [pairs[i] for i in pairs_indices]

def main():
    parser = argparse.ArgumentParser()

    parser.add_argument("--N", help="Num nodes", dest="N", type=int, required=True)
    parser.add_argument("--graph", help="The graph", dest="graph", required=True)

    parser.add_argument("--traffic", dest="traffic", help="The traffic matrix", default="perm")
    parser.add_argument("--path-db", required=True, help="The paths DB", dest="path_db")
    parser.add_argument("--seed", help="seed", required=True, dest="seed", type=int)
    parser.add_argument("--traffic-random-count", help="how many pairs to take from the traffic pairs", dest="random_traffic_pairs", type=int)
    parser.add_argument("--traffic-file", help="traffic file", dest="traffic_file")

    args = parser.parse_args()
    random.seed(args.seed)
    nprand.seed(args.seed)

    file_traffic = None
    if args.traffic_file:
        with open(args.traffic_file) as f:
            traffic_lines = f.readlines()
            file_traffic = list(map(int,traffic_lines[0].split(" ")))
    
    vertices, edges = createGraphFromFile(args.graph)
    db = PathDB(args.path_db, args.N)
    traffic_pairs = get_traffic_matrix(args.N, args.traffic, args.random_traffic_pairs, file_traffic)
    used_edges = {}
    success_count = 0
    test_count = 0
    for pair in traffic_pairs:
        source = pair[0]
        target = pair[1]
        paths = db.get_random_paths(source, target, 10, nprand)
        random.shuffle(paths)
        for path in paths:

        #assert the path is in the graph
            for i in range(len(path) - 1):
                assert path[i+1] in edges
            
            if not path_valid(path, used_edges):
                continue
            # print(f"marking path {path}")
            
            mark_edge_used(get_edges(path), used_edges)
            success_count += 1
            break

    print(success_count)
    print(test_count)


if __name__=="__main__":
    main()
