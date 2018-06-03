import numpy as np
import sys

def add_lines(s,l):
  return s+l+"\n"

def gen_fat_tree(d):
  assert np.mod(d,2) == 0

  num_nodes = 5*(d**2)//4
  num_tors = (d**2)//2
  num_aggs = (d**2)//2
  num_cores = (d**2)//4

  res_string = add_lines("", "# Fat-tree of k=%d\n"%d)
  res_string = add_lines(res_string, "#START PARAMS")
  res_string = add_lines(res_string, "#N=%d"%num_nodes)
  res_string = add_lines(res_string, "#END PARAMS")

  #res_string = add_lines(res_string,"")
  #res_string = add_lines(res_string,"START NODES")
  #for i in range(num_tors):
  #  res_string = add_lines(res_string,"%d 1"%i)
  #for i in range(num_tors, num_nodes):
  #  res_string = add_lines(res_string,"%d 0"%i)
  #res_string = add_lines(res_string,"END NODES")
  #res_string = add_lines(res_string,"")

  #res_string = add_lines(res_string,"START LINKS")

  print("connect tors to aggregators (pod by pod)")
  for pod in range(d):
    for aggi in range(d//2):
      for tori in range(d//2):
        tor = pod*d/2 + tori
        agg = num_tors + pod*d//2+aggi
        res_string = add_lines(res_string,"%d %d"%(tor,agg))
        res_string = add_lines(res_string,"%d %d"%(agg,tor))
    print("done pod " + str(pod))

  print("connect cores to aggregators (pod by pod)")
  for corei in range(num_cores):
     for pod in range(d):
           aggi=np.mod(corei,d//2);
           if agg==0:
               aggi = d//2;
           core = num_tors+num_aggs+corei
           agg = num_tors+pod*d/2+aggi
           res_string = add_lines(res_string,"%d %d"%(core,agg))
           res_string = add_lines(res_string,"%d %d"%(agg,core))
     print("done core " + str(corei))
           
  #res_string = add_lines(res_string,"END LINKS")

  with open("fat_tree_k%d.topology"%d, 'w') as f:
      f.write(res_string)

if __name__ == "__main__":
  args = sys.argv[1:]
  if len(args) != 1:
    print("Usage: fatgen.py degree")
  else:
    gen_fat_tree(int(args[0]))