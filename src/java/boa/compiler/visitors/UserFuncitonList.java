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
    private String code;
    private String reducerCode;
    private String returntype;
    private final List<String> compilerGenParams;
    private final List<String> paramCode;

    public UserFunctionDetails() {
        this.compilerGenParams = new ArrayList<String>();
        this.paramCode = new ArrayList<String>();
    }

    public UserFunctionDetails(String name) {
        this();
        this.userGivenName = name;
    }

    public UserFunctionDetails(String name, String code) {
        this(name);
        this.functionDeclCode = code;
    }

    public String getUserGivenName() {
        return this.userGivenName;
    }

    public void setCode(String code) {
        this.code = code;
        this.reducerCode = getAsReducerFunction(code);
    }

    public String getCode() {
        return this.code;
    }

    public String getCompilerGenName() {
        return this.compilerGenName;
    }

    public void setCompilerGenName(String name) {
        this.compilerGenName = name;
    }

    public List<String> getParamCode() {
        return this.paramCode;
    }

    public boolean addTupleParam(String name) {
        return this.compilerGenParams.add(name);
    }

    public boolean addTupleDecl(String code) {
        return this.paramCode.add(code);
    }

    public boolean isParam(String name) {
        return this.compilerGenParams.contains(name) || this.compilerGenParams.contains(name + "[]");
    }

    public String getFunctionDeclCode() {
        return this.functionDeclCode;
    }

    public void setReturntype(String type) {
        this.returntype = type;
    }

    public String getReturntype() {
        return this.returntype;
    }

    private String getAsReducerFunction(String code) {
        StringBuffer codegen = new StringBuffer(code);
        codegen.insert(code.indexOf('\n') , " extends UsrDfndReduceFunc");
        return codegen.toString();
    }

    public String getReducerCode() {
        return this.reducerCode;
    }
}

