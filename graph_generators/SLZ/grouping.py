import numpy as np

class RemainderMatrix:
    q = 3
    def __init__(self, M):
        self.M = np.remainder(M,RemainderMatrix.q)
        self.OM = M

    def __mul__(self,other):
        return RemainderMatrix(np.remainder(self.M*other.M,RemainderMatrix.q))

    def I(self):
        return RemainderMatrix(np.remainder(self.OM.I, RemainderMatrix.q))

    def __eq__(self, other):
        return (self.M==other.M).all()

    def __iter__(self):
        arr = np.ravel(self.M)
        for a in arr:
            yield a

    def __hash__(self):
        return hash(tuple(self))

    def __str__(self):
        return str(self.M)

    def neighs(self, gens):
        return [M for M in [self*G for G in gens]]

B = RemainderMatrix(np.matrix([[1,0],[1,1]]))
BI = B.I()
A = RemainderMatrix(np.matrix([[0,-1],[1,0]]))
AI = A.I()
gens = [B,BI,A,AI]
I = RemainderMatrix(np.matrix([[1,0],[0,1]]))

base_elements = [I,A, B*A , B*A*B*A]
base_elements += [M*A*A for M in base_elements]

total = set(base_elements)
print(len(set(total)))
groupings = []
for M in base_elements:
    group = [M, M*A*B, M*A*B*A*B]
    groupings.append(group)


flattened = set()
for g in groupings:
    for M in g:
        flattened.add(M)


print(len(flattened))
vertices = dict()
k = 0
double_groupings = []
for i,group1 in enumerate(groupings):
    for j,group2 in enumerate(groupings):
        if i>=j:
            continue

        for M in group1:
            neighs = M.neighs(gens)
            if(set(neighs) & set(group2)):
                break
        else:
            double_grouping = group1+group2
            for M in double_grouping:
                vertices[M] = k
                k+=1
            double_groupings.append(double_grouping)

for i,group1 in enumerate(double_groupings):
    for j,group2 in enumerate(double_groupings):
        if i==j:
            continue
        neigh_set = []
        for M in group1:
            neighs = list(set(group2).intersection(set(M.neighs(gens))))
            print(f"{vertices[M]} to group {j} neighs {len(neighs)}")
            neigh_set += neighs

        print(f"{i} {j}")
        for M in set(neigh_set):
            print(vertices[M], end=" ")
        print()

