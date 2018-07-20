public class LambdaDemo
{
   public static void main(String[] args)
   {
      new Thread(() -> System.out.println("Hello")).start();
   }
}