import argparse
import matplotlib.pyplot as plt
import os
import sys
from graphs import graph
import json
import random
from matplotlib.backends.backend_pdf import PdfPages

def extract(run_dir, requested, run_dict):
    print(run_dir)
    for filename in os.listdir(run_dir):
        full_path = os.path.join(run_dir, filename)
        with open(full_path,"r") as f:
            statistics = f.readlines()
            for stat in statistics:
                stat_split = stat.split("=")
                if stat_split[0] in requested:
                    print(" ".join(stat_split))
                    run_dict[stat_split[0]] = float(stat_split[1])


def get_run_dir(variable_properties,run_vars,seed):
    run_dict = dict()
    run_sub_dirs = "runs"
    for property in run_vars:
        run_dict[property] = run_vars[property]
        run_sub_dirs+=f"/{property}/{run_vars[property]}"
    with open(run_sub_dirs + f"/{seed}.path") as f:
        print(seed)
        return f.readlines()[-1].strip() , run_dict


def extract_results(full_vars,variable_properties,extra_vars,requested,i):
    if i == len(variable_properties):
        for seed in seeds:
            run_dir, run_dict = get_run_dir(variable_properties,extra_vars,seed)
            for var in extra_vars:
                print(extra_vars.get(var) + " ")
            extract(run_dir + "/results/analysis/",requested, run_dict)
        return [run_dict]

    run_list = []
    for node in (variable_properties[i]['nodes']):
        extra_vars[variable_properties[i]['property']] = node['label']
        run_list = extract_results(full_vars,variable_properties,extra_vars,requested,i+1) + run_list

    return  run_list


def generate_seeds(seed, num_seeds):
    random.seed(seed)
    seeds = []
    for i in range(num_seeds):
        seeds.append(random.randint(1, 100000000000))
    return seeds


def main():

    variable_properties = []
    for run_node in run['run_over']:
        variable_properties.append(run_node)
    return extract_results(full_vars,variable_properties,{},full_vars.requested,0)



if __name__=="__main__":
    full_parser = argparse.ArgumentParser()
    full_parser.add_argument("--extract", nargs="+", dest="requested")
    full_parser.add_argument("--graph", help="Make graph", action='store_true' ,dest="make_graph")
    full_parser.add_argument("--num_seeds", help="number of seeds", type=int, required=False, default=1)

    # loop_parser = argparse.ArgumentParser()
    full_parser.add_argument("--run_json", help="the run json file", dest="run_json", required=True)
    full_parser.add_argument("--label", help="where to draw the label from", dest="label", required='--graph' in sys.argv)
    full_parser.add_argument("--x_data", help="where to draw x data from", dest="x_data", required='--graph' in sys.argv)
    full_parser.add_argument("--y_data", help="where to draw y data from", dest="y_data", required='--graph' in sys.argv)
    full_parser.add_argument("--output_dir", help="where to save all the graphs", dest="output_dir", required='--graph' in sys.argv)
    # for property in loop_vars.get("run_over"):
    #     full_parser.add_argument("--" + property, nargs="+")
    full_vars = full_parser.parse_args()
    with open(full_vars.run_json) as f:
        run = (json.load(f))
    seeds = generate_seeds(run['seed'], full_vars.num_seeds)
    run_list = main()
    if full_vars.make_graph:
        fix = []
        for d in run_list:
            f = d.copy()
            del f[full_vars.x_data]
            del f[full_vars.label]
            for prop in full_vars.requested:
                del f[prop]
            fix.append(f)
        fix = [dict(t) for t in set(tuple(sorted(d.items())) for d in fix)]
        with PdfPages("graphs/netbench.pdf") as pdf:
            for i,f in enumerate(fix):
                if i%4==0:
                     fig = plt.figure(figsize=(12,10))
                graph(run_list,full_vars,f,i%4,fig)
                if (i+1)%4==0:
                    pdf.savefig(fig)
                    plt.close()   
