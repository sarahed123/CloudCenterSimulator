import matplotlib.pyplot as plt
from pprint import pprint

def get_data(graph_file):
	d = dict()
	with open(graph_file) as f:
		lines = f.readlines()
		for l in lines:
			kv = l.strip().split(":")
			k = float(kv[0])
			v = float(kv[1])
			d[k] = v
	return d

def cdf_dict(data):
	ret = {}
	for i,k in enumerate(data.keys()):
		s = 0
		for j,k2 in enumerate(data.keys()):
			if j<=i:
				s+=data[k2]
		ret[k] = s
	return ret

for i in range(2,13):	
	SLZ = get_data("results/SLZ_n25_d4/s" + str(i))
	zigzag = get_data("results/zigzag_n25_d4/s" + str(i))
	#zigzag = {k: v for k, v in sorted(zigzag.items(), key=lambda item: item[0])}
	jellyfish = get_data("results/jellyfish_n25_d4/s" + str(i) )

	SLZ_cdf = cdf_dict(SLZ)
	jellyfish_cdf = cdf_dict(jellyfish)
	zigzag_cdf = cdf_dict(zigzag)
	vals1 = {}
	for k in SLZ_cdf:
		vals1[k] = SLZ_cdf[k] - jellyfish_cdf[k] if k in jellyfish_cdf else 0
	vals2 = {}
	for k in zigzag_cdf:
		vals2[k] = zigzag_cdf[k] - jellyfish_cdf[k] if k in jellyfish_cdf else 0
	#jellyfish = {k: v for k, v in sorted(jellyfish.items(), key=lambda item: item[0])}
	plt.plot(jellyfish.keys(), jellyfish.values(), label="jellyfish")
	plt.plot(SLZ.keys(), SLZ.values(), label="SLZ")
	plt.plot(zigzag.keys(), zigzag.values(), label="zigzag")
	plt.legend()
	plt.savefig("graphs/res" + str(i))
	plt.close()
