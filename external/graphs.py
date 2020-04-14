import argparse
import matplotlib.pyplot as plt

def get_labels():
    labels = dict()
    with open("/cs/labs/schapiram/inonkp/smallgraphs/labels.properties") as f:
        lines = f.readlines()
        for line in lines:
            line_split = line.split("=")
            labels[line_split[0]] = line_split[1]
    return labels

def init_label(data,label):
    if label in data:
        return
    data[label] = dict()
    data[label]['x'] = dict()

def parse_lable(label):
    return label.split("_")[0]

def graph(run_list):

    labels = get_labels()
    parser = argparse.ArgumentParser()
    parser.add_argument("--label", help="where to draw the label from", dest="label", required=True)
    parser.add_argument("--x_data", help="where to draw x data from", dest="x_data", required=True)
    parser.add_argument("--y_data", help="where to draw y data from", dest="y_data", required=True)
    args = parser.parse_known_args()[0]
    data = dict()
    for res in run_list:
        if args.label in res:
            init_label(data, res[args.label])
            x = res[args.x_data]
            try:
                x = float(x)
            except:
                pass
            data[res[args.label]]['x'][x] = res[args.y_data]
    for label in data:
        x_data = []
        y_data = []
        for x in sorted(data[label]['x'].keys()):
            x_data.append(x)
            y_data.append(float(data[label]['x'][x]))

        plt.plot(x_data,y_data,label=labels[parse_lable(label)])
    plt.legend()
    plt.show()




