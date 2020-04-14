from matrix import Matrix

class PMatrix(Matrix):
	
	def __eq__(self, M):
		#print(tuple(self) == tuple(M.negate()))
		return tuple(self) == tuple(M) or tuple(self) == tuple(M.negate())
		

	def negate(self):
		M = self.M * -1
		r = self.to_matrix(M).remainder(3)
		return r

	def to_matrix(self, M):
		return PMatrix(M.astype(int))
	
	def __hash__(self):
		as_tup = tuple(self)
		neg_tup = tuple(self.negate())
		first_non_zero = 0
		for i in range(len(as_tup)):
			if as_tup[i] != 0:
				first_non_zero = i
				break
		#print(as_tup, first_non_zero)
		return hash(as_tup) + hash(neg_tup)
