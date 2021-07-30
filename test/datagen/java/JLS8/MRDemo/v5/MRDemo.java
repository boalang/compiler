import java.util.function.Function;

public class MRDemo
{
   private String name;

   MRDemo()
   {
      name = "";
   }

   MRDemo(String name)
   {
      this.name = name;
      System.out.printf("MRDemo(String name) called with %s%n", name);
   }

   public static void main(String[] args)
   {
      Function<String, MRDemo> function = MRDemo::new;
      System.out.println(function.apply("some name"));
   }
}