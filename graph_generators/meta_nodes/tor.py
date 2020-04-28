
class ToR:
	ToR_num = 0	
	def __init__(self):
		self.id = ToR.ToR_num
		ToR.ToR_num += 1
		self.connections = []

	def connect(self, ToR):
		self.connections.append(ToR)
		

	def get_available_conns(self):
		return ToR.out_deg - len(self.connections)

	def __str__(self):
		return f"ToR with {len(self.connections)} out going connections"

	def get_edges(self):
		edges = []
		for ToR in self.connections:
			edges.append(ToR.id)
		return edges

	def verify_edges(self):
		assert self.get_available_conns() == 0, f"tor {self.id} has {self.get_available_conns()} conns available"
		
	def add_servers(self, servers):
		assert ToR.s_lanes*ToR.s_deg >= len(servers)
		self.servers = servers

	def get_servers(self):
		return self.servers
