matchings_file=/cs/usr/inonkp/netbench/xpander_generation/opera/testing/matchings/$1
N=$2
export N matchings_file
logs_folder=/cs/labs/schapiram/inonkp/opera/n333/logs
sbatch -D './' --time=20-0 -c10 --mem=10g -o ${logs_folder}/matchings$1.stdout -e ${logs_folder}/matchings$1.stderr create_matchings.sh
