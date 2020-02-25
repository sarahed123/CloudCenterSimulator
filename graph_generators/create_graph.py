import sys
import os

def createGraphFromFile(file):
	verticesSet = set()
	edges = {}
	f= open(file,'r')
	line= f.readline()
	while(line):
		if line.startswith("#"):
			line = f.readline()
			continue
		vertices = line.split(" ")
		left = int(vertices[0])
		right = int(vertices[1].rstrip())
		verticesSet.add(left)
		verticesSet.add(right)
		edges[left] = edges.get(left,set())
		edges[left].add(right)
		line = f.readline()
	return verticesSet, edges
