public class LambdaDemo
{
   public static void main(String[] args)
   {
      new LambdaDemo().doWork();
   }

   public void doWork()
   {
      System.out.printf("this = %s%n", this);
      Runnable r = new Runnable()
                       {
                          @Override
                          public void run()
                          {
                             System.out.printf("this = %s%n", this);
                          }
                       };
      new Thread(r).start();
      new Thread(() -> System.out.printf("this = %s%n", this)).start();
   }
}