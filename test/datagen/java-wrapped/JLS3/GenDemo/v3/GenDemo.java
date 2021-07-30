import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GenDemo
{
   public static void main(String[] args)
   {
      List<String> directions = new ArrayList<String>();
      directions.add("north");
      directions.add("south");
      directions.add("east");
      directions.add("west");
      printList(directions);

      List<Integer> grades = new ArrayList<Integer>();
      grades.add(new Integer(98));
      grades.add(new Integer(63));
      grades.add(new Integer(87));
      printList(grades);
   }

   static void printList(List<Object> list)
   {
      Iterator<Object> iter = list.iterator();
      while (iter.hasNext())
         System.out.println(iter.next());
   }
}