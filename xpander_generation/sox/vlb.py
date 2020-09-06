from path_db import PathDB
import argparse
import os
import random

def get_intermediate(source, target, db, max_distance):
    ms = max_distance
    mt = max_distance
    nodes = []
    while True:
        nodes = db.get_intersection_by_distance(source,target,ms,mt)
        if nodes:
            break
        ms += 1
        nodes = db.get_intersection_by_distance(source,target,ms,mt)
        if nodes:
            break
        mt += 1


    return random.choice(nodes)

parser = argparse.ArgumentParser()
parser.add_argument("--N", help="Num nodes", dest="N", type=int, required=True)
parser.add_argument("--path-db", required=True, help="The paths DB", dest="path_db")
parser.add_argument("--num-paths", help="Num paths to randomize", type=int, default=1, dest="path_num")
parser.add_argument("--seed", help="seed", dest="seed", type=int, required=True)
parser.add_argument("--out-dir", help="Output dir", dest="out_dir", required=True)
parser.add_argument("--max-intermediate-dist", help="Should be used for shortest paths db only", dest="max_i_dist", default=99999, type=int)


args = parser.parse_args()
random.seed(args.seed)
output_path = args.out_dir
os.makedirs(output_path,exist_ok=True)
N = args.N
db = PathDB(args.path_db, args.N)
for i in range(N):
    paths = []
    for j in range(N):
        if i==j:
            continue
        for k in range(args.path_num):
            pathsA = []
            pathsB = []
            while not pathsA or not pathsB:
                intermediate = get_intermediate(i,j,db,args.max_i_dist)
                pathsA = db.get_paths(i, intermediate)
                pathsB = db.get_paths(intermediate, j)
            pathA = random.choice(pathsA)[:-1]
            pathB = random.choice(pathsB)
            paths.append(pathA+pathB)

    with open (f"{output_path}/{i}", "w") as f:
        for path in paths:
            f.write(", ".join(map(str,path)) + "\n")
        