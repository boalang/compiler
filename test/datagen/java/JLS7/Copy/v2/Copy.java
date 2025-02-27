import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Copy
{
   public static void main(String[] args)
   {
      if (args.length != 2)
      {
         System.err.println("usage: java Copy src dst");
         return;
      }

      try
      {
         copy(args[0], args[1]);
      }
      catch (FileNotFoundException fnfe)
      {
         System.err.printf("%s could not be found or is a directory, "+
                           "or %s couldn't be created because it "+
                           "might be a directory%n", args[0], args[1]);
      }
      catch (IOException ioe)
      {
         System.err.printf("%s could not be read or %s could not be "+
                           "written%n", args[0], args[1]);
      }
   }

   static void copy(String src, String dst) throws IOException
   {
      try (FileInputStream fis = new FileInputStream(src); 
           FileOutputStream fos = new FileOutputStream(dst))
      {
         int _byte;
         while ((_byte = fis.read()) != -1)
            fos.write(_byte);
      }
   }
}