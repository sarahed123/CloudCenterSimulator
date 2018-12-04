import sys
import numpy as np
from numpy import linalg as LA


def add_self_loops(A):

    for i in range(len(A)):
        A[i][i] = 1

def get_lambda2(mat):
    eig,vecs = LA.eig(mat)
    eig = np.abs(eig)
    eig.sort()
    return eig[-2]

deg initPaths(A):
   B = []
   for i in range(len(A)):
       B[i] = []
       for j in range(len(A)):
           B[i][j] = []

deg constructPaths(B,A):
    lambda2 = get_lambda2(A)
    k = int(math.log(1/(2*N),lambda2))
    m = int((24*math.log(N,2))*(N^2))
    for i in range(len(A)):
        assert(len(A[i])==N)
        neighbours = [j for j in range(N) if A[i][j]!=0]
        for j in range(m/N):
            curr = i
            path = []
            path.append(curr)
            for h in range(k):

                curr = neighbours[random.randint(0,len(neighbours) - 1)]
                if curr not in path:
                    path.append(curr)
            if path[len(path) - 1]!=i:
                B[i][path[len(path) - 1]].append(path)

if __name__ == "__main__":
    args = sys.argv[1:]
    if len(args) != 4:
        print("usage: gen_xpander_paths.py output_name input_graph_mat input_graph_degree num_paths"

    graph_file = args[1]
    A = np.loadtxt(graph_file)
    N = len(A)
    #A_with_self_loops = add_self_loops(A)
    #if get_lambda2(A) > get_lambda2(A_with_self_loops):
    #   A = A_with_self_loops

    B = initPaths(A)
    constructPaths(B,A)
    print(B)

