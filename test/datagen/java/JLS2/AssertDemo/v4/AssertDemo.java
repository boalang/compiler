public class AssertDemo
{
   public static void main(String[] args)
   {
      int[] array = { 20, 91, -6, 16, 0, 7, 51, 42, 3, 1 };
      sort(array);
  
   }

   private static boolean isSorted(int[] x)
   {
      for (int i = 0; i < x.length-1; i++)
         if (x[i] > x[i+1])
            return false;
      return true;
   }

   private static void sort(int[] x)
   {
      int j, a;
      // For all integer values except the leftmost value ...
      for (int i = 1; i < x.length; i++)
      {
         // Get integer value a.
         a = x[i];
         // Get index of a. This is the initial insert position, which is
         // used if a is larger than all values in the sorted section.
         j = i;
         // While values exist to the left of a's insert position and the
         // value immediately to the left of that insert position is
         // numerically greater than a's value ...
         while (j > 0 && x[j-1] > a)
         {
            // Shift left value -- x[j-1] -- one position to its right --
            // x[j].
            x[j] = x[j-1];
            // Update insert position to shifted value's original position
            // (one position to the left).
            j--;
         }
         // Insert a at insert position (which is either the initial insert
         // position or the final insert position), where a is greater than
         // or equal to all values to its left.
         x[j] = a;
      }

      assert isSorted(x): "array not sorted";
   }
}