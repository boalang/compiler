package boa.compiler.visitors;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nmtiwari on 12/31/16.
 */

public class UserFuncitonList {
    private static List<UserFunctionDetails> functions = new ArrayList<UserFunctionDetails>();
    private static int nextUnProcessedFunctionIndex = 0;
    private static String fileName = "";
    private static String jobName = "Job0";

    public static void setJobName(String name) {
        UserFuncitonList.jobName = name;
    }

    public static String getJobName() {
        return UserFuncitonList.jobName;
    }

    public static void setFileName(String name) {
        name = name.substring(0, name.indexOf('.'));
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        UserFuncitonList.fileName = name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public static String getFileName() {
        return UserFuncitonList.fileName;
    }


    public static boolean addUserFunction(UserFunctionDetails function) {
        return functions.add(function);
    }

     public static UserFunctionDetails findByUserGivenName(String name) {
         for(UserFunctionDetails function : functions) {
             if(function.getUserGivenName().equals(name)) {
                 return function;
             }
         }
         return null;
     }

     public static UserFunctionDetails findByCompilerGivenName(String name) {
         for(UserFunctionDetails function : functions) {
             if(function.getCompilerGenName().equals(name)) {
                 return function;
             }
         }
         return null;
     }

     public static UserFunctionDetails getNextUnProcessedFunction() {
          if(nextUnProcessedFunctionIndex < functions.size()) {
              return functions.get(nextUnProcessedFunctionIndex++);
          }
          return null;
     }

     public static List<UserFunctionDetails> getAllFunction() {
         return functions;
     }
}



 class UserFunctionDetails {
     private String userGivenName;
     private String compilerGenName;
     private String functionDeclCode;
     private String funcInitCode;
     private String reducerCode;
     private String returntype;
     private String interfaceDecl;
     private String aggClassName;
     private final List<String> params;  // user given param names
     private final List<String> compilerGenParams; // name of the compiler generated params
     private final List<String> parameterGenCode; //  code generated for tuples

     public UserFunctionDetails() {
         this.params = new ArrayList<String>();
         this.compilerGenParams = new ArrayList<String>();
         this.parameterGenCode = new ArrayList<String>();
     }

     public UserFunctionDetails(String name, String functionDeclCode) {
         this();
         this.userGivenName = name;
         this.functionDeclCode = "\t" + functionDeclCode;
         this.aggClassName = "userDefined$" + this.userGivenName;
     }

     public String getAggClassName() {
         return aggClassName;
     }

     public List<String> getParameterGenCode() {
         return parameterGenCode;
     }

     public boolean addParam(String name) {
         return this.params.add(name);
     }

     public boolean addCompilerGenParams(String name) {
         return this.compilerGenParams.add(name);
     }

     public boolean addParameterGenCode(String name) {
         return this.parameterGenCode.add(name);
     }

     public String getUserGivenName() {
         return userGivenName;
     }

     public void setUserGivenName(String userGivenName) {
         this.userGivenName = userGivenName;
     }

     public String getCompilerGenName() {
         return compilerGenName;
     }

     public void setCompilerGenName(String compilerGenName) {
         this.compilerGenName = compilerGenName;
     }

     public String getFunctionDeclCode() {
         return functionDeclCode;
     }

     public void setFunctionDeclCode(String functionDeclCode) {
         this.functionDeclCode = functionDeclCode;
     }

     public String getFuncInitCode() {
         return getFuncInitCode(funcInitCode);
     }

     private String getFuncInitCode(String code) {
         StringBuffer result = new StringBuffer(code);
         result.append(";");
         return result.toString().replace("long[", "Long[").replace("\n", "\n\t");
     }

     public void setFuncInitCode(String funcInitCode) {
         this.funcInitCode = funcInitCode;
     }

     public String getReducerCode() {
         return reducerCode;
     }

     public void setReducerCode(String reducerCode) {
         this.reducerCode = reducerCode;
     }

     public String getReturntype() {
         return returntype;
     }

     public void setReturntype(String returntype) {
         this.returntype = returntype;
     }

     public boolean isParam(String name) {
        return this.compilerGenParams.contains(name) || this.compilerGenParams.contains(name + "[]");
    }

    public String getAsReducerIFunctionDecl(String code) {
        StringBuffer codegen = new StringBuffer(code);
        codegen.insert(code.indexOf('\n') , " extends UsrDfndReduceFunc");
        return codegen.toString();
    }

     public String getInterfaceDecl() {
         return interfaceDecl;
     }

     public void setInterfaceDecl(String interfaceDecl) {
         this.interfaceDecl = "\t" + interfaceDecl.replace("long[]", "Long[]");
     }

     public String getUserAggClass() {
         String mapperType  = "boa." + UserFuncitonList.getFileName() + "."
                 + UserFuncitonList.getFileName()
                 + "BoaMapper."
                 + UserFuncitonList.getJobName()
                 + "."
                 + this.compilerGenParams.get(0).substring(0, this.compilerGenParams.get(0).length() - 2);

         StringBuffer code = new StringBuffer();
         code.append("class ")
                 .append(this.aggClassName)
                 .append(" extends boa.aggregators.UserDefinedAggregator {\n")
                 .append("\t java.util.List<")
                 .append(this.compilerGenParams.get(0).substring(0, this.compilerGenParams.get(0).length() - 2))
                 .append("> _$values = new java.util.ArrayList<")
                 .append(this.compilerGenParams.get(0).substring(0, this.compilerGenParams.get(0).length() - 2))
                 .append(">();\n")
                 .append(this.getFunctionDeclCode())
                 .append(this.interfaceDecl.replace("\n", "\n\t"))
                 .append("public userDefined$")
                 .append(this.userGivenName)
                 .append("() {\n\t\t")
                 .append(this.getFunctionDeclCode().replace("\n", "\n\t").split(" ")[1])
                 .delete(code.length() - 3, code.length() - 1)
                 .append(" = " + getFuncInitCode().replace("\n", "\n\t"))
                 .deleteCharAt(code.length() - 1)
                 .append(";\n")
                 .append("\t}")
                 .append(getOverridingMethods())
                 .append("\n }");
         return code.toString().replace(this.compilerGenParams.get(0).substring(0, this.compilerGenParams.get(0).length() - 2), mapperType);
     }

     private String getOverridingMethods() {
         StringBuffer methods = new StringBuffer();
         String name = this.functionDeclCode.split(" ")[1];
         methods.append("\n    @Override\n")
                 .append("    public void aggregate(String data, String metadata) throws java.io.IOException, ")
                 .append("java.lang.InterruptedException, boa.aggregators.FinishedException {\n")
                 .append("        this._$values.add(")
                 .append(this.compilerGenParams.get(0).substring(0, this.compilerGenParams.get(0).length() - 2))
                 .append(".deSerialize(data));\n    }\n\n\t@Override\n    public void finish() throws java.io.IOException, java.lang.InterruptedException {\n")
                 .append("        try{\n")
                 .append("            this.collect(this.")
                 .append(name.substring(0, name.length() - 2))
                 .append(".invoke(this._$values.toArray")
                 .append("( new ")
                 .append(this.compilerGenParams.get(0).substring(0, this.compilerGenParams.get(0).length() - 2))
                 .append("[")
                 .append("this._$values.size()")
                 .append("]")
                 .append(")")
                 .append("));\n        }catch(Exception e) {\n            e.printStackTrace();\n        }\n    }")
                 .append("\n  @Override  \n")
                 .append("    public void aggregate(")
                 .append("BoaTup data, String metadata) throws java.io.IOException, ")
                 .append("java.lang.InterruptedException, boa.aggregators.FinishedException {\n")
                 .append("        this._$values.add(")
                 .append("(")
                 .append(this.compilerGenParams.get(0).substring(0, this.compilerGenParams.get(0).length() - 2))
                 .append(")data);\n\t}");

         return methods.toString();
     }

}

