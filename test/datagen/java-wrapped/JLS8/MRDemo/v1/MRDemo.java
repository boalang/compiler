import java.util.Arrays;

import java.util.function.Consumer;

public class MRDemo
{
   public static void main(String[] args)
   {
      int[] array = { 10, 2, 19, 5, 17 };
      Consumer<int[]> consumer = Arrays::sort;
      consumer.accept(array);
      for (int x: array)
         System.out.println(x);
      System.out.println();
      int[] array2 = { 19, 5, 14, 3, 21, 4 };
      Consumer<int[]> consumer2 = (a) -> Arrays.sort(a);
      consumer2.accept(array2);
      for (int x: array2)
         System.out.println(x);
   }
}