public class LambdaDemo
{
   public static void main(String[] args)
   {
      int limit = 10;
      Runnable r = () -> {
                           limit = 5;
                           for (int i = 0; i < limit; i++)
                              System.out.println(i);
                         };
   }
}