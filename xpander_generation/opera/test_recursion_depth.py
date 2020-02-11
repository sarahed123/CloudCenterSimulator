import sys

sys.setrecursionlimit(100000)
def func(n):
	if n==1:
		print("success")
		return
	func(n-1)
func(90000)

