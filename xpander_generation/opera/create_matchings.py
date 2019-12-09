import sys
import random

def switch_prev(matching, curr_row_index):
    choices = []
    for i in range(len(matrix)):
        if matching[i]==-1:
            choices.append(i)
    rows = {}
    for j,row in enumerate(matrix):
        # if matching[j]==-1:
        #     continue
        row_choices = []
        for i in row:
            if i in choices:
                row_choices.append(i)
        if row_choices:
            rows[j] = row_choices
    if not rows.keys():
        return 0
    row_to_switch = random.choice(list(rows.keys()))
    new_match = random.choice(rows[row_to_switch])
    old_match = matching[row_to_switch]
    matching[old_match] = -1
    matrix[row_to_switch].append(old_match)
    matrix[old_match].append(row_to_switch)
    matching[row_to_switch] = new_match
    matrix[row_to_switch].remove(new_match)
    if(new_match != row_to_switch):
        matching[new_match] = row_to_switch
        matrix[new_match].remove(row_to_switch)
    return old_match


output_file = sys.argv[1]
matchings_num = int(sys.argv[2])


matrix = [[i for i in range(matchings_num)] for j in range(matchings_num) ]

matchings = []
for i in range(len(matrix)):
    matching = [-1 for k in range(matchings_num)]
    while -1 in matching:
        j = matching.index(-1)
        row = matrix[j]
        if matching[j] != -1:
            continue
        choices = list(filter(lambda x: x not in matching,row))
        if not choices:
            matching[j] = j
            continue
            
        elem = random.choice(choices)
        row.remove(elem)
        matching[j] = elem
        if elem!=j:
            matching[elem] = j
            matrix[elem].remove(j)
        
    print(f"matching {i} complete")
    matchings.append(matching)

edges = {}
for i in range(len(matrix)):
    edges[i] = []
for j,m in enumerate(matchings):
    assert len(set(m))==len(matrix)
    #check disjoint
    for i in range(len(m)):
        if(i!=m[i]):
            assert not m[i] in edges[i]
        edges[i].append(m[i])

with open(output_file, "w") as f:
    for m in matchings:
        for i in m:
            f.write(str(i) + " ")
        f.write("\n")