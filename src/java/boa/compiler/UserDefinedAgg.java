package boa.compiler;


import boa.compiler.ast.statements.VarDeclStatement;
import boa.compiler.ast.types.AbstractType;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UserDefinedAgg {
    private final String userGivenName;
    private final String code;

    public String getCode() {
        return code;
    }

    public static Builder builder() {
        return new Builder();
    }

    private UserDefinedAgg(String name, String code) {
        this.userGivenName = name;
        this.code = code;
    }

    public String getAggClassName() {
        return "UserDefined$" + this.userGivenName;
    }

    public static class Builder {
        private String funcVarName;
        private String lambdaName;
        private String lambdaType;
        private String lambdaInit;
        private String lambdaInterface;
        private String userGivenFuncArg = null;  // user given param names
        private String funcArgType = null; // funcVarName of the compiler generated userGivenParams
        private String returnType;
        private boolean isAggregator = false;
        private List<String> aggregatorOptionParamId = new ArrayList<String>();
        private List<String> aggregatorOptionParamInitializer = new ArrayList<String>();
        private List<AbstractType> aggregatorOutputParamTypes = new ArrayList<AbstractType>();
        private List<String> userAggOptionVariables = new ArrayList<String>();;
        

        public List<String> getAggregatorOptionParamId() {
            return aggregatorOptionParamId;
        }

        public boolean addAggregatorOptionParamId(String code, int index) {
            return this.aggregatorOptionParamId.add(aggregatorOutputParamTypes.get(index).type.toJavaType() + " " + code + ";\n");
        }

        public boolean addAggregatorOptionParamInitializer(String initializer) {
            return this.aggregatorOptionParamInitializer.add(initializer);
        }

        public void setAggregatorOptionParamType(List<VarDeclStatement> params) {
            for(VarDeclStatement stmt: params) {
                this.aggregatorOutputParamTypes.add(stmt.getType());
            }
        }

        public UserDefinedAgg build(){
            return new UserDefinedAgg(this.funcVarName, this.generateCode());
        }

        public Builder userGivenName(String name) {
            this.funcVarName = name;
            return this;
        }

        public Builder setLambdaNameAndType(String name) {
            String[] details = name.split(" ");
            this.lambdaType = details[0];
            this.lambdaName = details[1].substring(0, details[1].length()- 2);
            return this;
        }

        public Builder lambdaInit(String init) {
            this.lambdaInit = getInterfaceInitCode(init);
            return this;
        }

        public Builder returnType(String type) {
            this.returnType = type;
            return this;
        }

        public boolean isAggregator() {
            return this.isAggregator;
        }

        public boolean isAggregator(boolean flag) {
            this.isAggregator = flag;
            return this.isAggregator;
        }

        public Builder lambdaInterface(String decl) {
            this.lambdaInterface = updatedInterDeclAndGetOptionVars(decl);
            return this;
        }

        //FIXME: This currently just handles only one argument in the user defined aggregators
        public Builder argTypeName(String param) {
            if(this.funcArgType == null) {
                StringBuffer typeNameBuilder = new StringBuffer(param.substring(0, param.length() - 2));
                typeNameBuilder.setCharAt(0, Character.toUpperCase(typeNameBuilder.charAt(0)));
                this.funcArgType = typeNameBuilder.toString();
            }
            return this;
        }

        //FIXME: This currently just handles only one argument in the user defined aggregators
        public Builder userGivenFuncArg(String param) {
            if(this.userGivenFuncArg == null) {
                this.userGivenFuncArg = param;
            }
            return this;
        }

        public boolean isParam(String name) {
            return this.funcArgType.equals(name) || this.funcArgType.equals(name + "[]");
        }

        public String getFuncVarName() {
            return this.funcVarName;
        }

        public String getLambdaType() {
            return lambdaType;
        }

        private String getOverridingMethds() {
            String parser = typeParser(funcArgType.toString());
            STGroup stg = new STGroupFile("templates/UserAggregatorClass.stg");
            final ST st = stg.getInstanceOf("OverridenMethods");

            st.add("name", this.lambdaName);
            st.add("type", funcArgType.toString());
            st.add("returnType", this.returnType);
            if(parser.isEmpty()){
                st.add("nonPrimitive", true);
            }else {
                st.add("parser", parser);
                st.add("javaPrimitiveType", funcArgType.toLowerCase());
            }
            return st.render();
        }

        private String generateCode() {
            STGroup stg = new STGroupFile("templates/UserAggregatorClass.stg");
            final ST st = stg.getInstanceOf("AggregatorClass");

            st.add("funcName", this.getFuncVarName());
            st.add("methods", fulQualifiedNameGne(this.getOverridingMethds()));
            st.add("lambdaInit", fulQualifiedNameGne(this.lambdaInit));
            st.add("lambdaType", this.lambdaType);
            st.add("lambdaName", this.lambdaName);
            st.add("interface", fulQualifiedNameGne(this.lambdaInterface));
            st.add("aggParams", removeFinal());
            if(this.funcArgType != null) {
                st.add("funcArg", fulQualifiedNameGne(funcArgType));
            }else {
                st.add("funcArg", this.userGivenFuncArg);
            }

            String constructor = getAggregatorOptionalVarInit(userAggOptionVariables);
            if(!constructor.isEmpty())
                st.add("constcode", constructor);

            return st.render();
        }


        private String fulQualifiedNameGne(String str) {
                StringBuffer qualifiedName = new StringBuffer();
                qualifiedName
                        .append("boa.")
                        .append(UserDefinedAggregators.getFileName())
                        .append(".")
                        .append(UserDefinedAggregators.getFileName())
                        .append("BoaMapper")
                        .append(".")
                        .append(UserDefinedAggregators.getJobName())
                        .append(".BoaTup_");
                return  str.replace("BoaTup_", qualifiedName.toString());
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
            }
            return result;
        }

        private String updatedInterDeclAndGetOptionVars(String lambdaInterfaceDecl) {
            final int startindex = lambdaInterfaceDecl.indexOf('(');
            final int endIndex = lambdaInterfaceDecl.indexOf(')');
            String allVargs = lambdaInterfaceDecl.substring(startindex + 1, endIndex);
            String vars[] = allVargs.split(",");
            if(vars.length > 0) {
                if(!isArrayArgument(vars[0])) {
                    throw new RuntimeException("First argument of userDefinedAggregator must be array of values");
                }
                // update the interface declaration code with removing all of the arguments with just one
                if(userAggOptionVariables.size() <= 0) {
                    String tmp = vars.length > 1 ? flattenArrayWithSeperator(Arrays.copyOfRange(vars, 1, vars.length), ',') : "";
                    Collections.addAll(userAggOptionVariables, tmp.split(","));
                }
                return lambdaInterfaceDecl.replace(allVargs, vars[0]);
            } else {
                throw new RuntimeException("Userdefined aggregators must have atleast one argument, which is array of values");
            }
        }

        private boolean isArrayArgument(String arg) {
            return arg.matches("final\\s+.*\\[]\\s+_*.*");
        }

        private String flattenArrayWithSeperator(final String[] vars, final char seperator) {
            final StringBuffer flattened = new StringBuffer();
            for(final String s: vars) {
                if(s.startsWith("final")) {
                    flattened.append(s.substring(5)).append(seperator);
                } else {
                    flattened.append(s).append(seperator);
                }
            }
            flattened.deleteCharAt(flattened.length() - 1);
            return flattened.toString();
        }

        private String getInterfaceInitCode(String lambdaInterfaceDecl) {
            final StringBuffer codegenerator = new StringBuffer(lambdaInterfaceDecl);
            //FIXME: this is an hack in the generated code
            final int startindex = lambdaInterfaceDecl.indexOf("invoke") + "invoke".length();
            final int endIndex = lambdaInterfaceDecl.indexOf(" throws"); // space is important here
            String allVargs = lambdaInterfaceDecl.substring(startindex + 1, endIndex - 1);
            System.out.println(allVargs);
            String vars[] = allVargs.split(",");
            if(vars.length > 0) {
                return lambdaInterfaceDecl.replace(allVargs, vars[0]);
            } else {
                throw new RuntimeException("Userdefined aggregators must have atleast one argument, which is array of values");
            }
        }

        private String getAggregatorOptionalVarInit(final List<String> args) {
            if(args.size() <= 0) {
                return "";
            }
            StringBuffer code = new StringBuffer();
            StringBuffer param = new StringBuffer();
            param.append("( ");
            for(String s: args) {
                String[] tmp = s.split(" ");
                code.append("\t\tthis.").append(tmp[tmp.length - 1]).append(" = ").append(tmp[tmp.length - 1]).append(";\n");
                param.append(s).append(", ");
            }
            param.deleteCharAt(param.length() - 2);
            param.append(" ) {").append("\n\t").append("this();\n\t").append(code.toString()).append("\n}");
            return param.toString();
        }

        private List<String> removeFinal() {
            for(int i = 0; i < userAggOptionVariables.size(); i++) {
                userAggOptionVariables.set(i, userAggOptionVariables.get(i).substring(7));
            }
            return userAggOptionVariables;
        }

    }
}