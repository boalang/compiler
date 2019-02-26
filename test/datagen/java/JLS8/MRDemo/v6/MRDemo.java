import java.util.function.Consumer;

class Superclass
{
   void print(String msg)
   {
      System.out.printf("Superclass print(): %s%n", msg);
   }
}

class Subclass extends Superclass
{
   Subclass()
   {
      Consumer<String> consumer = Subclass.super::print;
      consumer.accept("Subclass.super::print");
      consumer = this::print;
      consumer.accept("this::print");
   }

   @Override
   void print(String msg)
   {
      System.out.printf("Subclass print(): %s%n", msg);
   }
}

public class MRDemo
{
   public static void main(String[] args)
   {
      new Subclass();
   }
}