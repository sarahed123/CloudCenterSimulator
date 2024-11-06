import matplotlib.pyplot as plt

#run time of threads 1,2,4,8,16 for each different flows
data = {
    '10 FLOWS': [0.5, 0.4, 0.35, 0.3, 0.25],  
    '100 FLOWS': [2.5, 2.0, 1.8, 1.5, 1.2],  
    '1000 FLOWS': [25.0, 20.0, 18.0, 15.0, 12.0],
    '10000 FLOWS': [25.0, 20.0, 18.0, 15.0, 12.0]   
}

# num threads
threads = [1, 2, 4, 8, 16]

# create graph
plt.figure(figsize=(10, 6))

for flows, runtimes in data.items():
    plt.plot(threads, runtimes, marker='o', label=flows)

plt.xscale('log', base=2)  
plt.xticks(threads) 
plt.xlabel('Number of Threads')
plt.ylabel('Runtime (seconds)')
plt.title('Runtime vs Number of Threads for Different Flows')
plt.legend(title='Number of Flows')
plt.grid(True)
plt.tight_layout()

plt.savefig('Graph2 - simulation_runtime_vs_flows.png')
