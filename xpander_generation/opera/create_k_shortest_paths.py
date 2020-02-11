import sys
import math
import os
from rotor import *
import random

rotors_dir = sys.argv[1]
output_dir = sys.argv[2]
rotor_num = int(sys.argv[3])
cycle_num = int(sys.argv[4])
N = int(sys.argv[5])
K = int(sys.argv[6])
print(cycle_num)
def find_k_shortest_paths(s,t,paths):
	queue = [[(s,)]]
	while queue:
		curr = queue.pop(0)
		if curr[-1][-1] == t:
			path = curr[:]
			paths.append(path)
			if len(paths) == K:
				return
			continue	
		neigh = get_rotor_neighbours(curr[-1][-1],rotors)
		random.shuffle(neigh)
		for nei in neigh:
			if nei[1] in list(map(lambda x: x[-1],curr)):
				continue
			path = curr[:]
			path.append(nei)
			queue.append(path)


rotors = load_rotors(rotors_dir,rotor_num)

curr_rotor_index = 0
cycle = 0
while cycle < cycle_num:
    rotors[curr_rotor_index].advance()
    curr_rotor_index = (curr_rotor_index + 1) % len(rotors)
    cycle+=1

final_paths = [[-1 for i in range(N)] for j in range(N)]
for i in range(N):
	for j in range(N):
		if i==j:
			continue
		paths = []
		find_k_shortest_paths(i,j,paths)
		final_paths[i][j] = paths

if not os.path.exists(output_dir + "/" + "cycle_" + str(cycle_num)):
    os.makedirs(output_dir + "/" + "cycle_" + str(cycle_num))

for i in range(N):
    with open(output_dir + "/" + "cycle_" + str(cycle_num) + "/" + "n" + str(i), "w") as f:
        for j in range(N):
            if i==j:
                continue
            f.write(f"{j}:")
            for m,path in enumerate(final_paths[i][j]):
                for node in path[1:]:
                   f.write(str(node[0]) + "-" + str(node[1]) + " ")
                f.write("," if m < len(final_paths[i][j]) else "")
            f.write("\n")
