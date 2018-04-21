import json
import copy
import sys
import os


def DFS(folder,dirName ='',variants ={}):
    contents = os.listdir(folder)
    if('common_drop_statistics.csv.log' in contents):
        dirs = os.path.normpath(folder).split(os.sep)
        newFile = {}
        newFile['file_name'] = os.path.join(folder, 'common_drop_statistics.csv.log')
        newFile['secondaries'] = []
        for i in range(len(dirs)):
            if(dirs[i] in secondaries):
                newFile['secondaries'].append(dirs[i+1])
            if (dirs[i]==main_variant):
                newFile['main_variant'] = dirs[i+1]
        jsonOut['files'].append(newFile)
        return
    for f in contents:
        filePath = os.path.join(folder,f)
        if(os.path.isdir(filePath)):
            DFS(filePath,f)



if __name__ == '__main__':
    args = sys.argv

    if(len(args)<2):
        print("Usage: <infolder> <outfile> <main_variant> <secondaries>")
    infolder = args[1]
    outfile = args[2]
    main_variant = args[3]
    secondaries = args[4:]
    jsonOut = {}
    jsonOut['files'] = []

    DFS(infolder)

    with open(outfile,'w') as file:
        json.dump(jsonOut,file,indent=4)
