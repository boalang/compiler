# Boa Dataset Generation and Run Boa Queries Locally
This tutorial will describe how to create your own Boa dataset for specific projects in Github and run Boa queries on 
that dataset locally. We will use command line and Eclipse IDE for that purpose.

## Prerequisite
You need to have following already installed in your system:
1. JDK
2. Apache Ant
3. Git
4. Eclipse IDE

## Development Setup Steps
1. Clone the Boa project using the command line: $ git clone https://github.com/boalang/compiler.git 
2. Go to the cloned repository: $ cd compiler
3. Clean the project: $ ant clean
4. Create a directory for libraries: $ mkdir -p build/classes
5. Compile the project: $ ant compile
6. Create a class folder: $ mkdir compile
7.	In Eclipse go to: File > Open Projects from File System > Import Source – Directory > Browse the cloned repository (compiler) > Hit Finish

