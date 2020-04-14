
class ToR:
	ToR_num = 0	
	def __init__(self, ToR_id):
		self.id = ToR_id
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
		assert self.get_available_conns() == 0
		
	def add_servers(self, servers)
		self.servers = servers
