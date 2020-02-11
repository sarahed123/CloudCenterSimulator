import sys
import random


def split_matchings(matchings):
    for i in range(0, len(matchings), matchings_per_rotor):
        yield matchings[i:i + matchings_per_rotor]  


matching_file_path = sys.argv[1]
rotor_num = int(sys.argv[2])
rotor_output_dir = sys.argv[3]

matchings = []
with open(matching_file_path, "r") as f:
    lines = f.readlines()
    for line in lines:
        line = line.strip()
        if not line:
            break #last line might be blank
        matchings.append(line)

#random.shuffle(matchings)
matchings_per_rotor = (len(matchings)) // rotor_num
for j,rotor_matchings in enumerate(split_matchings(matchings)):
    with open(rotor_output_dir + "/" + "rotor_" + str(j), "w") as f:
        f.write("\n".join(rotor_matchings))
