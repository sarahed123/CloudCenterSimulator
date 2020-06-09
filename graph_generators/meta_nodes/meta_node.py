from tor import ToR
from server import Server

class MetaNode:
	
	def __init__(self, ToR_num, MN_id): 
		self.ToRs = []
		self.id = MN_id
		for i in range(ToR_num):
			self.ToRs.append(ToR())
	
	def make_connections(self, dest_MN, conn_num):
		assert conn_num%len(self.ToRs) == 0
		assert len(self.ToRs) == len(dest_MN.ToRs)
		matching_num = int(conn_num/len(self.ToRs))
		conns_to_make = conn_num
		for i in range(matching_num):
			for j in range(len(self.ToRs)):
				s = self.ToRs[j]
				t = dest_MN.ToRs[(i+j) % len(self.ToRs)]
				s.connect(t)
				t.connect(s)
	
	def get_least_connected_ToR(self, dest_MN):
		max_conns = ToR.out_deg
		ret = None
		edges = self.get_edges_to(dest_MN)
		sorted_ToRs = sorted(self.ToRs, key=lambda T: T.get_available_conns(), reverse=True)
		for T in sorted_ToRs:
			if len(edges[T.id]) < max_conns and T.get_available_conns():
				max_conns = len(edges[T.id])
				ret = T
		return ret
	
	def get_num_avilable_conns(self):
		res = 0
		for T in self.ToRs:
			res += T.get_available_conns()
		return res
	
	def __str__(self):
		rep = f"Meta Node with {len(self.ToRs)} num ToRs:\n"
		for ToR in self.ToRs:
			rep += str(ToR) + "\n"
		return rep

	def get_edges(self):
		edges = dict()
		for ToR in self.ToRs:
			edges[ToR.id] = ToR.get_edges()
		return edges
	
	def verify_edges(self):
		for ToR in self.ToRs:
			if(ToR.get_available_conns()):
				print(f"ToR {ToR.id} has {ToR.get_available_conns()} available conns")

	def get_edges_to(self, MN):
		edges_to_dest = {}
		MN_ToR_ids = [ToR.id for ToR in MN.ToRs]
		for ToR in self.ToRs:
			ToR_edges = ToR.get_edges()
			edges_to_dest[ToR.id] = [dest for dest in ToR_edges if dest in MN_ToR_ids]
		return edges_to_dest
	
	def get_server_edges(self):
		ret = dict()
		for ToR in self.ToRs:
			servers = ToR.get_servers()
			edges = []
			for s in servers:
				edges.append(s.id)
			ret[ToR.id] = edges
		return ret
		
	def add_servers(self, serv_num):
		servers = []
		for s in range(serv_num):
			servers.append(Server())
		for ToR in self.ToRs:
			ToR.add_servers(servers)
