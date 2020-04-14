import argparse
import os
import sys

def log_run(description,log_path):
    with open(log_path + "/run.txt", "w") as f:
        f.write(f"#{description}\n")
        f.write(' '.join(sys.argv) + "\n")

def run_sbatch(full_vars,variable_properties,run_vars):
    properties_as_string = full_vars.get('properties')
    additional_params = full_vars.get("additional_params")
    if additional_params:
        for param in additional_params:
            properties_as_string += " " + param
    for prop in run_vars:
        properties_as_string += f" {prop}={run_vars[prop]}"
    run_sub_dirs = ""
    for property in variable_properties:
        run_sub_dirs+=f"/{property}/{run_vars[property]}"
    properties_as_string+= f" run_path={full_vars.get('run_path')}/{run_sub_dirs}"
    log_path = f"{full_vars.get('log_path')}/{run_sub_dirs}"
    os.makedirs(log_path, exist_ok=True)
    log_run(full_vars.get("description"),log_path)
    log_path+="/run.stdout"
    print(log_path)
    command = f'sbatch -o {log_path} -e {log_path} run.sh "{properties_as_string}"'
    stream = os.popen(command)
    output = stream.read()
    print(output)

def run_all_sbatch(full_vars,variable_properties,extra_vars,i):
    if i== len(variable_properties):
        run_sbatch(full_vars,variable_properties,extra_vars)
        return
    for property in full_vars.get(variable_properties[i]):
        extra_vars[variable_properties[i]] = property
        run_all_sbatch(full_vars,variable_properties,extra_vars,i+1)

loop_parser = argparse.ArgumentParser()
loop_parser.add_argument("--run_over", nargs="+", dest="run_over")


full_parser = argparse.ArgumentParser()
full_parser.add_argument("--properties", help="the propeties file", required=True)
full_parser.add_argument("--log_path", help="the logs path", default="logs")
full_parser.add_argument("--description", help="what is this run for?", required=True)
full_parser.add_argument("--run_path", help="Where to store the run under?", default="runs")
full_parser.add_argument("--additional_params", help="additional run params", nargs="+", required=False)
loop_vars = vars(loop_parser.parse_known_args()[0])
for property in loop_vars.get("run_over"):
    full_parser.add_argument("--" + property, nargs="+")


full_vars = vars(full_parser.parse_known_args()[0])
variable_properties = loop_vars.get("run_over")

run_all_sbatch(full_vars,variable_properties,{},0)
