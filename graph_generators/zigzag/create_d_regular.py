import sys

n = int(sys.argv[1])
d = int(sys.argv[2])
if not n%2==0:
	print(str(n) + " should be even")
with open("n"+str(n)+"_d"+str(d)+".topology", "w") as f:
	for i in range(n):
		f.write(str(i) + " " + str((i+1) % n))
		f.write("\n")
		f.write(str(i) + " " + str((i-1) % n))
		f.write("\n")
		if d%2==1:
			f.write(str(i) + " " + str((i + n/2) % n))
		j = d -2
		while(j>0):
			f.write(str(i) + " " + str((i + n/2 - j/2) % n))
			f.write("\n")
			f.write(str(i) + " " + str((i + n/2 + j/2) % n))
			f.write("\n")
			j=j-2
	f.close()
