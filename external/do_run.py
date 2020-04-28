import argparse
import os
import sys
import json
import random

def log_run(description,log_path):
    with open(log_path + "/run.log", "w") as f:
        f.write(f"#{description}\n")
        f.write(' '.join(sys.argv) + "\n")

def run_sbatch(run,variable_properties,run_vars,seed):
    properties_as_string = run.get('properties')
    additional_params = run.get("additional_params")
    if additional_params:
        for param in additional_params:
            properties_as_string += " " + param
    for prop in run_vars:
        properties_as_string += f" {prop}={run_vars[prop]['value']}"
    run_sub_dirs = ""
    for run_node in variable_properties:
        run_sub_dirs+=f"/{run_node['property']}/{run_vars[run_node['property']]['label']}"
    properties_as_string+= f" common_base_dir={run.get('run_path')}/{run_sub_dirs} common_run_name={seed}.path"
    log_path = f"{run.get('log_path')}/{run_sub_dirs}"
    os.makedirs(log_path, exist_ok=True)
    # log_run(run.get("description"),log_path)
    log_path+=f"/{seed}.stdout"
    print(log_path)
    mem=100
    cores=10
    properties_as_string+= f" seed={seed}"
    command = f'sbatch -c{cores} --mem={mem}g --time=20-0 -o {log_path} -e {log_path} run.sh "{properties_as_string}"'
    # print(command)
    stream = os.popen(command)
    output = stream.read()
    print(output)

def run_all_sbatch(run,variable_properties,extra_vars,i):
    if i== len(variable_properties):
        for seed in run['seeds']:
            run_sbatch(run,variable_properties,extra_vars, seed)
        return
    for property_node in variable_properties[i]['nodes']:
        extra_vars_copy = extra_vars.copy()
        extra_vars_copy[variable_properties[i]["property"]] = property_node
        if 'dependencies' in property_node:
            for dep in property_node['dependencies']:
                extra_vars_copy[dep] = property_node['dependencies'][dep]
        run_all_sbatch(run,variable_properties,extra_vars_copy,i+1)

def generate_seeds(seed, num_seeds):
    random.seed(seed)
    seeds = []
    for i in range(num_seeds):
        seeds.append(random.randint(1, 100000000000))
    return seeds

full_parser = argparse.ArgumentParser()
full_parser.add_argument("--run_json", help="the run json file", dest="run_json", required=True)
full_parser.add_argument("--properties", help="the propeties file", required=False, default="defaults.properties")
full_parser.add_argument("--log_path", help="the logs path", default="logs")
full_parser.add_argument("--run_path", help="Where to store the run under?", default="runs")
full_parser.add_argument("--additional_params", help="additional run params", nargs="+", required=False)
full_parser.add_argument("--num_seeds", help="number of seeds", type=int, required=False, default=1)
run_args = full_parser.parse_args()
# loop_vars = vars(loop_parser.parse_known_args()[0])
# for property in loop_vars.get("run_over"):
#     full_parser.add_argument("--" + property, nargs="+")
print(run_args.run_json)
with open(run_args.run_json) as f:
    run = (json.load(f))

# variable_properties = loop_vars.get("run_over")
run['properties'] = run_args.properties
run['log_path'] = run_args.log_path
run['run_path'] = run_args.run_path
run['additional_params'] = run_args.additional_params
run['seeds'] = generate_seeds(run['seed'], run_args.num_seeds)

run_all_sbatch(run,run["run_over"],{},0)
