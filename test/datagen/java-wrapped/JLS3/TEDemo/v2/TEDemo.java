enum Coin
{
   NICKEL(5),   // constants must appear first
   DIME(10),
   QUARTER(25),
   DOLLAR(100); // the semicolon is required

   private final int valueInPennies;

   Coin(int valueInPennies)
   {
      this.valueInPennies = valueInPennies;
   }

   int toCoins(int pennies)
   {
      return pennies/valueInPennies;
   }
}

public class TEDemo
{
   public static void main(String[] args)
   {
      if (args.length != 1)
      {
          System.err.println("usage: java TEDemo amountInPennies");
          return;
      }
      int pennies = Integer.parseInt(args[0]);
      for (Coin coin: Coin.values())
           System.out.println(pennies+" pennies contains "+
                              coin.toCoins(pennies)+" "+
                              coin.toString().toLowerCase()+"s");
   }
}