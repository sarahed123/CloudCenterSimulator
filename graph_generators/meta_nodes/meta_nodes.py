import argparse
import math
from meta_node import MetaNode
from tor import ToR
import os

def create_meta_node(meta_nodes_num, ToRs_per_meta_node):
	if ToRs_per_meta_node < args.L:
		return
	servers_in_meta_node = math.ceil(args.S/meta_nodes_num)
	s_deg = math.ceil(servers_in_meta_node/args.L)
	out_degree = args.D - s_deg
	if out_degree%(meta_nodes_num - 1) != 0:
		return False
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
	max_edges = max([len(edges_to_dest[n]) for n in edges_to_dest])
	min_edges = max([len(edges_to_dest[n]) for n in edges_to_dest])
	assert min_edges == max_edges
	sum_edges = 0
	for edge_list in edges_to_dest.values():
		#assert len(set(edge_list)) == len(edge_list)
		print((edge_list), end=" ")
		sum_edges+=len(edge_list)
	print()
	assert sum_edges == conns_between_mns


def create_file(output_file, edges):
	with open(output_file, "w") as f:
		for s in edges:
			for t in edges[s]:
				f.write(f"{s} {t}\n")

def extend_file(output_file,server_edges):
	with open(output_file, "a") as f:
		for s in server_edges:
			for t in server_edges[s]:
				f.write(f"{s} {t}\n")
				f.write(f"{t} {s}\n")
		
def create_graph(meta_nodes_num, ToRs):
	if ToRs%meta_nodes_num != 0:
		raise Error
	MNs = []
	for i in range(meta_nodes_num):
		MNs.append(MetaNode(ToRs_per_meta_node, i))
	for i in range(len(MNs)):
		for j in range(i+1, len(MNs)):
			MNs[i].make_connections(MNs[j], conns_between_mns)
		print(MNs[i].get_edges())
	while True:
		try:
			print("should only be printed once")
			sorted_MNs = sorted(MNs, key=lambda M: M.get_num_avilable_conns(), reverse=True)
			for i in range(1,len(sorted_MNs)):
				edges_as_list = sum(sorted_MNs[0].get_edges_to(sorted_MNs[i]).values(), [])
				print(edges_as_list)
				if len(edges_as_list) <=  conns_between_mns: 
					sorted_MNs[0].make_connections(sorted_MNs[i], 1)
					print(f"connecting {sorted_MNs[i].id} to {sorted_MNs[i+1].id}")
					break
			else:
				break
		except Exception as e:
			print(e)
			break
	for M in MNs:
		print(M.get_edges())
	edges = dict()
	for sMN in MNs:
		for tMN in MNs:
			if sMN is tMN:
				continue
			verify_edges(sMN, tMN)

	for MN in MNs:
		edges.update(MN.get_edges())
	
		
	print("ToR edges: ", edges)
	total_servers = args.S
	server_edges = {}
	for MN in MNs:
		servers_to_add = math.ceil(args.S/meta_nodes_num)
		if servers_to_add > total_servers:
			servers_to_add = total_servers
		total_servers-=servers_to_add
		MN.add_servers(servers_to_add)
		MN_server_edges = MN.get_server_edges()
		server_edges.update(MN_server_edges)
	print("servers: " ,server_edges)
	output_file = f"topologies/S{args.S}_O{args.O}_L{args.L}_D{args.D}.topology"
	create_file(output_file, edges)
	extend_file(output_file, server_edges)
	
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
		ToR.s_lanes = args.L
		ToR.s_deg = math.ceil(servers_per_mn/args.L)
		ToR.out_deg = args.D - ToR.s_deg
		conns_between_mns = int(ToRs_per_meta_node*(ToR.out_deg/(meta_nodes_num - 1))) if meta_nodes_num > 1 else 0
		print(f"Servers per MN {servers_per_mn} ToRs per MN {ToRs_per_meta_node}")
		print(f"ToR server lanes: {ToR.s_lanes} ToR server deg: {ToR.s_deg}")
		print(f"ToR out deg: {ToR.out_deg} Connections between MNs {conns_between_mns}")
		create_graph(meta_nodes_num, ToRs)
		break
	ToRs+=1
else:
	print("could not find anything")


