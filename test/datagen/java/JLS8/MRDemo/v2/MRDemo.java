import java.util.function.Supplier;

public class MRDemo
{
   public static void main(String[] args)
   {
      String s = "The quick brown fox jumped over the lazy dog";
      print(s::length);
      print(() -> s.length());
      print(new Supplier<Integer>()
      {
         @Override
         public Integer get()
         {
            return s.length(); // closes over s
         }
      });
   }

   public static void print(Supplier<Integer> supplier)
   {
      System.out.println(supplier.get());
   }
}