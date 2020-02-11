import sys

if __name__ == "__main__":
    N = int(sys.argv[2])
    with open(sys.argv[1], 'r') as f:
        rows = f.readlines()
        A = [[] for j in range(int(N))]
        for row in rows:
            nodes = row.split(" ")
            A[int(nodes[0])].append(int(nodes[1]))
    with open(sys.argv[1]+'_mat','w') as f:
        for i in range(N):
            for j in range(N):
                if j in A[i]:
                    f.write("1 ")
                else:
                    f.write("0 ")
            f.write("\n")

