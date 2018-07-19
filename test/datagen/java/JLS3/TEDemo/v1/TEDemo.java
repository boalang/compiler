public class TEDemo
{
   enum Direction { NORTH, WEST, EAST, SOUTH }

   public static void main(String[] args)
   {
      for (int i = 0; i < Direction.values().length; i++)
      {
         Direction d = Direction.values()[i];
         System.out.println(d);
         switch (d)
         {
            case NORTH: System.out.println("Move north"); break;
            case WEST : System.out.println("Move west"); break;
            case EAST : System.out.println("Move east"); break;
            case SOUTH: System.out.println("Move south"); break;
            default   : assert false: "unknown direction";
         }
      }
      System.out.println(Direction.NORTH.compareTo(Direction.SOUTH));
   }
}