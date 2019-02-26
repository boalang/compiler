import java.io.Closeable;
import java.io.IOException;

public class SupExDemo implements Closeable
{
   @Override
   public void close() throws IOException
   {
      System.out.println("close() invoked");
      throw new IOException("I/O error in close()");
   }

   public void doWork() throws IOException
   {
      System.out.println("doWork() invoked");
      throw new IOException("I/O error in work()");
   }

   public static void main(String[] args) throws IOException
   {
      try (SupExDemo supexDemo = new SupExDemo())
      {
         supexDemo.doWork();
      }
      catch (IOException ioe)
      {
         ioe.printStackTrace();
         System.out.println();
         System.out.println(ioe.getSuppressed()[0]);
      }
   }
}