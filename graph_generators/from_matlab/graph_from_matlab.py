import sys

inGraph = sys.argv[1]
outfile = sys.argv[2]
edges = {}
with open(inGraph, "r") as f:
	line = f.readline()
	while line:
		vertices = line.strip().split(",")
		vertexList = edges.get(int(vertices[0])-1,[])
		vertexList.append(int(vertices[1]) -1)
		edges[int(vertices[0])-1] = vertexList

		vertexList = edges.get(int(vertices[1])-1,[])
                vertexList.append(int(vertices[0]) - 1)
                edges[int(vertices[1])-1] = vertexList

		line = f.readline()
#print(edges)
with open(outfile,"w") as f:
	for vertex in edges:
		for v in edges[vertex]:
			f.write(str(vertex) + " " + str(v) + "\n")
	f.close()
