import sys
import os

run_dir = "/cs/labs/schapiram/inonkp/small_graphs/runs/" + sys.argv[1]


os.makedirs(run_dir, exist_ok=True)
os.makedirs(run_dir + "/runs", exist_ok=True)
os.makedirs(run_dir + "/topologies", exist_ok=True)
os.makedirs(run_dir + "/logs", exist_ok=True)

os.symlink("/cs/labs/schapiram/inonkp/small_graphs/FS.jar", run_dir + "/FS.jar")
os.symlink("/cs/labs/schapiram/inonkp/small_graphs/do_run.py", run_dir + "/do_run.py")
os.symlink("/cs/labs/schapiram/inonkp/small_graphs/run.sh", run_dir + "/run.sh")
os.symlink("/cs/labs/schapiram/inonkp/small_graphs/extract_results.py.py", run_dir + "/extract_results.py.py")