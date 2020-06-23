#!/bin/bash
#SBATCH --nodes=1
#SBATCH --cpus-per-task=16
#SBATCH --mem=101G
#SBATCH --time=30-02:30:02

# ----- optional
#SBATCH --output=job.%J.out
#SBATCH --error=job.%J.err
#SBATCH --job-name="combine"
#SBATCH --partition=speedy

# ----- load module 
module load jdk

JARFILE="./seq-repo-combiner.jar"
RAM="-Xmx100G"

# local test
# OUTPUT_PATH="/Users/hyj/git/BoaData/DataSet/p3test"

# remote
OUTPUT_PATH="/work/LAS/hridesh-lab/yijia/p3datagen/dataset"

CMD="java ${RAM} -Xss64M -jar \
${JARFILE} \
${OUTPUT_PATH}"

echo "Execute: ${CMD}\n"
${CMD}