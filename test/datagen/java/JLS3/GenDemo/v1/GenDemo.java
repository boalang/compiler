class Container<E>
{
   private E[] elements;
   private int index;

   Container(int size)
   {
      elements = (E[]) new Object[size];
      index = 0;
   }

   void add(E element)
   {
      elements[index++] = element;
   }

   E get(int index)
   {
      return elements[index];
   }

   int size()
   {
      return index;
   }
}

public class GenDemo
{
   public static void main(String[] args)
   {
      Container<String> con = new Container<String>(5);
      con.add("North");
      con.add("South");
      con.add("East");
      con.add("West");
      for (int i = 0; i < con.size(); i++)
         System.out.println(con.get(i));
   }
}