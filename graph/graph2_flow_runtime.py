# import matplotlib.pyplot as plt

# #run time of threads 1,2,4,8,16 for each different flows
# data = {
#     '10 FLOWS': [2.062, 1.245, 0.118, 0.051, 0.289],  
#     '100 FLOWS': [18.284, 3.008, 0.561, 0.119, 0.139],  
#     '1000 FLOWS': [212.212, 16.459, 3.706, 1.007, 0.972],   
# }

# # num threads
# threads = [1, 2, 4, 8, 16]


import matplotlib.pyplot as plt
from matplotlib.ticker import MaxNLocator

# run time of threads 1,2,4,8,16 for each different flows
data = {
    '10 FLOWS': [2.062, 1.245, 0.118, 0.051, 0.289],  
    '100 FLOWS': [18.284, 3.008, 0.561, 0.119, 0.139],  
    '1000 FLOWS': [212.212, 16.459, 3.706, 1.007, 0.972],   
}

# num threads
threads = [1, 2, 4, 8, 16]

# create graph - logarithmic
plt.figure(figsize=(10, 6))

for flows, runtimes in data.items():
    plt.plot(threads, runtimes, marker='o', label=flows)

plt.xscale('linear')
plt.yscale('log')  
plt.xticks(threads)

plt.xlabel('Number of Threads')
plt.ylabel('Runtime (seconds)')
plt.title('Runtime vs Number of Threads for Different Flows')
plt.legend(title='Number of Flows')
plt.grid(True, which="both", linestyle='--', linewidth=0.5)  
plt.tight_layout()

plt.savefig('./graph/Graph2-logarithmic-simulation_runtime_vs_flows.png')


# create graph - linear
plt.figure(figsize=(10, 6))

for flows, runtimes in data.items():
    plt.plot(threads, runtimes, marker='o', label=flows)

plt.xscale('linear') 
plt.xticks(threads) 


plt.xlabel('Number of Threads')
plt.ylabel('Runtime (seconds)')
plt.title('Runtime vs Number of Threads for Different Flows')
plt.legend(title='Number of Flows')
plt.grid(True)
plt.tight_layout()

plt.savefig('./graph/Graph2-liear-simulation_runtime_vs_flows.png')