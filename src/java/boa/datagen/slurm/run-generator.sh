#!/bin/bash
#SBATCH --nodes=1
#SBATCH --cpus-per-task=8
#SBATCH --mem=2G
#SBATCH --time=30-02:30:02

# ----- optional
#SBATCH --output=job.%J.out
#SBATCH --error=job.%J.err
#SBATCH --job-name="run-gen"

# ----- load module 
module load jdk

JARFILE="./seq-repo-generator.jar"
RAM="-Xmx1G"

# local test
# SLURM_JOB_TEMPLATE_PATH="slurmJob.sh"
# JSON_INPUT_PATH="/Users/hyj/git/BoaData/DataGenInputJson"
# REPO_INPUT_PATH="/Users/hyj/git/BoaData/DataGenInputRepo"
# SPLIT_JSON_PATH="split"
# DATAGEN_JAR_PATH="seq-repo-builder.jar"
# OUTPUT_PATH="/Users/hyj/git/BoaData/DataSet/p3test"
# FILE_NUM_PER_JOB="1"

# remote 
SLURM_JOB_TEMPLATE_PATH="slurmJob.sh"
JSON_INPUT_PATH="/work/LAS/hridesh-lab/longvu/2020_java_dataset/2020_java_json_sized"
REPO_INPUT_PATH="/work/LAS/hridesh-lab/longvu/2020_java_dataset/input_repo_java"
SPLIT_JSON_PATH="split"
DATAGEN_JAR_PATH="seq-repo-builder.jar"
OUTPUT_PATH="/work/LAS/hridesh-lab/yijia/p3datagen/dataset"
FILE_NUM_PER_JOB="10"

CMD="java ${RAM} -Xss64M -jar \
${JARFILE} \
${SLURM_JOB_TEMPLATE_PATH} \
${JSON_INPUT_PATH} \
${REPO_INPUT_PATH} \
${SPLIT_JSON_PATH} \
${DATAGEN_JAR_PATH} \
${OUTPUT_PATH} \
${FILE_NUM_PER_JOB}"

echo "Execute: ${CMD}\n"
${CMD}