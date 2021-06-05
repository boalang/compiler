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
1. Clone the Boa project using the command line: `git clone https://github.com/boalang/compiler.git`
2. Go to the cloned repository: `cd compiler`
3. Clean the project: `ant clean`
4. Create a directory for libraries: `mkdir -p build/classes`
5. Compile the project: `ant compile`
6. Create a class folder: `mkdir compile`
7.	In Eclipse go to: File > Open Projects from File System > Import Source – Directory > Browse the cloned repository (compiler) > Hit Finish

<p align="center"> 
<img src="img/import.png" title="Import compiler project in Eclipse">
</p>


8.	Right click on the project compiler > Build Path > Configure Build Path
9.	In Source tab, click Add Folder to add the required source folders and remove unnecessary folder(s). After adding all the folders, the window should look like the following:

<p align="center"> 
<img src="img/after_config.png" title="After configuring Eclipse">
</p>

10.	Select Libraries tab in the same window and click on Add Class Folder and add the compiler/compile folder that has been created in step 6. 
11.	Click Add JARs… in the same Libraries tab > select lib > select all the files inside lib, including files under datagen and evaluator folder > hit Apply and Close.
12.	From the compiler project in Eclipse, right click on build.xml > Run as > 1 Ant build. This should build the project successfully. The development setup is completed. Now, we will move on to data generation steps. 

## Boa Data Generation Steps
1.	Go to github.com and search the project for which you want to create the dataset. For example, if you want to create dataset for Apache Mavan project, go to https://github.com/apache/maven .
2.	Invoke a GitHub http-based RESTful API to get the metadata of the project by constructing a URL https://api.github.com/repos/repo_full_name, for example https://api.github.com/repos/apache/maven. 
3.	Copy the whole JSON metadata, create a blank text file, type a pair of brackets ‘[]’, paste the metadata inside the brackets.
4.	Search for "languages_url" field in the JSON data, go to the URL, copy everything, create another field in the JSON file ("language_list": ), paste copied text and save the file as filename.json (e.g., maven.json). The last few lines of the JSON file should look like:

<p align="center"> 
<img src="img/json_looklike.png" title="JSON file">
</p>

5.	In this way, for each project, that will be included in the dataset, create a JSON file. So, if you want to create a dataset for 5 projects, you will create 5 separate JSON files. Save all the JSON files in a folder. Alternatively, you can create a single JSON file for all of the projects by separating them by comma in an array ‘[]’. The format looks like [{}, {}]. Each curly brace is for one project. 
6.	In Eclipse, go to the project compiler > src/java > boa.datagen, right click on BoaGenerator.java > Run As > Run Configurations.
7.	Double click on Java Application to create a new configuration. Browse project and select compiler, Search Main Class and select boa.datagen.BoaGenerator.

<p align="center"> 
<img src="img/datagen.png" title="Data generator configuration">
</p>

8.	Select Arguments tab and add program arguments. The program arguments format should look like:
<pre><center>
  -inputJson 	&#60;directory containing project JSON files&#62;	
  -output 	&#60;output directory of the dataset (folder will be automatically created)&#62;	
  -inputRepo 	&#60;temporary directory used to clone the projects (this folder 
                   will be automatically created)&#62;	
 </center></pre>
 
 The other arguments are optional. For example, to print debug messages in console use -debug. 
 
<p align="center"> 
<img src="img/datagenparam.png" title="Data generator parameters">
</p>

9.	Hit Run.
10.	This should start cloning the projects form Github and generating dataset. Depending on the number of projects and size of the projects, this will take some time to finish. When the red Terminate option in the console goes off, the data generation process is finished. 

## Run Boa Query on New Dataset
1.	Create a dataset folder copying three files (projects.seq, ast/data, ast/index) from the generated output folder from step 8 of data generation process.
2.	In Eclipse, go to the project compiler > src/java > boa.evaluator, right click on BoaEvaluator.java > Run As > Run Configurations… 
3.	Create a new configuration by clicking the New Configuration in the upper left corner of the window. 
4.	Give a Name to the configuration, Browse project and select compiler, Search Main Class and select boa.evaluator.BoaEvaluator.
5.	Select Arguments tab and add program arguments. The program arguments format should look like:
<pre><center>
  -input 	&#60;file path to the boa source code file&#62;
  -data 	&#60;dataset directory containing three files(projects.seq, data, index)&#62;
  -output 	&#60;output directory&#62;
 </center></pre>
 
 <p align="center"> 
<img src="img/evalparam.png" title="Data evaluator parameters">
</p>

6. The output of the query will be printed in the console. 

