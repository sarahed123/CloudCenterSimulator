import sys

matrix = []
matching_file = sys.argv[1]
with open(matching_file,"r") as f:
	lines = f.readlines()
	for line in lines:
		matrix.append(list(map(lambda x : x , line.strip().split(" "))))

new_id = len(matrix) + 1
new_matching = []
for row in range(len(matrix)):
	colunm = [matrix[i][row] for i in range(len(matrix))]
	to_switch_index = colunm.index(row)
	prev = matrix[row][row]
	matrix[row][row] = i
	matrix[to_switch_index][row] = prev
	matrix[to_switch_index][prev] = row
	
matrix.append(new_matching)
