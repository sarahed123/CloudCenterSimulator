import matplotlib.pyplot as plt

# num threads
threads = [1, 2, 4, 8, 16]

# run time of the threads
run_times = [50.346, 12.128, 0.687, 0.038, 0.044]  

#create graph
plt.figure(figsize=(8, 6))
plt.plot(threads, run_times, marker='o', linestyle='-', color='b', label='Simulation Run Time')


plt.xlabel('Number of Threads')
plt.ylabel('Simulation Run Time (s)')
plt.title('Simulation Run Time vs Number of Threads')
plt.xticks(threads)  

plt.grid(True)
plt.legend()

plt.savefig('./graph/Graph1-simulation_runtime_vs_threads.png')
