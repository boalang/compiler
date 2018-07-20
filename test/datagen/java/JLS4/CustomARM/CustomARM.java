public class CustomARM
{
   public static void main(String[] args)
   {
      try (USBPort usbp = new USBPort())
      {
         System.out.println(usbp.getID());
      }
      catch (USBException usbe)
      {
         System.err.println(usbe.getMessage());
      }
   }
}

class USBPort implements AutoCloseable
{
   USBPort() throws USBException
   {
      if (Math.random() < 0.5)
         throw new USBException("unable to open port");
      System.out.println("port open");
   }

   @Override
   public void close()
   {
      System.out.println("port close");
   }

   String getID()
   {
      return "some ID";
   }
}

class USBException extends Exception
{
   USBException(String msg)
   {
      super(msg);
   }
}