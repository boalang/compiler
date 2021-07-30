import java.util.ArrayList;
import java.util.List;

import java.util.function.Consumer;
import java.util.function.Predicate;

class Account
{
   private int id, balance;

   Account(int id, int balance)
   {
      this.balance = balance;
      this.id = id;
   }

   void deposit(int amount)
   {
      balance += amount;
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
      // Deposit enough money in accounts with negative balances so that they 
      // end up with zero balances (and are no longer overdrawn).
      adjustAccounts(account -> account.getBalance() < 0,
                     account -> account.deposit(-account.getBalance()));
   }

   static void adjustAccounts(Predicate<Account> tester, 
                              Consumer<Account> adjuster)
   {
      for (Account account: accounts)
      {
         if (tester.test(account))
         {
            adjuster.accept(account);
            account.print();
         }
      }
   }
}