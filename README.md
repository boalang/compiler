#BoaEvaluator: Local Mode Boa Compiler
BoaEvaluator is a facility to run Boa scripts in local mode, without explicit 
Hadoop setup. This facility provides easy way to experiment with Boa programs before
submitting the queries to remote. A default small dataset is also provided with 
this repository.

##Using IntelliJ Idea

1. **Cloning Boa Repository:** You can follow this step from 
    [Official IntelliJ Page](https://www.jetbrains.com/help/idea/2016.3/cloning-a-repository-from-github.html) 
    or
    1.  Open IntelliIdea and click on "Checkout from Version Control" option from IntelliJ Idea Startup menu  
    2.  Choose Github as version control (You may be asked for you Github credentials)  
    2.  From the Repository drop-down list, select the source repository to clone the 
        data from.     
    4.  In the Folder text box, specify the directory where the local repository for 
        cloned sources will be set up. Type the path to the directory manually or click 
        the Browse button and choose the desired  directory in the Select project 
        destination folder dialog box that opens.   
    5.  In the Project name text box, specify the name of the project to be created 
        based on the cloned sources.  
    6.  Click the Clone button to start cloning the sources from the specified remote repository.
    
2. **Project Setup**
    1. After cloning the project, IntelliJ prompts if you want to create a project
       **Hit Yes** and **Next**.   
    2. IntelliJ will automatically import the libs and modules available in project.  
       (**Hit Next** for all the steps, until project is imported.)   
    3. You have successfully imported the project into IDE.
    4. You need a directory for compiling your Boa program to Java program. So,
       create a directory named "compile" (Please note that the name of the directory must exactly 
       match "compile", this is in order to load your compiled Java program and run it using reflections) and add that to dependencies of the project. Please see [Official IntelliJ Page 
       for managing dependencies](https://www.jetbrains.com/help/idea/2016.3/configuring-module-dependencies-and-libraries.html)  
    5. Now you are all set to run your Boa program locally. Add your Boa program and path to local directory into BoaEvaluator class
         and hit run. Please note that you can also provide output directory path to direct evaluator to write output.
        

##Using Eclipse

1. **Cloning Boa Repository:** You can follow this step from 
    [Official Eclipse Page](http://wiki.eclipse.org/EGit/User_Guide) 
    or
    1.  Open Eclipse and click on "File->import->Project From Git -> clone URI".    
    2.  Provide path to local directory  
    2.  Hit next and then finish. This process will clone Boa directory to your local directory.
    
2. **Project Setup**
    1. After cloning the project, import your program as simple Java program. You can follow instructions from [here](http://agile.csc.ncsu.edu/SEMaterials/tutorials/import_export/).       
    2. Once you have imported the program as Java program, you need a directory for compiling Boa program to Java program.       
    3. You have successfully imported the project into IDE.
    4. You need a directory for compiling your Boa program to Java program. So,
       create a directory named "compile" (Please note that the name of the directory must exactly 
       match "compile", this is in order to load your compiled Java program and run it using reflections) and add that to dependencies of the project. Please see [Official Eclipse Page 
       for managing dependencies](http://www.oxfordmathcenter.com/drupal7/node/44)  
    5. Now you are all set to run your Boa program locally. Add your Boa program and path to local directory into BoaEvaluator class
         and hit run. Please note that you can also provide output directory path to direct evaluator to write output.
