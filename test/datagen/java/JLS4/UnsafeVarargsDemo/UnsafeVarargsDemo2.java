import java.util.Arrays;
import java.util.List;

public class UnsafeVarargsDemo2 {
	
	 public static <T> void addToList (List<T> listArg, T... elements) {
		    for (T x : elements) {
		      listArg.add(x);
		    }
		  }

		  @SuppressWarnings({"unchecked", "varargs"})
		  public static <T> void addToList2 (List<T> listArg, T... elements) {
		    for (T x : elements) {
		      listArg.add(x);
		    }
		  }
}
