import random
import copy
import sys

def super_shuffle(lst):
    new_lst = copy.copy(lst)
    random.shuffle(new_lst)
    for old, new in zip(lst, new_lst):
        if old == new:
            return super_shuffle(lst)

    return new_lst

def main(outname, n,servers):

    s=list(range(n))
    #random.shuffle(s) # << shuffle before print or assignment
    s2 = super_shuffle(s);
    
    
    with open(outname, 'w') as f:
        for i in range(len(s)):
            f.write("%d->%d"%(s[i],s2[i]))
            if i<len(s)-1:
                   f.write(",") 
    
if __name__ == "__main__":
    args = sys.argv[1:]
    if len(args) != 3:
        print("Usage: fat_tree_traffic.py <outname> <set_size> <servers_per_node>")
    else:
        main(args[0],int(args[1]),int(args[2]))
    
    
