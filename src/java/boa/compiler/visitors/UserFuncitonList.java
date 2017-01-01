package boa.compiler.visitors;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nmtiwari on 12/31/16.
 */

public class UserFuncitonList {
    private static List<UserFunctionDetails> functions = new ArrayList<UserFunctionDetails>();
    private static int nextUnProcessedFunctionIndex = 0;


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
          return functions.get(nextUnProcessedFunctionIndex++);
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
         this.functionDeclCode = functionDeclCode;
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
          StringBuffer gencode = new StringBuffer();
          gencode.append(";\n")
                 .append("@Override\n public Object invoke(");

         for(int i = 0; i < this.params.size(); i++) {
             gencode.append("java.util.List<Object> ")
                     .append(params.get(i))
                     .append(",");
         }
          gencode.deleteCharAt(gencode.length() - 1);
          gencode.append(") throws Exception {\n\t")
                  .append("return invoke(");

         for(int i = 0; i < this.params.size(); i++) {
             gencode
//                     .append("( " + compilerGenParams.get(i))
//                     .append(") " )
                     .append(params.get(i))
                     .append(".toArray(")
                     .append("new ")
                     .append(compilerGenParams.get(i))
//                     .append("[")
//                     .append(params.get(i))
                     .insert(gencode.length() - 1, params.get(i) + ".size()")
//                     .append("]")
                     .append(")")
                     .append(",");
         }
         gencode.deleteCharAt(gencode.length() - 1)
                 .append(");\n");

          gencode.append("\t}");
         StringBuffer result = new StringBuffer(code);
         System.out.print(code);
         result.insert(result.length()-2, gencode.toString()).append(";");
         return result.toString();
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
         this.interfaceDecl = interfaceDecl;
     }

}

