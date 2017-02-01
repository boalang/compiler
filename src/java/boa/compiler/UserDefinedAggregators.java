package boa.compiler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import boa.compiler.UserDefinedAgg.Builder;

public class UserDefinedAggregators {
    private static List<Builder> allFunctions = new ArrayList<Builder>();
    private static String jobName;
    private static String fileName;

    public static void setJobName(String jobName) {
        UserDefinedAggregators.jobName = jobName;
    }

    public static void setFileName(String fileName) {
        fileName = fileName.substring(0, fileName.indexOf('.'));
        fileName = fileName.substring(0, 1).toUpperCase() + fileName.substring(1);
        UserDefinedAggregators.fileName = fileName.substring(0, 1).toUpperCase() + fileName.substring(1);;
    }

    public static Iterator<Builder> iterator() {
        return allFunctions.iterator();
    }

    public static boolean addNewUserFunction(Builder function) {
        return allFunctions.add(function);
    }

    public static List<Builder> getAllFunctions() {
        return allFunctions;
    }

    public static Builder findByUserGivenName(String name) {
        for(Builder function : allFunctions) {
            if(function.getFuncVarName().equals(name)) {
                return function;
            }
        }
        return null;
    }

    public static Builder findByLambdaType(String name) {
        for(Builder function : allFunctions) {
            if(function.getLambdaType().equals(name)) {
                return function;
            }
        }
        return null;
    }

    public static String getJobName() {
        return jobName;
    }

    public static String getFileName() {
        return fileName;
    }
}