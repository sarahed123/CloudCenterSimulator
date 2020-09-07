from path_db import PathDB
import argparse
import random
import math

parser = argparse.ArgumentParser()
parser.add_argument("--N", help="Num nodes", dest="N", type=int, required=True)
parser.add_argument("--path-db", required=True, help="The paths DB", dest="path_db")
parser.add_argument("--seed", help="seed", required=True, dest="seed", type=int)
parser.add_argument("--show-prefix", default=0.04, dest="prefix", type=float, help="Show this prefix of the least connected pairs")
args = parser.parse_args()
N = args.N
db = PathDB(args.path_db, args.N)
random.seed(args.seed)
paths = {}
for i in range(N):
    for j in range(N):
        if i == j:
            continue
        paths[(i,j)]= len(db.get_paths(i,j))
items = list(paths.items())
random.shuffle(items)
items = [(p[0], p[-1]) for p in sorted(items, key=lambda item: item[1] )]
paths = dict(items)
prefix = math.ceil((N*(args.prefix/2)))
pairs = list(paths.keys())
least_connected = []
for i in range(prefix):
    least_connected.append(pairs[i][0])
    least_connected.append(pairs[i][1])
print(" ".join([str(v) for v in least_connected]))

