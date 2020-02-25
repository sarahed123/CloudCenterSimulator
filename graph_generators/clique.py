import sys

N = int(sys.argv[1])

with open("clique_n" + str(N) + ".topology", "w") as f:
	for i in range(N):
		for j in range(N):
			if i==j:
				continue
			f.write(str(i) + " " + str(j) + "\n")
	f.close()
