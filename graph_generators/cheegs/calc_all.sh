graph=$1
N=$2
log=$(basename $graph)
log="${log%.*}"
cores=10
mem=100
mkdir results/$log
for (( i=2; i <= $N/2; ++i ))
do
	log_file=${log}/s$i
	sbatch -c${cores} --mem=${mem}g --time=20-0 -o results/$log_file -e results/$log_file calc.sh $graph $N $i    
done
