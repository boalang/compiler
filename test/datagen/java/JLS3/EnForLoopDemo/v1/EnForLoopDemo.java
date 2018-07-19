import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class Employee
{
   private String name;
   private int age;

   Employee(String name, int age)
   {
      this.name = name;
      this.age = age;
   }

   public String toString()
   {
      return name+" "+age;
   }
}

public class EnForLoopDemo
{
   public static void main(String[] args)
   {
      List<Employee> employees = new ArrayList<Employee>();
      employees.add(new Employee("John Doe", 25));
      employees.add(new Employee("Sally Smith", 32));

      // Traditional iteration.

      for (Iterator i = employees.iterator(); i.hasNext();)
           System.out.println(i.next());

      System.out.println();

      // Enhanced for loop iteration.

      for (Employee employee: employees)
           System.out.println(employee);
   }
}