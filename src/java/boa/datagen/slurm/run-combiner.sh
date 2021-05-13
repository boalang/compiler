#!/bin/bash
#SBATCH --nodes=1
#SBATCH --cpus-per-task=24
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
RAM="-Xmx100G" # need to change accordingly

# local test
# OUTPUT_PATH="/Users/hyj/git/BoaData/DataSet/p3test"
# PROJECT_NUM_IN_AST="1"

# remote
OUTPUT_PATH="/work/LAS/hridesh-lab/yijia/p3datagen/dataset_new"
PROJECT_NUM_IN_AST="10000"

# main
CMD="java ${RAM} -Xss64M -jar \
${JARFILE} \
${OUTPUT_PATH} \
${PROJECT_NUM_IN_AST}"

echo "Execute: ${CMD}\n"
${CMD}