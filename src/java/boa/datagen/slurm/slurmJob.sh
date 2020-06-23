#!/bin/bash
#SBATCH --nodes=1
#SBATCH --cpus-per-task=8
#SBATCH --mem=25G
#SBATCH --time=30-02:30:02

# ----- optional
#SBATCH --output=job.%J.out
#SBATCH --error=job.%J.err
#SBATCH --job-name="datagen"

# ----- load module 
module load jdk

# ----- main
# $1: datagen jar path
# $2: bare repo path
# $3: json files in a list
# $4: output path
JARFILE="${1}"
REPO="${2}"
JSON_FILES="${3}"
OUTPUT="${4}"
RAM="-Xmx24G"

# ----- run\
CMD="java ${RAM} -Xss64M -jar \
${JARFILE} \
${REPO} \
${JSON_FILES} \
${OUTPUT}"

echo "Execute: ${CMD}"
${CMD}