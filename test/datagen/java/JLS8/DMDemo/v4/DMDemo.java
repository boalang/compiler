interface A 
{
   default void method()
   {
      System.out.println("A.method() invoked");
   }
}

public class DMDemo implements A
{
   @Override
   public void method()
   {
      System.out.println("DMDemo.method() invoked");
      A.super.method();
   }

   public static void main(String[] args)
   {
      DMDemo dmdemo = new DMDemo();
      dmdemo.method();
   }
}