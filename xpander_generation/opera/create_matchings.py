import sys
import random

def switch_prev(matching, curr_row_index):
    choices = []
    for i in range(len(matrix)):
        if i not in matching:
            choices.append(i)
    rows = {}
    for j,row in enumerate(matrix):
        if j==len(matching):
            break
        row_choices = []
        for i in row:
            if i in choices:
                row_choices.append(i)
        if row_choices:
            rows[j] = row_choices
    
    row_to_switch = random.choice(list(rows.keys()))
    new_match = random.choice(rows[row_to_switch])
    matrix[row_to_switch].append(matching[row_to_switch])
    matching[row_to_switch] = new_match
    matrix[row_to_switch].remove(new_match)

output_file = sys.argv[1]
matchings_num = int(sys.argv[2])
rotor_num = int(sys.argv[3])
matchings_per_rotor = matchings_num//rotor_num

matrix = [[i for i in range(matchings_num) if i != j] for j in range(matchings_num) ]

matchings = []
for i in range(len(matrix) - 1):
    matching = []
    for j,row in enumerate(matrix):

        choices = list(filter(lambda x: x not in matching,row))
        while not choices:
            switch_prev(matching,j)
            choices = list(filter(lambda x: x not in matching,row))
        
        elem = random.choice(choices)
        row.remove(elem)
        matching.append(elem)
    print(f"matching {i} complete")
    matchings.append(matching)

edges = {}
for i in range(len(matrix)):
    edges[i] = []
for j,m in enumerate(matchings):
    assert j!=m[j], f"Bad match {m} at index {j}"
    assert len(set(m))==len(matrix)
    #check disjoint
    for i in range(len(m)):
        assert not m[i] in edges[i]
        edges[i].append(m[i])

with open(output_file, "w") as f:
    for m in matchings:
        for i in m:
            f.write(str(i) + " ")
        f.write("\n")