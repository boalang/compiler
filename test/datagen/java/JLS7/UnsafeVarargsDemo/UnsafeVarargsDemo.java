import java.util.Arrays;
import java.util.List;

public class UnsafeVarargsDemo
{
   public static void main(String[] args)
   {
      unsafe(Arrays.asList("A", "B", "C"),
             Arrays.asList("D", "E", "F"));
   }

   @SafeVarargs
   static void unsafe(List<String>... l)
   {
      Object[] oArray = l;
      oArray[0] = Arrays.asList(new Double(3.5));
      String s = l[0].get(0);
   }
}