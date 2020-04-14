import argparse
import math
from meta_node import MetaNode
from tor import ToR

def create_meta_node(meta_nodes_num, ToRs_per_meta_node):
	if ToRs_per_meta_node < args.L:
		return
	servers_in_meta_node = math.ceil(args.S/meta_nodes_num)
	out_degree = int(args.D - (servers_in_meta_node/args.L))
	overs = ((out_degree*ToRs_per_meta_node)/servers_in_meta_node)/(meta_nodes_num - 1)
	print(f"Trying with meta node num {meta_nodes_num} with {args.L} lanes")
	if overs >= args.O:
		print(f"found with oversubscription {overs}")
		return True
	return False

def create_meta_nodes(ToRnum):
	if ToRnum >= args.L and (args.S/args.L) <= args.D:
		return 1
	for t in range(2, ToRnum+1):
		if ToRnum%t==0:
			print(f"Trying with {ToRnum} ToRs")
			if(create_meta_node(t,ToRnum/t)):
				return t
	return 0

def verify_edges(sMN, tMN):
	sMN.verify_edges()
	edges_to_dest = sMN.get_edges_to(tMN)
	for n in edges_to_dest:
		print(edges_to_dest)
		assert len(edges_to_dest[n]) <= math.ceil(conns_between_mns/ToRs_per_meta_node)
		assert len(edges_to_dest[n]) >= math.floor(conns_between_mns/ToRs_per_meta_node)
	sum_edges = 0
	for edge_list in edges_to_dest.values():
		sum_edges+=len(edge_list)
	assert sum_edges == conns_between_mns

def create_graph(meta_nodes_num, ToRs):
	if ToRs%meta_nodes_num != 0:
		raise Error
	MNs = []
	for i in range(meta_nodes_num):
		MNs.append(MetaNode(ToRs_per_meta_node, i))
	for i in range(len(MNs)):
		for j in range(i+1, len(MNs)):
			MNs[i].make_connections(MNs[j], conns_between_mns)
	edges = dict()
	for sMN in MNs:
		for tMN in MNs:
			if sMN is tMN:
				continue
			verify_edges(sMN, tMN)

	for MN in MNs:
		edges.update(MN.get_edges())
	
		
	print(edges)
	total_servers = args.S
	for MN in MNs:
		servers_to_add = math.ceil(args.S/meta_nodes_num)
		if servers_to_add > total_servers:
			servers_to_add = total_servers
		total_servers-=servers_to_add
		MN.add_servers(servers_to_add,args.L)
		server_edges = MN.get_server_edges()
		edges.update(server_edges)
	
	
parser = argparse.ArgumentParser()
parser.add_argument("--s", type=int, help="server number", dest="S", required=True)
parser.add_argument("--overs", type=float, help="Oversubscription", dest="O", required=True)
parser.add_argument("--lanes", type=int, help="Number of lanes", dest="L", required=True)
parser.add_argument("--deg", type=int, help="ToR degree", dest="D", required=True)

args = parser.parse_args()

ToRs = 0
while ToRs <= args.S:
	meta_nodes_num =  create_meta_nodes(ToRs)
	if(meta_nodes_num):
		print(f"found meta node num {meta_nodes_num} with ToR num {ToRs}")
		ToRs_per_meta_node = int(ToRs/meta_nodes_num)
		servers_per_mn = int(math.ceil(args.S/meta_nodes_num))
		ToR.out_deg = args.D - servers_per_mn/args.L
		conns_between_mns = int(ToRs_per_meta_node*(ToR.out_deg/(meta_nodes_num - 1))) if meta_nodes_num > 1 else 0
		create_graph(meta_nodes_num, ToRs)
		break
	ToRs+=1
else:
	print("could not find anything")


