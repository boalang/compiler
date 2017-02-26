package boa.functions;

import Jama.Matrix;
import boa.BoaTup;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Longs;
import org.apache.commons.lang.ArrayUtils;


import java.lang.reflect.Array;
import java.util.*;

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
    @FunctionSpec(name = "multiply", returnType = "array of array of int", formalParameters = { "array of array of int", "array of array of int" })
    public static long[][] multiply(final long[][] a, final long[][] b) {
        double[][] a_double = new double[a.length][];
        double[][] b_double = new double[b.length][];

        for(int i = 0; i< a.length; i++){
            a_double[i] = Doubles.toArray(Longs.asList(a[i]));
        }

        for(int i = 0; i< b.length; i++){
            b_double[i] = Doubles.toArray(Longs.asList(b[i]));
        }
        Matrix a_matrix = new Matrix(a_double);
        Matrix b_matrix = new Matrix(b_double);
        double[][] result = a_matrix.times(b_matrix).getArray();
        long[][]result_long = new long[result.length][];
        for(int i = 0; i< result.length; i++){
            result_long[i] = Longs.toArray(Doubles.asList(result[i]));
        }
        return result_long;
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
    @FunctionSpec(name = "multiply", returnType = "array of array of int", formalParameters = { "array of array of int", "int" })
    public static long[][] multiply(final long[][] a, final long b) {
        double[][] a_double = new double[a.length][];

        for(int i = 0; i< a.length; i++){
            a_double[i] = Doubles.toArray(Longs.asList(a[i]));
        }

        Matrix a_matrix = new Matrix(a_double);
        double[][] result = a_matrix.times(b).getArray();
        long[][]result_long = new long[result.length][];
        for(int i = 0; i< result.length; i++){
            result_long[i] = Longs.toArray(Doubles.asList(result[i]));
        }
        return result_long;
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
    @FunctionSpec(name = "multiply", returnType = "array of array of int", formalParameters = { "array of array of int", "float" })
    public static double[][] multiply(final long[][] a, final float b) {
        double[][] a_double = new double[a.length][];

        for(int i = 0; i< a.length; i++){
            a_double[i] = Doubles.toArray(Longs.asList(a[i]));
        }

        Matrix a_matrix = new Matrix(a_double);
        return a_matrix.times(b).getArray();
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
    @FunctionSpec(name = "matrixsum", returnType = "array of array of int", formalParameters = { "array of array of int", "array of array of int" })
    public static long[][] matrixsum(final long[][] a, final long[][] b) {
        double[][] a_double = new double[a.length][];
        double[][] b_double = new double[b.length][];

        for(int i = 0; i< a.length; i++){
            a_double[i] = Doubles.toArray(Longs.asList(a[i]));
        }

        for(int i = 0; i< b.length; i++){
            b_double[i] = Doubles.toArray(Longs.asList(b[i]));
        }
        Matrix a_matrix = new Matrix(a_double);
        Matrix b_matrix = new Matrix(b_double);
        double[][] result = a_matrix.plus(b_matrix).getArray();
        long[][]result_long = new long[result.length][];
        for(int i = 0; i< result.length; i++){
            result_long[i] = Longs.toArray(Doubles.asList(result[i]));
        }
        return result_long;
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
    @FunctionSpec(name = "matrixsubstract", returnType = "array of array of int", formalParameters = { "array of array of int", "array of array of int" })
    public static long[][] matrixsubstract(final long[][] a, final long[][] b) {
        double[][] a_double = new double[a.length][];
        double[][] b_double = new double[b.length][];

        for(int i = 0; i< a.length; i++){
            a_double[i] = Doubles.toArray(Longs.asList(a[i]));
        }

        for(int i = 0; i< b.length; i++){
            b_double[i] = Doubles.toArray(Longs.asList(b[i]));
        }
        Matrix a_matrix = new Matrix(a_double);
        Matrix b_matrix = new Matrix(b_double);
        double[][] result = a_matrix.minus(b_matrix).getArray();
        long[][]result_long = new long[result.length][];
        for(int i = 0; i< result.length; i++){
            result_long[i] = Longs.toArray(Doubles.asList(result[i]));
        }
        return result_long;
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
    @FunctionSpec(name = "multiply", returnType = "array of array of float", formalParameters = { "array of array of float", "array of array of float" })
    public static double[][] multiply(final double[][] a, final double[][] b) {
        Matrix a_matrix = new Matrix(a);
        Matrix b_matrix = new Matrix(b);
        return a_matrix.times(b_matrix).getArray();
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
    @FunctionSpec(name = "multiply", returnType = "array of array of float", formalParameters = { "array of array of float", "float" })
    public static double[][] multiply(final double[][] a, final double b) {
        Matrix a_matrix = new Matrix(a);
        return a_matrix.times(b).getArray();
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
    @FunctionSpec(name = "multiply", returnType = "array of array of float", formalParameters = { "array of array of float", "int" })
    public static double[][] multiply(final double[][] a, final long b) {
        Matrix a_matrix = new Matrix(a);
        return a_matrix.times(b).getArray();
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
    @FunctionSpec(name = "multiply", returnType = "array of array of float", formalParameters = { "array of float", "array of array of float" })
    public static double[][] multiply(final double[] a, final double[][] b) {
        Matrix a_matrix = new Matrix(a, 1);
        Matrix b_matrix = new Matrix(b);
        return a_matrix.times(b_matrix).getArray();
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
    @FunctionSpec(name = "multiply", returnType = "array of float", formalParameters = { "array of float", "float" })
    public static double[] multiply(final double[] a, final double b) {
        Matrix a_matrix = new Matrix(a, a.length);
        return a_matrix.times(b).getArray()[0];
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
    @FunctionSpec(name = "multiply", returnType = "array of float", formalParameters = { "array of float", "int" })
    public static double[] multiply(final double[] a, final long b) {
        Matrix a_matrix = new Matrix(a, 1);
        double[][] temp = a_matrix.times(b).getArray();;
        return temp[0];
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
    @FunctionSpec(name = "multiply", returnType = "array of array of float", formalParameters = { "array of array of float", "array of float" })
    public static double[][] multiply(final double[][] a, final double[] b) {
        Matrix a_matrix = new Matrix(a);
        Matrix b_matrix = new Matrix(b, 1);
        return a_matrix.times(b_matrix).getArray();
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
    @FunctionSpec(name = "matrixsum", returnType = "array of array of float", formalParameters = { "array of array of float", "array of array of float" })
    public static double[][] matrixsum(final double[][] a, final double[][] b) {
        Matrix a_matrix = new Matrix(a);
        Matrix b_matrix = new Matrix(b);
        return a_matrix.plus(b_matrix).getArray();
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
    @FunctionSpec(name = "matrixsum", returnType = "array of float", formalParameters = { "array of float", "array of float" })
    public static double[] matrixsum(final double[] a, final double[] b) {
        if(a.length != b.length) {
            throw new IllegalArgumentException("Argument lengths are not equal");
        }
        double[] result = new double[a.length];
        for(int i =0 ; i < a.length; i++) {
            result[i] = a[i] + b[i];
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
    @FunctionSpec(name = "matrixsubstract", returnType = "array of array of float", formalParameters = { "array of array of float", "array of array of float" })
    public static double[][] matrixsubstract(final double[][] a, final double[][] b) {
        Matrix a_matrix = new Matrix(a);
        Matrix b_matrix = new Matrix(b);
        return a_matrix.minus(b_matrix).getArray();
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
    @FunctionSpec(name = "flatten", returnType = "array of float", formalParameters = { "array of array of float" })
    public static double[] flatten(final double[][] a) {
        final double[] flattenedTuples = new double[a.length * a[0].length];
        int counter = 0;
        for(int i = 0; i< a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                flattenedTuples[counter++] = a[i][j];
            }
        }
        return flattenedTuples;
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
    @FunctionSpec(name = "flatten", returnType = "array of tuple", formalParameters = { "array of array of tuple" })
    public static BoaTup[] flatten(final BoaTup[][] a) {
        final BoaTup[] flattenedTuples = new BoaTup[a.length * a[0].length];
        int counter = 0;
        for(int i = 0; i< a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                flattenedTuples[counter++] = a[i][j];
            }
        }
        return flattenedTuples;
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
    @FunctionSpec(name = "flatten", returnType = "array of float", formalParameters = { "tuple" })
    public static double[] flatten(final BoaTup a) {
        final int size = tupleLength(a);
        final double[] results = new double[size];
        int i = 0;
        for(Double ele: a.<Double>asArray(new Double[1])){
            results[i] = ele;
        }
        return results;
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
    @FunctionSpec(name = "tupleLength", returnType = "int", formalParameters = { "tuple" })
    public static int tupleLength(final BoaTup a) {
        final int sizeOfTuple = a.<Double>asArray(new Double[1]).length;
        return sizeOfTuple;
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
    @FunctionSpec(name = "means", returnType = "array of float", formalParameters = { "array of tuple"})
    public static double[] means(final BoaTup[] a) {
        final int sizeOfTuple = a[0].<Double>asArray(new Double[1]).length;
        final double[] means = new double[sizeOfTuple];

        for(int i = 0; i< a.length; i++) {
            Double[] tupAsArr = a[i].<Double>asArray(new Double[1]);
            for (int index = 0; index < sizeOfTuple; index++) {
                means[index] = means[index] + tupAsArr[index];
            }
        }

        for (int index = 0; index < sizeOfTuple; index++) {
            means[index] = means[index] / a.length;
        }
        return means;
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
    @FunctionSpec(name = "sum", returnType = "array of float", formalParameters = { "array of tuple"})
    public static double[] sum(final BoaTup[] a) {
        final int sizeOfTuple = a[0].<Double>asArray(new Double[1]).length;
        final double[] sum = new double[sizeOfTuple];

        for(int i = 0; i< a.length; i++) {
            Double[] tupAsArr = a[i].<Double>asArray(new Double[1]);
            for (int index = 0; index < sizeOfTuple; index++) {
                sum[index] = sum[index] + tupAsArr[index];
            }
        }

        return sum;
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
    @FunctionSpec(name = "sum", returnType = "array of float", formalParameters = { "tuple", "tuple"})
    public static double[] sum(final BoaTup a, final BoaTup b) {
        final int sizeOfTupleA = a.<Double>asArray(new Double[1]).length;
        final int sizeOfTupleB = b.<Double>asArray(new Double[1]).length;
        if(sizeOfTupleA != sizeOfTupleB) {
            throw new IllegalArgumentException("Dissimilar sttributes in Tuples");
        }
        final double[] sum = new double[sizeOfTupleA];
        for(int i = 0; i< sizeOfTupleA; i++) {
            Double[] tupAsArrA = a.<Double>asArray(new Double[1]);
            Double[] tupAsArrB = a.<Double>asArray(new Double[1]);
            for (int index = 0; index < sizeOfTupleA; index++) {
                sum[index] = tupAsArrB[index] + tupAsArrA[index];
            }
        }
        return sum;
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
    @FunctionSpec(name = "flatten", returnType = "array of int", formalParameters = { "array of array of int" })
    public static long[] flatten(final long[][] a) {
        final long[] flattenedTuples = new long[a.length * a[0].length];
        int counter = 0;
        for(int i = 0; i< a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                flattenedTuples[counter++] = a[i][j];
            }
        }
        return flattenedTuples;
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
    @FunctionSpec(name = "transpose", returnType = "array of array of float", formalParameters = { "array of array of float" })
    public static double[][] transpose(final double[][] a) {
        Matrix matrix = new Matrix(a);
        matrix = matrix.transpose();
        return matrix.getArray();
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
    @FunctionSpec(name = "transpose", returnType = "array of array of float", formalParameters = { "array of float" })
    public static double[][] transpose(final double[] a) {
        double[][] data = new double[a.length][];
        for(int i = 0; i < a.length; i++){
            double[] __temp = {a[i]};
            data[i] = __temp;
        }
        return data;
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
    @FunctionSpec(name = "eigenvalsReal", returnType = "array of float", formalParameters = { "array of array of float" })
    public static double[] eigenvalsReal(final double[][] a) {
        Matrix matrix = new Matrix(a);
        double[] temp = matrix.eig().getRealEigenvalues();
        return matrix.eig().getRealEigenvalues();
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
    @FunctionSpec(name = "eigenvalsImg", returnType = "array of float", formalParameters = { "array of array of float" })
    public static double[] eigenvalsImg(final double[][] a) {
        Matrix matrix = new Matrix(a);
        return matrix.eig().getImagEigenvalues();
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
    @FunctionSpec(name = "eigenvectors", returnType = "array of array of float", formalParameters = { "array of array of float" })
    public static double[][] eigenvectors(final double[][] a) {
        Matrix matrix = new Matrix(a);
        return matrix.eig().getV().getArray();
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
    @FunctionSpec(name = "vector", returnType = "array of array of float", formalParameters = { "array of float" })
    public static double[][] vector(final double[] a) {
        double[][] data = new double[a.length][];
        for(int i = 0; i < a.length; i++){
            double[] __temp = {a[i]};
            data[i] = __temp;
        }
        return data;
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
    @FunctionSpec(name = "inverse", returnType = "array of array of float", formalParameters = { "array of array of float" })
    public static double[][] inverse(final double[][] a) {
        Matrix matrix = new Matrix(a);
        matrix = matrix.inverse();
        return matrix.getArray();
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
    @FunctionSpec(name = "inverse", returnType = "array of array of float", formalParameters = { "array of array of int" })
    public static double[][] inverse(final long[][] a) {
        double[][] data = new double[a.length][];
        for(int i = 0; i< data.length; i++){
            data[i] = Doubles.toArray(Longs.asList(a[i]));
        }
        Matrix matrix = new Matrix(data);
        matrix = matrix.inverse();
        return matrix.getArray();
    }

    /**
     * Returns the matrix version of an array.
     *
     * @param a
     *            An array of array of int
     *
     * @return A sub matrix from Matrix <em>a</em>
     */
    @FunctionSpec(name = "submatrix", returnType = "array of array of int", formalParameters = { "array of array of int", "int", "int", "int", "int"  })
    public static long[][] submatrix(final long[][] a, final long r_start, final long r_end, final long c_start, final long c_end) {
        double[][] data = new double[a.length][];
        for(int i = 0; i< data.length; i++){
            data[i] = Doubles.toArray(Longs.asList(a[i]));
        }
        Matrix matrix = new Matrix(data);
        matrix = matrix.getMatrix((int)r_start, (int)r_end, (int)c_start, (int)c_end);
        data = matrix.getArray();
        long[][] result = new long[a.length][];
        for(int i = 0; i< data.length; i++){
            result[i] = Longs.toArray(Doubles.asList(data[i]));
        }
        return result;
    }

    /**
     * Returns the matrix version of an array.
     *
     * @param a
     *            An array of array of int
     *
     * @return A sub matrix from Matrix <em>a</em>
     */
    @FunctionSpec(name = "submatrix", returnType = "array of array of float", formalParameters = { "array of array of float", "int", "int", "int", "int" })
    public static double[][] submatrix(final double[][] a, final long r_start, final long r_end, final long c_start, final long c_end) {
        Matrix matrix = new Matrix(a);
        matrix = matrix.getMatrix((int)r_start, (int)r_end, (int)c_start, (int)c_end);
        return matrix.getArray();
    }

    /**
     * Returns the matrix version of an array.
     *
     * @param a
     *            An array of array of int
     *
     * @return A sub matrix from Matrix <em>a</em>
     */
    @FunctionSpec(name = "identity", returnType = "array of array of float", formalParameters = { "int", "int" })
    public static double[][] identity(final int row, final int col) {
        Matrix matrix = Matrix.identity(row, col);
        return matrix.getArray();
    }

    /**
     * Returns the matrix version of an array.
     *
     * @param a
     *            An array of array of int
     *
     * @return A sub matrix from Matrix <em>a</em>
     */
    @FunctionSpec(name = "getCol", returnType = "array of float", formalParameters = { "array of array of float", "int" })
    public static double[] getCol(final double[][] data, final long col) {
        double[] result = new double[data.length];
        for(int i = 0;  i < result.length; i++ ) {
            result[i] = data[i][(int)col];
        }
        return result;
    }

    /**
     * Returns the matrix version of an array.
     *
     * @param a
     *            An array of array of int
     *
     * @return A sub matrix from Matrix <em>a</em>
     */
    @FunctionSpec(name = "unique", returnType = "array of float", formalParameters = { "array of float" })
    public static double[] unique(final double[] data) {
        Set<Double> set = new HashSet<Double>(Doubles.asList(data));
        final double[] result = new double[set.size()];
        int i = 0;
        for(Double d: set) {
            result[i++] = d;
        }
        return result;
    }


    /**
     * Returns the matrix version of an array.
     *
     * @param a
     *            An array of array of int
     *
     * @return A sub matrix from Matrix <em>a</em>
     */
    @FunctionSpec(name = "unique", returnType = "array of int", formalParameters = { "array of int" })
    public static long[] unique(final long[] data) {
        Set<Long> set = new HashSet<Long>(Longs.asList(data));
        final Long[] result = set.toArray(new Long[data.length]);
        return ArrayUtils.toPrimitive(result);
    }

    /**
     * Returns the matrix version of an array.
     *
     * @param a
     *            An array of array of int
     *
     * @return A sub matrix from Matrix <em>a</em>
     */
    @FunctionSpec(name = "getRow", returnType = "array of float", formalParameters = { "array of array of float", "int" })
    public static double[] getRow(final double[][] data, final long row) {
        return data[(int)row];
    }

    /**
     * Returns the matrix version of an array.
     *
     * @param a
     *            An array of array of int
     *
     * @return A sub matrix from Matrix <em>a</em>
     */
    @FunctionSpec(name = "getCol", returnType = "array of int", formalParameters = { "array of array of int", "int" })
    public static long[] getCol(final long[][] data, final long col) {
        long[] result = new long[data[0].length];
        for(int i = 0;  i < data[0].length; i++ ) {
            result[i] = data[i][(int)col];
        }
        return result;
    }


    /**
     * Returns the matrix version of an array.
     *
     * @param a
     *            An array of array of int
     *
     * @return A sub matrix from Matrix <em>a</em>
     */
    @FunctionSpec(name = "getRow", returnType = "array of int", formalParameters = { "array of array of int", "int" })
    public static long[] getRow(final long[][] data, final int row) {
        return data[row];
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
    @FunctionSpec(name = "meanScaling", returnType = "array of array of float", formalParameters = { "array of array of float", "array of float" })
    public static double[][] meanScaling(final double[][] a, final double[] b) {
        for(int i = 0; i < a.length; i++) {
            for(int j = 0; j < a[i].length; j++) {
                a[i][j] = a[i][j] - b[j];
            }
        }
        Matrix a_matrix = new Matrix(a);
        return a_matrix.getArray();
    }



}
