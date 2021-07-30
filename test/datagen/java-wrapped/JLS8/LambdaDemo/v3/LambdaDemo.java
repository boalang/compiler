@FunctionalInterface
interface BinaryCalculator
{
   double calculate(double value1, double value2);
}

@FunctionalInterface
interface UnaryCalculator
{
   double calculate(double value);
}

public class LambdaDemo
{
   public static void main(String[] args)
   {
      System.out.printf("18 + 36.5 = %f%n", calculate((double v1, double v2) -> 
                        v1 + v2, 18, 36.5));
      System.out.printf("89 / 2.9 = %f%n", calculate((v1, v2) -> v1 / v2, 89,
                        2.9));
      System.out.printf("-89 = %f%n", calculate(v -> -v, 89));
      System.out.printf("18 * 18 = %f%n", calculate((double v) -> v * v, 18));
   }

   static double calculate(BinaryCalculator calc, double v1, double v2)
   {
      return calc.calculate(v1, v2);
   }

   static double calculate(UnaryCalculator calc, double v)
   {
      return calc.calculate(v);
   }
}