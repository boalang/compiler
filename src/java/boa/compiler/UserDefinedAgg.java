package boa.compiler;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

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
        private String compilerGenParamCode = null; //  code generated for tuples

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

        public Builder compilerGenParamCode(String param) {
            if(this.compilerGenParamCode == null) {
                this.compilerGenParamCode = param;
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
            if(this.funcArgType != null) {
                st.add("funcArg", fulQualifiedNameGne(funcArgType));
            }else {
                st.add("funcArg", this.userGivenFuncArg);
            }
            return st.render();
        }


        private String fulQualifiedNameGne(String name) {
            if(isPrimitive(funcArgType)) {
                return name;
            } else {
                StringBuffer qualifiedName = new StringBuffer();
                qualifiedName
                        .append("boa.")
                        .append(UserDefinedAggregators.getFileName())
                        .append(".")
                        .append(UserDefinedAggregators.getFileName())
                        .append("BoaMapper")
                        .append(".")
                        .append(UserDefinedAggregators.getJobName())
                        .append(".")
                        .append(funcArgType);
                return  name.replace(funcArgType, qualifiedName.toString());
            }
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