
class Server:
	S_num = 0
	def __init__(self, lane_num):
		self.lane_num = lane_num
		self.id = ToR.ToR_num + Server.S_num
		Server.S_num += 1
