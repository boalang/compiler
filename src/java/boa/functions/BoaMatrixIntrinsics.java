package boa.functions;

import boa.BoaTup;
import boa.compiler.ast.literals.IntegerLiteral;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by nmtiwari on 2/11/17.
 */
public class BoaMatrixIntrinsics {
    /**
     * Returns the matrix version of an array. Only scalar values can be sorted.
     * Values will be arranged in increasing order. (An optional comparison
     * function, which takes two elements and returns int {-,0,+}, is accepted
     * as a second argument, but it is curently ignored.)
     *
     * @param a
     *            An array of long
     *
     * @return A sorted copy of <em>a</em>
     */
    @FunctionSpec(name = "matrix", returnType = "array of array of int", formalParameters = { "array of int", "int" })
    public static long[][] matrix(final long[] a, final long colsize) {
        final int cols = (int)colsize;
        final int rows = a.length/cols;
        final long[][] result = new long[rows][cols];
        for(int i = 0; i< rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = a[i * cols + j];
            }
        }
        return result;
    }

    /**
     * Returns the matrix version of an array. Only scalar values can be sorted.
     * Values will be arranged in increasing order. (An optional comparison
     * function, which takes two elements and returns int {-,0,+}, is accepted
     * as a second argument, but it is curently ignored.)
     *
     * @param a
     *            An array of long
     *
     * @return A sorted copy of <em>a</em>
     */
    @FunctionSpec(name = "matrix", returnType = "array of array of string", formalParameters = { "array of string", "int" })
    public static String[][] matrix(final String[] a, final long colsize) {
        final int cols = (int)colsize;
        final int rows = a.length/cols;
        final String[][] result = new String[rows][cols];
        for(int i = 0; i< rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = a[i * cols + j];
            }
        }
        return result;
    }

    /**
     * Returns the matrix version of an array. Only scalar values can be sorted.
     * Values will be arranged in increasing order. (An optional comparison
     * function, which takes two elements and returns int {-,0,+}, is accepted
     * as a second argument, but it is curently ignored.)
     *
     * @param a
     *            An array of long
     *
     * @return A sorted copy of <em>a</em>
     */
    @FunctionSpec(name = "matrix", returnType = "array of array of float", formalParameters = { "array of float", "int" })
    public static float[][] matrix(final float[] a, final long colsize) {
        final int cols = (int)colsize;
        final int rows = a.length/cols;
        final float[][] result = new float[rows][cols];
        for(int i = 0; i< rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = a[i * cols + j];
            }
        }
        return result;
    }

    /**
     * Returns the matrix version of an array. Only scalar values can be sorted.
     * Values will be arranged in increasing order. (An optional comparison
     * function, which takes two elements and returns int {-,0,+}, is accepted
     * as a second argument, but it is curently ignored.)
     *
     * @param a
     *            An array of long
     *
     * @return A sorted copy of <em>a</em>
     */
    @FunctionSpec(name = "matrix", returnType = "array of array of tuple", formalParameters = { "array of tuple", "int" })
    public static BoaTup[][] matrix(final BoaTup[] a, final long colsize) {
        final int cols = (int)colsize;
        final int rows = a.length/cols;
        final BoaTup[][] result = new BoaTup[rows][cols];
        for(int i = 0; i< rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = a[i * cols + j];
            }
        }
        return result;
    }

    /**
     * Returns the matrix version of an array. Only scalar values can be sorted.
     * Values will be arranged in increasing order. (An optional comparison
     * function, which takes two elements and returns int {-,0,+}, is accepted
     * as a second argument, but it is curently ignored.)
     *
     * @param a
     *            An array of long
     *
     * @return A sorted copy of <em>a</em>
     */
    @FunctionSpec(name = "flattenedMatrix", returnType = "array of array of float", formalParameters = { "array of tuple", "int" })
    public static double[][] flattenedMatrix(final BoaTup[] a, final long colsize) {
        final List<Double> flattenedTuples = new ArrayList<Double>();
        for(int i = 0; i< a.length; i++) {
            for(Double ele: a[i].<Double>asArray(new Double[1])){
                flattenedTuples.add(ele);
            }
        }

        final int cols = (int)colsize;
        final int rows = flattenedTuples.size()/cols;
        final double[][] result = new double[rows][cols];

        for(int i = 0; i< rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = flattenedTuples.get(i * cols + j);
            }
        }
        return result;
    }


}
