public class AnnDemo
{
   public static void main(String[] args)
   {
      String[] cities = { "New York", "Melbourne", "Beijing", "Moscow", 
                          "Paris", "London" };
      sort(cities);
   }

   @ToDo(value="1000,10/10/2013,John Doe")
   public static void sort(Object[] objects)
   {
   }

   @ToDo("1000,10/10/2013,John Doe")
   public static boolean search(Object[] objects, Object key)
   {
      return false;
   }
}