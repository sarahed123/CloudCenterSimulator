rotors_dir=$1
output_dir=$2
rotor_num=$3
N=$4
K=$5
export i N rotors_dir output_dir rotor_num K
C=$((N-1))
logs_folder=/cs/labs/schapiram/inonkp/opera/n333/logs
for i in $(seq 0 $C); do 
	sh run_k_shortest_cycle.sh
done
