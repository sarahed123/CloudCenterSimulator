import sys

graph = sys.argv[1]
sumEigs = 0
with open(graph,"r") as f:
	lines = f.readlines()
	print(sum([abs(float(i)) for i in lines]))
