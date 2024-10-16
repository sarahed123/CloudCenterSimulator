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
odd = True
if matchings_num%2==0:
	odd = False
sys.setrecursionlimit(15000)
def find_matchings_row_recursive(row_num):
    if row_num >= len(matrix):
        return True
    ret = False
    print(row_num)
    while not ret:

        elem_from_choices = [i for i in range(len(matrix[row_num])) if matrix[row_num][i]==-1]

        while elem_from_choices:

            elem_from = random.choice(elem_from_choices)

            elem_to_choices = [i for i in range(len(matrix[row_num])) if matrix[row_num][i]==-1]
            while elem_to_choices:

                elem_to = random.choice(elem_to_choices)
                colunm = [matrix[i][elem_to] for i in range(len(matrix))]
                if elem_from in colunm:
                    if row_num==0:
                        print(matrix)
                        print(elem_to_choices)
                    elem_to_choices.remove(elem_to)
                    continue

                matrix[row_num][elem_to] = elem_from
                matrix[row_num][elem_from] = elem_to
                elem_from_choices.remove(elem_from)
                if elem_from!=elem_to:
                    elem_from_choices.remove(elem_to)
                break
            if matrix[row_num][elem_from]==-1:
                matrix[row_num] = [-1 for i in range(matchings_num)]
                return False


        ret = find_matchings(row_num+1)
        if not ret:
            matrix[row_num] = [-1 for i in range(matchings_num)]
        else:
            return True
    
#this works best for odd number of requested matchings
def find_matchings_element_recursive(row_num,row_successes):
    if row_num >= len(matrix):
        return True
    elem_from_choices = [i for i in range(len(matrix[row_num])) if matrix[row_num][i]==-1]
    elem_from = random.choice(elem_from_choices)

    colunm = [matrix[i][elem_from] for i in range(0, row_num)]
    elem_to_choices = list(filter(lambda x: x not in colunm and x not in matrix[row_num], range(len(matrix))))
    if odd and elem_from in elem_to_choices:
        elem_to_choices.remove(elem_from)
    while elem_to_choices:
        elem_to = random.choice(elem_to_choices)
        matrix[row_num][elem_to] = elem_from
        matrix[row_num][elem_from] = elem_to
        next_row_successes = row_successes +  (1 if elem_from == elem_to else 2)
        new_row_num = row_num if next_row_successes < len(matrix) else row_num + 1
        ret = find_matchings_element_recursive(new_row_num,next_row_successes if new_row_num == row_num else (1 if odd else 0))
        if not ret:
            elem_to_choices.remove(elem_to)
            matrix[row_num][elem_to] = -1
            matrix[row_num][elem_from] = -1
            continue
        return True
    return False    
    



matrix = [[-1 if i!=j else (i if odd else -1) for i in range(matchings_num)] for j in range(matchings_num) ]

matchings = []
print(find_matchings_element_recursive(0,1 if odd else 0))

print(matrix)
edges = {}
for i in range(len(matrix)):
    edges[i] = []
for j,m in enumerate(matrix):
    assert len(set(m))==len(matrix)
    #check disjoint
    for i in range(len(m)):

        assert i == m[m[i]], str(m) + " " + str(i)
        assert len(set(m)) == len(list(m))
        assert not m[i] in edges[i]
        edges[i].append(m[i])

with open(output_file, "w") as f:
    for m in matrix:
        for i in m:
            f.write(str(i) + " ")
        f.write("\n")
