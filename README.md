#BoaEvaluator: Development setup for local Mode Boa evaluation
This file describes how you might set up your development environment to view, edit and test
the Boa compiler's source code on local system.

##Using Eclipse
1. Setup your development environment by following the instruction from [Development Setup page](https://github.com/boalang/compiler/blob/master/doc/dev/setup.md).  
  
2. You have successfully imported the project into IDE and have the capabilities to trigger ant builds and edit project 
source files within IDE itself. 

3. In order to run a Boa program locally, compiler compiles Boa program to Java and runs the generated Java program using 
Reflections and Hadoop libraries. To enable this within the Eclipse IDE  
    1. Create a directory named **"compile"** in the project's root directory.           
    2. From the project's "Properties > Java Build Path", select the "Libraries" tab."
    3. Use "Add External Class Folder" to add newly created directory (compile) in classpath. 
    4. After adding the "compile" directory, your "Libraries" tab should look something like this: ![](doc/dev/img/eclipse_library_tab.png)  
    6. From "Run > Run Configuration > Java Application" select "Main" tab to create a "Run Configuration" for BoaEvaluator class.
       After this step your "Main" tab should look something like this: ![](doc/dev/img/eclipse_runconfig_main.png)
    7. Select "Arguments" tab in same window to provide program arguments separated by single space.  
       Program arguments include 
       1. **Path to Boa program**   
       2. **Path of the local dataset**  
       3. **Path of the output directory**.   
       Your "Arguments" tab should be look like: ![](doc/dev/img/eclipse_runconfig_arguments.png)
    8. Hit apply and Run, this will run your Boa program on local data.  Once your BoaEvaluator finishes with the execution
      of the query, your "Console" will look similar to this: ![]( doc/dev/img/boa_evaluator_output.png )

##DataSet
1. A small data set is provided to start experiments. You can access the dataset under "dataset" directory in root.    
The complete dataset comprises of 3 files:
1. **index:** map to look into data stored in data file
2. **data:** Abstract syntax tree of each project 
3. **projects.seq:** metadata information

**This dataset contain 2 projects, [Boa](https://github.com/boalang/compiler) and [Panini](https://github.com/hridesh/panc).**
