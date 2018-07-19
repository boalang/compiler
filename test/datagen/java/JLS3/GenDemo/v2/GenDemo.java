import java.math.BigDecimal;

import java.util.Arrays;

abstract class Employee
{
   private BigDecimal hourlySalary;
   private String name;

   Employee(String name, BigDecimal hourlySalary)
   {
      this.name = name;
      this.hourlySalary = hourlySalary;
   }

   public BigDecimal getHourlySalary()
   {
      return hourlySalary;
   }

   public String getName()
   {
      return name;
   }

   public String toString()
   {
      return name+": "+hourlySalary.toString();
   }
}

class Accountant extends Employee implements Comparable<Accountant>
{
   Accountant(String name, BigDecimal hourlySalary)
   {
      super(name, hourlySalary);
   }

   public int compareTo(Accountant acct)
   {
      return getHourlySalary().compareTo(acct.getHourlySalary());
   }
}

class SortedEmployees<E extends Employee & Comparable<E>>
{
   private E[] employees;
   private int index;

   SortedEmployees(int size)
   {
      employees = (E[]) new Employee[size];
      int index = 0;
   }

   void add(E emp)
   {
      employees[index++] = emp;
      Arrays.sort(employees, 0, index);
   }

   E get(int index)
   {
      return employees[index];
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
      SortedEmployees<Accountant> se = new SortedEmployees<Accountant>(10);
      se.add(new Accountant("John Doe", new BigDecimal("35.40")));
      se.add(new Accountant("George Smith", new BigDecimal("15.20")));
      se.add(new Accountant("Jane Jones", new BigDecimal("25.60")));
      
      for (int i = 0; i < se.size(); i++)
         System.out.println(se.get(i));
   }
}