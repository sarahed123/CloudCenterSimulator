import shortest_paths as sp
from pprint import pprint
import sys

parser = sp.get_parser()
parser.add_argument("--g-file", help="the graph file", dest="file")
args = parser.parse_args()
sp.args = args
graphs = []
with open (args.file, "r") as f:
	lines = f.readlines()
	for l in lines:
		graphs.append(l.strip())
ret = sp.compute(graphs, args)
ret = {k: v for k, v in sorted(ret.items(), key=lambda item: item[1])}
for r in ret:
	print(f"{r} {ret[r]}")
