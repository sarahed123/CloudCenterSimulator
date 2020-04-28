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

def graph(run_list,args,fix,i,fig):
    labels = get_labels()

    data = dict()
    for res in run_list:
        if not all(item in res.items() for item in fix.items()):
            continue
        if args.label in res:
            init_label(data, res[args.label])
            x = res[args.x_data]
            try:
                x = float(x)
            except:
                pass
            data[res[args.label]]['x'][x] = res[args.y_data]

    ax1 = fig.add_subplot(2,2,i+1)
    for label in data:
        x_data = []
        y_data = []
        for x in sorted(data[label]['x'].keys()):
            x_data.append(x)
            y_data.append(float(data[label]['x'][x]))
        ax1.plot(x_data,y_data,label=label)
    title = ""
    for k in fix:
        title+= f"{fix[k]} "
    title = title.strip()
    ax1.set_title(title)
    ax1.legend()
    #plt.show()
    #plt.savefig(args.output_dir + "/" + title.replace(" ", "_") + ".png")

    return plt


