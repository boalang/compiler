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

     public String getCompilerGenName() {
         return compilerGenName;
     }

     public void setCompilerGenName(String compilerGenName) {
         this.compilerGenName = compilerGenName;
     }

     public String getFunctionDeclCode() {
         return functionDeclCode;
     }

     public String getFuncInitCode() {
         return getFuncInitCode(funcInitCode);
     }

     private String getFuncInitCode(String code) {
         StringBuffer result = new StringBuffer(code);
         result.append(";");
         return autoBoxPrimitives(result.toString()).replace("\n", "\n\t");
     }

     public void setFuncInitCode(String funcInitCode) {
         StringBuffer code = new StringBuffer(funcInitCode);
         int retIndexStart = code.indexOf("public ") + "public ".length();
         int retIndexEnd = code.indexOf("invoke");
         String returnT = code.substring(retIndexStart, retIndexEnd);
         if (returnT.endsWith("[] ")) {
             int startIndexofRetStmt = code.indexOf("return ") + "return ".length();
             String retStmt = code.substring(startIndexofRetStmt);
             int endIndexofRetStmt = startIndexofRetStmt + retStmt.indexOf(';');
             retStmt = code.substring(startIndexofRetStmt, endIndexofRetStmt);

             // replace code from the return type and return statement
             code.replace(startIndexofRetStmt, endIndexofRetStmt, "java.util.Arrays.toString(" + retStmt + ")");
             code.replace(retIndexStart, retIndexEnd, "String ");
         }
         this.funcInitCode = code.toString();
     }

     public boolean isParam(String name) {
        return this.compilerGenParams.contains(name) || this.compilerGenParams.contains(name + "[]");
    }

     public void setInterfaceDecl(String interfaceDecl) {
         StringBuffer code = new StringBuffer(interfaceDecl);
         int retIndexStart = code.indexOf("{") + 1;
         int retIndexEnd = code.indexOf(" invoke");
         String returnT = code.substring(retIndexStart, retIndexEnd);
         if (returnT.endsWith("[]")) {
             // replace code from the return type and return statement
             code.replace(retIndexStart, retIndexEnd, "\n\tString ");
         }
         interfaceDecl = code.toString();
         this.interfaceDecl = "\t" + autoBoxPrimitives(interfaceDecl);
     }

     private String autoBoxPrimitives(String code) {
         return code.replace("long[]", "Long[]")
                 .replace("double[", "Double[")
                 .replace("float[", "Float[")
                 .replace("char[", "Char[")
                 .replace("byte[", "Byte[")
                 .replace("boolean[", "Boolean[");
     }

     public String getUserAggClass() {
         String typename = this.compilerGenParams.get(0).substring(0, this.compilerGenParams.get(0).length() - 2);
         typename = typename.substring(0, 1).toUpperCase() + typename.substring(1);
         String mapperType  = "";
         mapperType = isPrimitive(typename) ? typename : "boa." + UserFuncitonList.getFileName() + "."
                 + UserFuncitonList.getFileName()
                 + "BoaMapper."
                 + UserFuncitonList.getJobName()
                 + "."
                 + typename;

         StringBuffer code = new StringBuffer();
         code.append("class ")
                 .append(this.aggClassName)
                 .append(" extends boa.aggregators.UserDefinedAggregator {\n")
                 .append("\t java.util.List<")
                 .append(typename)
                 .append("> _$values")
                 .append(";\n")
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
         return code.toString().replace(typename, mapperType);
     }

     private String getOverridingMethods() {
         String typeName = this.compilerGenParams.get(0).substring(0, this.compilerGenParams.get(0).length() - 2);
         typeName = typeName.substring(0, 1).toUpperCase() + typeName.substring(1);
         StringBuffer methods = new StringBuffer();
         String name = this.functionDeclCode.split(" ")[1];
         methods.append("\n    @Override\n")
                 .append("    public void aggregate(String data, String metadata) throws java.io.IOException, ")
                 .append("java.lang.InterruptedException, boa.aggregators.FinishedException {\n")
                 .append("        this._$values.add(")
                 .append(typeParser(typeName));

         methods.append("(data));\n    }\n\n\t@Override\n    public void finish() throws java.io.IOException, java.lang.InterruptedException {\n")
                 .append("        try{\n")
                 .append("            this.collect(this.")
                 .append(name.substring(0, name.length() - 2))
                 .append(".invoke(this._$values.toArray")
                 .append("( new ")
                 .append(typeName)
                 .append("[")
                 .append("this._$values.size()")
                 .append("]")
                 .append(")")
                 .append("));\n        }catch(Exception e) {\n            e.printStackTrace();\n        }\n    }\n")
                 .append("\t@Override\n \t public void start(final boa.io.EmitKey key) {\n\t\t")
                 .append("_$values = new java.util.ArrayList<")
                 .append(typeName)
                 .append(">();\n")
                 .append("        super.start(key);\n    }");

         if (!isPrimitive(typeName)) {
            methods.append("\n  @Override  \n")
                     .append("    public void aggregate(")
                     .append("BoaTup data, String metadata) throws java.io.IOException, ")
                     .append("java.lang.InterruptedException, boa.aggregators.FinishedException {\n")
                     .append("        this._$values.add(")
                     .append("(")
                     .append(typeName)
                     .append(")data);\n\t}");
         }

         return methods.toString();
     }

     private String typeParser(String typeName) {
         String result = "";
         if ("long".equalsIgnoreCase(typeName)
                 || "int".equalsIgnoreCase(typeName)
                 || "long[]".equalsIgnoreCase(typeName)
                 || "int[]".equalsIgnoreCase(typeName)
                 || "integer[]".equalsIgnoreCase(typeName)
                 || "integer".equalsIgnoreCase(typeName)) {
             result = "Long.parseLong";
         } else if("float".equalsIgnoreCase(typeName)
                 || "double".equalsIgnoreCase(typeName)
                 || "float[]".equalsIgnoreCase(typeName)
                 || "double[]".equalsIgnoreCase(typeName)) {
             result = "Double.parseDouble";
         } else if("boolean".equalsIgnoreCase(typeName)
                 || "boolean[]".equalsIgnoreCase(typeName)
                 || "bool".equalsIgnoreCase(typeName)
                 || "bool[]".equalsIgnoreCase(typeName)) {
             result = "Boolean.valueOf";
         } else if("string".equalsIgnoreCase(typeName)
                 || "string[]".equalsIgnoreCase(typeName)
                 || "char".equalsIgnoreCase(typeName)
                 || "char[]".equalsIgnoreCase(typeName)
                 || "Byte[]".equalsIgnoreCase(typeName)
                 || "byte[]".equalsIgnoreCase(typeName)) {
             result = "";
         } else {
             result = typeName + ".deSerialize";
         }
         return result;
     }

     private boolean isPrimitive(String typeName) {
         return "long".equalsIgnoreCase(typeName)
                 || "int".equalsIgnoreCase(typeName)
                 || "long[]".equalsIgnoreCase(typeName)
                 || "int[]".equalsIgnoreCase(typeName)
                 || "integer[]".equalsIgnoreCase(typeName)
                 || "integer".equalsIgnoreCase(typeName)
                 || "float".equalsIgnoreCase(typeName)
                 || "double".equalsIgnoreCase(typeName)
                 || "float[]".equalsIgnoreCase(typeName)
                 || "double[]".equalsIgnoreCase(typeName)
                 || "boolean".equalsIgnoreCase(typeName)
                 || "boolean[]".equalsIgnoreCase(typeName)
                 || "bool".equalsIgnoreCase(typeName)
                 || "bool[]".equalsIgnoreCase(typeName)
                 || "String[]".equalsIgnoreCase(typeName)
                 || "String".equalsIgnoreCase(typeName)
                 || "byte".equalsIgnoreCase(typeName)
                 || "byte[]".equalsIgnoreCase(typeName)
                 || "char[]".equalsIgnoreCase(typeName)
                 || "char".equalsIgnoreCase(typeName);
     }

}

