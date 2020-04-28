import sys
import os
import shutil

print("Usage: python gen_run_dir.py N d")
N = sys.argv[1]
d = sys.argv[2]
run_dir = "/cs/labs/schapiram/inonkp/small_graphs/runs/" + f"n{N}/d{d}" + "/netbench/"


os.makedirs(run_dir, exist_ok=True)
os.makedirs(run_dir + "/runs", exist_ok=True)
os.makedirs(run_dir + "/topologies", exist_ok=True)
os.makedirs(run_dir + "/logs", exist_ok=True)
try:
    os.symlink("/cs/usr/inonkp/netbench/NetBench.jar", run_dir + "/NetBench.jar")
except:
    pass
try:
    os.symlink("/cs/labs/schapiram/inonkp/smallgraphs/do_run.py", run_dir + "/do_run.py")

except:
    pass

try:
    os.symlink("/cs/usr/inonkp/netbench/external/run.sh", run_dir + "/run.sh")
except:
    pass


try:
    os.symlink("/cs/labs/schapiram/inonkp/smallgraphs/extract_netbench_results.py", run_dir + "/extract_results.py")
except:
    pass


try:
    os.symlink("/cs/usr/inonkp/netbench/external/import_topology.py", run_dir + "/import_topology.py")
except:
    pass


shutil.copy("/cs/usr/inonkp/netbench/external/defaults.properties", run_dir + "/defaults.properties")





