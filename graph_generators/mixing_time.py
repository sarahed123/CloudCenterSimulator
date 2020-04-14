import numpy as np
import sys
from to_mat import to_mat

def check_percision(v,p):
	l = 1/len(v)
	for c in v:
		if abs(c-l) > p:
			return False
	return True

graph_file = sys.argv[1]
N = int(sys.argv[2])
d = int(sys.argv[3])
percision = float(sys.argv[4])

graph = to_mat(graph_file,N)
M = np.array(graph)
M = M/d
total = 0
print(np.shape(M))
for c in range(N):
	
	v = np.zeros(N)
	v[c] = 1
	i=0
	while not check_percision(v,percision):
		v=M.dot(v)
		i+=1

	total+=i

print(total/N)
