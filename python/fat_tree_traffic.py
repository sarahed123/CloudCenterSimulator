import random
import sys

def main(outname, n,servers):
    if n%(2) != 0:
        print("set size must be even")
        sys.exit(0)
    setSize = n/2
    s=list(range(n))
    #random.shuffle(s) # << shuffle before print or assignment
    s1 = s[0:int(setSize)]
    s2 = s[int(setSize):n]
    
    
    with open(outname, 'w') as f:
        for j in range(servers):
            for i in range(int(setSize)):
                f.write("%d->%d"%(s1[i],s2[int((i+j)%setSize)]))
                
                if not (j==servers-1 and i==n/2-1):
                   f.write(",") 
    
if __name__ == "__main__":
    args = sys.argv[1:]
    if len(args) != 3:
        print("Usage: fat_tree_traffic.py <outname> <set_size> <servers_per_node>")
    else:
        main(args[0],int(args[1]),int(args[2]))