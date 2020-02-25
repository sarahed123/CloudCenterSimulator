import sys
from create_graph import *

vertices, edges = createGraphFromFile(sys.argv[1])

for v in vertices:
    for u in edges[v]:
        if not v in edges[u]:
            raise Exception("%d %d",u,v)
