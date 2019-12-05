rotors_dir=$1
output_dir=$2
rotor_num=$3
N=$4
export i N rotors_dir output_dir rotor_num
C=$((N-2))
logs_folder=/cs/labs/schapiram/inonkp/opera/n333/logs
for i in $(seq 0 $C); do 
	sbatch -D './' --time=20-0 -c10 --mem=10g -o ${logs_folder}/$i.stdout -e ${logs_folder}/$i.stderr run_cycle.sh
done
