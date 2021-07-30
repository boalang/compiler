import java.util.ArrayList;
import java.util.List;

public class GenDemo
{
   public static void main(String[] args)
   {
      List<Integer> grades = new ArrayList<Integer>();
      Integer[] gradeValues = 
      {
         new Integer(96),
         new Integer(95),
         new Integer(27),
         new Integer(100),
         new Integer(43),
         new Integer(68)
      };
      for (int i = 0; i < gradeValues.length; i++)
         grades.add(gradeValues[i]);
      List<Integer> failedGrades = new ArrayList<Integer>();
      copy(grades, failedGrades, new Filter<Integer>()
                                 {
                                    public boolean accept(Integer grade)
                                    {
                                       return grade.intValue() <= 50;
                                    }
                                 });
      for (int i = 0; i < failedGrades.size(); i++)
         System.out.println(failedGrades.get(i));
   }

   static <T> void copy(List<T> src, List<T> dest, Filter<T> filter)
   {
      for (int i = 0; i < src.size(); i++)
         if (filter.accept(src.get(i)))
            dest.add(src.get(i));
   }
}

interface Filter<T>
{
   boolean accept(T o);
}