from tor import ToR
class Server:
	S_num = 0
	def __init__(self):
		self.id = ToR.ToR_num + Server.S_num
		Server.S_num += 1
