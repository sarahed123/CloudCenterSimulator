import sys
import json

graph_file = sys.argv[1]
with open(graph_file, 'r') as f:
	with open("tmp.topology", "w") as out:
		list_graph = json.load(f)
		for i in range(len(list_graph)):
			for j in range(len(list_graph)):
				if int(list_graph[i][j])==1:
					out.write(str(i) + " " + str(j) + "\n")
	out.close()
