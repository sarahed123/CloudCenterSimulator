import sys
import math
import os
from rotor import *


def paths_exists(i,j,l):
    if l==N:
        return False
    if i==j:
        return True
    exists = True
    for n in paths[i][j]:
        exists = paths_exists(n[1],j,l+1)
        if not exists:
            break
    return exists

rotors_dir = sys.argv[1]
output_dir = sys.argv[2]
rotor_num = int(sys.argv[3])
cycle_num = int(sys.argv[4])
N = int(sys.argv[5])

rotors = load_rotors(rotors_dir,rotor_num)

curr_rotor_index = 0
cycle = 0
while cycle < cycle_num:
    rotors[curr_rotor_index].advance()
    curr_rotor_index = (curr_rotor_index + 1) % len(rotors)
    cycle+=1

# for i,rotor in enumerate(rotors):
#     print(f"rotor {i} {rotor.matchings}")
# print()
# for i,rotor in enumerate(rotors):
#     print(f"rotor {i} {rotor}")


matrix = [[0 if i==j else (1 if rotor_neighbours(j,i,rotors) else math.inf) for i in range(N)] for j in range(N)]


for k in range(0,N):
    for i in range(0,N):
        for j in range(0,N):
            if matrix[i][j] > matrix[i][k] + matrix[k][j]:
                matrix[i][j]= matrix[i][k] + matrix[k][j]

paths = [[[] for i in range(N)] for j in range(N)]

for k in range(0,N):
    for i in range(0,N):
        if i==k:
            continue
        for j in get_rotor_neighbours(k,rotors):
            if matrix[k][i] == matrix[j[1]][i] + 1:
                paths[k][i].append(j)

for i in range(N):
    for j in range(N):
        if i==j:
            continue
        assert paths[i][j], "path between " + str(i) + " to " + str(j) + " doesnt exists"
        assert paths_exists(i,j,0),"path between " + str(i) + " to " + str(j) + " doesnt exists"

if not os.path.exists(output_dir + "/" + "cycle_" + str(cycle_num)):
    os.makedirs(output_dir + "/" + "cycle_" + str(cycle_num))

for i in range(N):
    with open(output_dir + "/" + "cycle_" + str(cycle_num) + "/" + "n" + str(i), "w") as f:
        if i==j:
            continue
        for j in range(N):
            f.write(str(j) + ":")
            for k,p in enumerate(paths[i][j]):
                f.write(str(p[0]) + "-" + str(p[1]) + ("," if k!=len(paths[i][j])-1 else ""))
            f.write("\n")