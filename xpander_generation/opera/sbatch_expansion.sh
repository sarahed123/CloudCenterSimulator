rotors_dir=$1
rotor_num=$2
N=$3
export i N rotors_dir output_dir rotor_num
C=$((N-1))
logs_folder=/cs/labs/schapiram/inonkp/opera/n333/logs
for i in $(seq 0 $C); do 
	sbatch -D './' --time=20-0 -c10 --mem=10g -o ${logs_folder}/$i.stdout -e ${logs_folder}/$i.stderr verify_expansion.sh
	
	sbatch -D './' --time=20-0 -c10 --mem=10g -o ${logs_folder}/$i.stdout -e ${logs_folder}/$i.stderr verify_connected.sh
done
