class PathDB:

    def __init__(self, db_folder, N):
        self.N = N
        self.db = self.read_db(db_folder)
        
    def get_paths(self, source, target):
        try:
            return self.db[source][target]
        except KeyError:
            return []

    def get_random_path(self, source, target, chooser):
        return chooser.choice(self.db[source][target])

    def get_random_paths(self, source, target, num_paths, chooser):
        indices =  chooser.choice(len(self.db[source][target]), num_paths)
        return [self.db[source][target][i] for i in indices]

    def read_db(self, db_folder):
        db = {}
        for n in range(self.N):
            print(f"reading node {n} db")
            db[n] = self.read_node_paths(n,db_folder + "/" + str(n))
        return db

    def read_node_paths(self, n, node_db_file):
        node_db = {}
        with open(node_db_file, "r") as f:
            for line in f:
                path = list(map(int,line.strip().split(",")))
                target = path[-1]
                if not node_db.get(target, False):
                    node_db[target] = []
                node_db[target].append(path)
        return node_db

    def get_intersection_by_distance(self, v, u, du, dv):
        nodes = list()
        for t in self.db[v]:
            if u == t:
                continue
            if t not in self.db[v] or t not in self.db[u]:
                continue
            if len(self.db[v][t][0]) <= dv and len(self.db[u][t][0]) <= du:
                nodes.append(t)
        return nodes