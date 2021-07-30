public class VarargsDemo
{
   public static void main(String[] args)
   {
      printNames("Java", "JRuby", "Jython", "Scala");
      printNames2(new String[] { "Java", "JRuby", "Jython", "Scala" });
   }

   static void printNames(String... names)
   {
      for (String name: names)
         System.out.println(name);
   }

   static void printNames2(String... names)
   {
      for (int i = 0; i < names.length; i++)
         System.out.println(names[i]);
   }
}