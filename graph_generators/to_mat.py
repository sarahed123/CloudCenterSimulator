import numpy as np
import sys

def to_mat(graph_file, n):
	mat = np.zeros((n,n))
	with open(graph_file, "r") as f:
		line = f.readline()
		while(line):
			if line.startswith("#"):
				line = f.readline()
				continue
			vertices = line.split(" ")
			mat[int(vertices[0])][int(vertices[1])] = 1
			line = f.readline()
	return mat

def list_graph_to_mat(list_graph):
	mat = np.zeros((len(list_graph),len(list_graph)))
	for i in range(len(list_graph)):
		for j in range(len(list_graph)):
			mat[i][j] = int(list_graph[i][j])
	return mat

def mat_to_file(mat,outfile):
	with open(outfile, "w") as out:
		for row in mat:
			for elem in row[:-1]:
				out.write(str(int(elem))+" ")
			out.write(str(int(row[-1])))
			out.write("\n")

if __name__ == '__main__':
	N = int(sys.argv[2])
	graph_path = sys.argv[1]
	mat = to_mat(graph_path,N)
	mat_to_file(mat,sys.argv[3])
				
