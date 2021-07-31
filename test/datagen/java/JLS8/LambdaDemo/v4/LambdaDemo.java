import java.io.File;
import java.io.FileFilter;

import java.nio.file.Files;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitor;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;

import java.nio.file.attribute.BasicFileAttributes;

import java.security.AccessController;
import java.security.PrivilegedAction;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import java.util.concurrent.Callable;

public class LambdaDemo
{
   public static void main(String[] args) throws Exception
   {
      // Target type #1: variable declaration

      Runnable r = () -> { System.out.println("running"); };
      r.run();
      
      // Target type #2: assignment

      r = () -> System.out.println("running");
      r.run();

      // Target type #3: return statement (in getFilter())

      File[] files = new File(".").listFiles(getFilter("txt"));
      for (File file: files)
         System.out.println(file);

      // Target type #4: array initializer

      FileSystem fs = FileSystems.getDefault();
      final PathMatcher matchers[] = 
      {
         (path) -> path.toString().endsWith("txt"),
         (path) -> path.toString().endsWith("java")
      };
      FileVisitor<Path> visitor;
      visitor = new SimpleFileVisitor<Path>() 
                {
                   @Override
                   public FileVisitResult visitFile(Path file, 
                                                    BasicFileAttributes attribs)
                   {
                      Path name = file.getFileName();
                      for (PathMatcher matcher: matchers)
                      {
                         if (matcher.matches(name))
                            System.out.printf("Found matched file: '%s'.%n", 
                                              file);
                      }
                      return FileVisitResult.CONTINUE;
                   }
                };
      Files.walkFileTree(Paths.get("."), visitor);

      // Target type #5: method or constructor arguments

      new Thread(() -> System.out.println("running")).start();

      // Target type #6: lambda body (a nested lambda)

      Callable<Runnable> callable = () -> () -> 
         System.out.println("called");
      callable.call().run();

      // Target type #7: ternary conditional expression

      boolean ascendingSort = false;
      Comparator<String> cmp;
      cmp = (ascendingSort) ? (s1, s2) -> s1.compareTo(s2) 
                            : (s1, s2) -> s2.compareTo(s1);
      List<String> cities = Arrays.asList("Washington", "London", "Rome", 
                                          "Berlin", "Jerusalem", "Ottawa", 
                                          "Sydney", "Moscow");
      Collections.sort(cities, cmp);
      for (String city: cities)
         System.out.println(city);

      // Target type #8: cast expression

      String user = AccessController.doPrivileged((PrivilegedAction<String>) () 
                                           -> System.getProperty("user.name"));
      System.out.println(user);
   }

   static FileFilter getFilter(String ext)
   {
      return (pathname) -> pathname.toString().endsWith(ext);
   }
}