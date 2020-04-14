from tor import ToR

class MetaNode:
	
	def __init__(self, ToR_num, MN_id): 
		self.ToRs = []
		self.id = MN_id
		for i in range(ToR_num):
			self.ToRs.append(ToR(ToR_num*MN_id + i))
	
	def make_connections(self, dest_MN, conn_num):
		conns_to_make = conn_num
		while conns_to_make > 0:
			source = self.get_least_connected_ToR()
			target = dest_MN.get_least_connected_ToR()
			source.connect(target)
			target.connect(source)
			conns_to_make-=1
	
	def get_least_connected_ToR(self):
		max_conns = 0
		ToR = None
		for T in self.ToRs:
			conns = T.get_available_conns()
			if conns > max_conns:
				max_conns = conns
				ToR = T
		return ToR
	
	def __str__(self):
		rep = f"Meta Node with {len(self.ToRs)} num ToRs:\n"
		for ToR in self.ToRs:
			rep += str(ToR) + "\n"
		return rep

	def get_edges(self):
		edges = dict()
		for ToR in self.ToRs:
			print(f"getting edges from {ToR.id}")
			edges[ToR.id] = ToR.get_edges()
		return edges
	
	def verify_edges(self):
		for ToR in self.ToRs:
			ToR.verify_edges()

	def get_edges_to(self, MN):
		edges_to_dest = {}
		MN_ToR_ids = [ToR.id for ToR in MN.ToRs]
		for ToR in self.ToRs:
			ToR_edges = ToR.get_edges()
			edges_to_dest[ToR.id] = [dest for dest in ToR_edges if dest in MN_ToR_ids]
		return edges_to_dest

	def add_servers(self, serv_num, lane_num):
		servers = []
		for s in range(serv_num):
			servers.append(Server(lane_num))
		for ToR in self.ToRs:
			ToR.add_servers(servers)
