import sys 
from rotor import *
import numpy as np

rotors_dir = sys.argv[1]
rotor_num = int(sys.argv[2])
cycle_num = int(sys.argv[3])
N = int(sys.argv[4])

rotors = load_rotors(rotors_dir,rotor_num)
advance_to_cycle(cycle_num,rotors)

matrix = [[1 if rotor_neighbours(j,i,rotors) else 0 for i in range(N)] for j in range(N)]
eigs, eigv = np.linalg.eig(matrix)
eigs = np.abs(eigs)
eigs.sort()
print(eigs[-2])
print(eigs)

assert eigs[-2] + 0.1 < eigs[-1]
#assert eigs[-2] < 2*np.sqrt(rotor_num - 1), eigs[-2]
