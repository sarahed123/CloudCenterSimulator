import sys
import random

matching_file = sys.argv[1]
lifts_file = sys.argv[2]
k = int(sys.argv[3])
outfile = sys.argv[4]
base_matchings = []
matchings = []

with open(matching_file, "r") as f:
	lines = f.readlines()
	for line in lines:
		base_matchings.append(list(map(lambda x: int(x),line.strip().split(" "))))

lifts = []
with open(lifts_file, "r") as f:
        lines = f.readlines()
        for line in lines:
                lifts.append(list(map(lambda x: int(x),line.strip().split(" "))))

random.shuffle(lifts)

base_len = len(base_matchings)
for base in base_matchings:
	for lift in lifts:
		matching = [-1 for i in range(k*base_len)]
		for n,m in enumerate(base):
			for j,l in enumerate(lift):
				matching[l*base_len + m] = j*base_len + n
		matchings.append(matching)

edges = [[] for i in range(len(matchings))]
for j,m in enumerate(matchings):
	assert len(set(m))==len(matchings)
    #check disjoint
	for i in range(len(m)):

		assert i == m[m[i]], str(m) + " " + str(i)
		assert len(set(m)) == len(m)
		if(i!=m[i]):
			assert not m[i] in edges[i], str(i) + " " + str(m[i])
		edges[i].append(m[i])


with open(outfile, "w") as f:
	for m in matchings:
		for j,i in enumerate(m):
			f.write(str(i) + (" " if j < len(m)-1 else ""))
		f.write("\n")
