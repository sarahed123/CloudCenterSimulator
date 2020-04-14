import argparse
import os

from graphs import graph


def extract(run_dir, requested, run_dict):
    for filename in os.listdir(run_dir):
        full_path = os.path.join(run_dir, filename)
        with open(full_path,"r") as f:
            statistics = f.readlines()
            for stat in statistics:
                stat_split = stat.split("=")
                if stat_split[0] in requested:
                    print(" ".join(stat_split))
                    run_dict[stat_split[0]] = float(stat_split[1])


def get_run_dir(variable_properties,run_vars):
    run_dict = dict()
    run_sub_dirs = "runs"
    for property in variable_properties:
        run_dict[property] = run_vars[property]
        run_sub_dirs+=f"/{property}/{run_vars[property]}"
    with open(run_sub_dirs + "/run.txt") as f:
        return f.readline() , run_dict


def extract_results(full_vars,variable_properties,extra_vars,requested,i):
    if i == len(variable_properties):
        run_dir, run_dict = get_run_dir(variable_properties,extra_vars)
        for var in extra_vars:
            print(extra_vars.get(var) + " ")
        extract(run_dir + "/logs/analysis/",requested, run_dict)
        return [run_dict]

    run_list = []
    for property in full_vars.get(variable_properties[i]):
        extra_vars[variable_properties[i]] = property
        run_list = extract_results(full_vars,variable_properties,extra_vars,requested,i+1) + run_list

    return  run_list

def main(full_parser):

     variable_properties = loop_vars.get("run_over")
     return extract_results(full_vars,variable_properties,{},full_vars.get("requested"),0)

if __name__=="__main__":
    full_parser = argparse.ArgumentParser()
    full_parser.add_argument("--properties", nargs="+", dest="requested")
    full_parser.add_argument("--graph", help="Make graph", type=bool, default=False, dest="make_graph")
    loop_parser = argparse.ArgumentParser()
    loop_parser.add_argument("--run_over", nargs="+", dest="run_over")
    loop_vars = vars(loop_parser.parse_known_args()[0])
    for property in loop_vars.get("run_over"):
        full_parser.add_argument("--" + property, nargs="+")
    full_vars = vars(full_parser.parse_known_args()[0])

    run_list = main(full_parser)
    if full_vars.get("make_graph"):
        graph(run_list)


