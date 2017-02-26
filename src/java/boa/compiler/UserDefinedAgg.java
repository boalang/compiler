package boa.compiler;


import boa.compiler.ast.statements.VarDeclStatement;
import boa.compiler.ast.types.AbstractType;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import java.util.ArrayList;
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
            this.lambdaInit = init;
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
            this.lambdaInterface = decl;
            return this;
        }

        public Builder argTypeName(String param) {
            if(this.funcArgType == null) {
                StringBuffer typeNameBuilder = new StringBuffer(param.substring(0, param.length() - 2));
                typeNameBuilder.setCharAt(0, Character.toUpperCase(typeNameBuilder.charAt(0)));
                this.funcArgType = typeNameBuilder.toString();
            } else {
                throw new RuntimeException("Aggregator function can not have more than one arguments");
            }
            return this;
        }

        public Builder userGivenFuncArg(String param) {
            if(this.userGivenFuncArg == null) {
                this.userGivenFuncArg = param;
            } else {
                throw new RuntimeException("Aggregator function can not have more than one arguments");
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
            st.add("aggParams", getAggregatorOptionParamId());
            if(this.funcArgType != null) {
                st.add("funcArg", fulQualifiedNameGne(funcArgType));
            }else {
                st.add("funcArg", this.userGivenFuncArg);
            }
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

    }
}