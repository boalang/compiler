import java.io.File;

import java.util.Date;

public class Touch
{
   static Date current = new Date(0);

   public static void main(String[] args)
   {
      for (String arg: args)
         switch (arg)
         {
            case "-c": 
            case "-C": current = new Date();
                       break;

            case "-h":
            case "-H": help();
                       break;

         }
      touch(new File("."));
   }

   static void help()
   {
      String helpInfo = "Touch files with the same timestamp.\n"+
                        "\n"+
                        "Command-line options:\n"+
                        "\n"+
                        "-c | -C use current timestamp\n"+
                        "-h | -H output this help message\n";
      System.out.println(helpInfo);
   }

   static void touch(File start)
   {
      File[] files = start.listFiles();
      for (int i = 0; i < files.length; i++)
      {
         files[i].setLastModified(current.getTime());
         if (files[i].isDirectory())
            touch(files[i]);
      }
   }
}