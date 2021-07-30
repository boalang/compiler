import java.util.function.Function;

public class MRDemo
{
   public static void main(String[] args)
   {
      print(String::toLowerCase, "STRING TO LOWERCASE");
      print(s -> s.toLowerCase(), "STRING TO LOWERCASE");
      print(new Function<String, String>()
      {
         @Override
         public String apply(String s) // receives argument in parameter s;
         {                             // doesn't need to close over s
            return s.toLowerCase();
         }
      }, "STRING TO LOWERCASE");
   }

   public static void print(Function<String, String> function, String s)
   {
      System.out.println(function.apply(s));
   }
}