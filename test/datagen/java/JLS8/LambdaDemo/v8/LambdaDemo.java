import java.awt.AWTException;

import java.io.IOException;

@FunctionalInterface
interface Work
{
   void doSomething() throws IOException;
}

public class LambdaDemo
{
   public static void main(String[] args) throws AWTException, IOException
   {
      Work work = () -> { throw new IOException(); };
      work.doSomething();
      work = () -> { throw new AWTException(""); };
   }
}