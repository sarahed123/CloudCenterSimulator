import random
import copy
import sys
import os

def super_shuffle(lst):
    new_lst = copy.copy(lst)
    random.shuffle(new_lst)
    for old, new in zip(lst, new_lst):
        if old == new:
            return super_shuffle(lst)

    return new_lst

def main(outname, n,perm_number):
    os.makedirs(outname,exist_ok=True)
    s=list(range(n))
    for j in range(30):
        with open(outname + "/props" + str(j) + ".properties" , 'w') as f:
            f.write("run_folder_name=run_props" + str(j) +"\n")
            f.write("traffic_pairs=")
            for k in range(perm_number):
                s2 = super_shuffle(s)
               
                for i in range(len(s)):
                    f.write("%d->%d"%(s[i],s2[i]))
                    if i<len(s)-1 or k<perm_number-1:
                           f.write(",") 
    
if __name__ == "__main__":
    args = sys.argv[1:]
    if len(args) != 3:
        print("Usage: fat_tree_traffic.py <outdir> <set_size> <premutation number>")
    else:
        main(args[0],int(args[1]),int(args[2]))
    
    
