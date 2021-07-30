public class EnForLoopDemo
{
   public static void main(String[] args)
   {
      int[] x = { 10, 30, 500, -1 };

      for (int i: x)
           System.out.println(i);

      System.out.println();

      // The above code is implemented in
      // terms of a loop such as this loop:

      for (int j = 0; j < x.length; j++)
      {
         int i = x[j];
        System.out.println(i);
      }
   }
}