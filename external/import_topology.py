import sys


edges_list = []
nodes = set()
topology_path = sys.argv[1]
run_dir = "runs/" + sys.argv[2]
topology_dir = "/cs/labs/schapiram/inonkp/smallgraphs/" + run_dir + "/topologies/"
with open(topology_path, "r") as f:
    edges_list += f.readlines()

    for edge in edges_list:
        edge_split = edge.strip().split(" ")
        nodes.add(edge_split[0])
        nodes.add(edge_split[1])

topology_path_split = topology_path.strip().split("/")
properties = topology_path_split[-1].split("\\.")[0] + ".properties"
with open(topology_dir + properties, "w") as f:
    f.write("num_edges=" + str(len(edges_list))+ "\n")
    f.write("num_nodes=" + str(len(nodes)) + "\n")
    f.write("switches_which_are_tors=" + "incl_range" + "(" + "0" + "," +  str(len(nodes)-1) + ")" + "\n")
    f.write("servers="+"set()"+"\n")
    f.write("switches=" + "incl_range"+ "("+ "0"+"," + str(len(nodes)-1) + ")" + "\n")
    f.write("edges=\\" + "\n")
    for i,edge in enumerate(edges_list):
        f.write(edge.strip())
        f.write(",\\\n" if i<len(edges_list)-1 else "")
