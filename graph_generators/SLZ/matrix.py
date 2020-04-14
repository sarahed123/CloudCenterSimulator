import numpy as np

class Matrix:
	def __init__(self, M):
		self.M = M

	def __iter__(self):
		return iter(tuple(np.array(self.M).flatten()))

	def __eq__(self, M):
		return tuple(self) == tuple(M)

	def __hash__(self):
		return hash(tuple(self))

	def remainder(self, q):
		return self.to_matrix((np.remainder(self.M,q)))

	def dot(self, M):
		return self.to_matrix(self.M.dot(M.M))

	def to_matrix(self, M):
		return Matrix(M)

	def __str__(self):
		return str(tuple(self))
	
	def __repr__(self):
		return str(tuple(self))
