import sys
import numpy as np
from numpy import linalg as LA
from scipy.sparse import *
from scipy.sparse.linalg import eigs

def get_lambda2(mat):
    eig,vecs = eigs(mat)
    eig = np.abs(eig)
    eig.sort()
    return eig[-2]

def get_spectral_gap(d):
    return 2*np.sqrt(d-1)

def is_ramanujan(mat,d):
    return get_lambda2(mat) < get_spectral_gap(d)

# d= the degree of the graph
# k= number of lifts to perform
# e.g.,: random_k_lift(4,6) will create a 4 regualr graph with 30 nodes
def random_k_lift(d, k):
    num_nodes = (d+1)*k
    mat = lil_matrix((num_nodes,num_nodes))


    # meta_ind_to_nid = {}
    # for

    # go over all meta nodes
    for meta1 in range(d+1):
	print(meta1)
        # connect to any other meta node
        for meta2 in range(meta1+1, d+1):

            # connect the ToRs between the meta-nodes randomally
            perm = np.random.permutation(k)
            for src_ind in range(k):
                src = meta1*k + src_ind
                dst = meta2*k + perm[src_ind]

                # connect the link
                mat[src,dst] = 1
                mat[dst,src] = 1

    if not is_ramanujan(mat,d):
        # try again if we got a bad Xpander
        return random_k_lift(d,k)

    return mat

def main(outname, d, n, delim=','):
    if n%(d+1) != 0:
        print("This script supports only multiplications of d+1 (the degree plus 1), now quitting")
        sys.exit(0)

    lambda2 = 999999
    mat = 0
    for i in range(1):
        cur_mat = random_k_lift(d, n//(d+1))
        cur_lambda = get_lambda2(cur_mat)
        print(cur_lambda)
        if(cur_lambda<lambda2):
            lambda2 = cur_lambda
            mat = cur_mat
    np.savetxt(outname + "_mat", mat, delimiter=delim)
    print(lambda2)
    with open(outname, 'w') as f:
        for i in range(n):
            for j in range(n):
                if i==j or mat[i,j]==0: continue
                f.write("%d %d\n"%(i,j))
    

if __name__ == "__main__":
    args = sys.argv[1:]
    if len(args) != 3:
        print("Usage: gen_xpander.py <out_file> <switch network degree>(int) <num switches>(int)")
    else:
        main(args[0], int(args[1]), int(args[2]))
