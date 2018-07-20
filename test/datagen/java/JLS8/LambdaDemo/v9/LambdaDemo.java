import java.util.ArrayList;
import java.util.List;

import java.util.function.Predicate;

class Account
{
   private int id, balance;

   Account(int id, int balance)
   {
      this.balance = balance;
      this.id = id;
   }

   int getBalance()
   {
      return balance;
   }

   int getID()
   {
      return id;
   }

   void print()
   {
      System.out.printf("Account: [%d], Balance: [%d]%n", id, balance);
   }
}

public class LambdaDemo
{
   static List<Account> accounts;

   public static void main(String[] args)
   {
      accounts = new ArrayList<>();
      accounts.add(new Account(1000, 200));
      accounts.add(new Account(2000, -500));
      accounts.add(new Account(3000, 0));
      accounts.add(new Account(4000, -80));
      accounts.add(new Account(5000, 1000));
      // Print all accounts
      printAccounts(account -> true);
      System.out.println();
      // Print all accounts with negative balances.
      printAccounts(account -> account.getBalance() < 0);
      System.out.println();
      // Print all accounts whose id is greater than 2000 and less than 5000.
      printAccounts(account -> account.getID() > 2000 && 
                               account.getID() < 5000);
   }

   static void printAccounts(Predicate<Account> tester)
   {
      for (Account account: accounts)
         if (tester.test(account))
            account.print();
   }
}