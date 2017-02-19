package boa.functions;

import Jama.Matrix;
import boa.BoaTup;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Longs;


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

}
