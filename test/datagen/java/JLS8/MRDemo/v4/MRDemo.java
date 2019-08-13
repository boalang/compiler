import java.util.function.Supplier;

public class MRDemo
{
   public static void main(String[] args)
   {
      Supplier<MRDemo> supplier = MRDemo::new;
      System.out.println(supplier.get());
   }
}