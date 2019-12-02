class Rotor:
    def __init__(self, matchings):
        self.curr_cycle = 0
        self.matchings = matchings

    def advance(self):
        self.curr_cycle = (self.curr_cycle+1) % len(self.matchings)

    def get_current_matching(self):
        return self.matchings[self.curr_cycle]

    def __repr__(self):
        return str(self.matchings[self.curr_cycle])

    def in_rotor(self, n):
        return n in self.get_current_matching()

def load_rotors(rotors_dir,rotor_num):
    rotors = []
    for r in range(rotor_num):
        with open(rotors_dir + "/rotor_" + str(r)) as f:
            lines = f.readlines()
            matchings = []
            for line in lines:
                matching = line.split(" ")
                matching = list(map(lambda x: int(x), matching))
                matchings.append(matching)
            rotors.append(Rotor(matchings))   
    return rotors 

def rotor_neighbours(i,j,rotors):
    for rotor in rotors:
        if j==rotor.get_current_matching()[i]:
            return True
    return False

def get_rotor_neighbours(k,rotors):
    neighbours = []
    for j,rotor in enumerate(rotors):
        neighbours.append((j,rotor.get_current_matching()[k]))
    return neighbours

def advance_to_cycle(cycle_num, rotors):
    curr_rotor_index = 0
    cycle = 0