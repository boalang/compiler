public class BinLitDemo
{
   public static void main(String[] args)
   {
      byte byte1 = 0b01111111;
      System.out.println(byte1); // Output: 127

      byte byte2 = (byte) 0b10000000;
      System.out.println(byte2); // Output: -128

      long long1 = 0B1010101010101010101010101010101010101010101010101010101010101010L;
      System.out.println(long1);

      short mask = 0b1100001;
      System.out.println(Integer.toBinaryString(0b11001101 & mask)); // Output: 10000001

      System.out.println();

      short[][] font =
      {
         // Letter A

         // ...

         // Letter E

         {
            0b0111111111,
            0b0100000000,
            0b0100000000,
            0b0100000000,
            0b0111111110,
            0b0100000000,
            0b0100000000,
            0b0100000000,
            0b0111111111
         },

         // Letter T

         {
            0b0111111111,
            0b0000010000,
            0b0000010000,
            0b0000010000,
            0b0000010000,
            0b0000010000,
            0b0000010000,
            0b0000010000,
            0b0000010000
         }

         // ...
      };

      int offsetE = 0;
      int offsetT = 1;
      outputLetter(font, offsetE);
      System.out.println();
      outputLetter(font, offsetT);
   }

   static void outputLetter(short[][] font, int offset)
   {
      short[] powers = { 256, 128, 64, 32, 16, 8, 4, 2, 1 };
      for (int row = 0; row < font[offset].length; row++)
      {
         for (int col = 0; col < powers.length; col++)
            System.out.print(((font[offset][row]&powers[col]) != 0) ? '*' : ' ');
         System.out.println();
      }
   }
}