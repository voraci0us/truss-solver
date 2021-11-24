import java.util.*;
import java.io.*;

public class TrussSolver
{
  public static void main (String[] args)
  {
    Scanner s;
    try {
      File file = new File(args[0]);
      s =  new Scanner(file);
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
      return;
    }

    int n = s.nextInt();
    System.out.println("Scanning in "+n+" nodes...");
    double len = s.nextDouble();
    Truss myTruss = new Truss(n, len);

    for (int i = 0; i < n; i++)
    {
      double x = s.nextDouble();
      double y = s.nextDouble();
      myTruss.addNode(Truss.character(i), x, y);
    }

    int e = s.nextInt();
    System.out.println("Scanning in "+e+" edges...");
    s.nextLine();
    for (int i = 0; i < e; i++)
    {
      String str = s.nextLine();
      myTruss.addEdge(str.charAt(0), str.charAt(1));
    }

    int f = s.nextInt();
    System.out.println("Scanning in "+f+" external forces...\n");
    s.nextLine();
    for (int i = 0; i < f; i++)
    {
      String str = s.next();
      myTruss.addExt(str.charAt(0), s.nextDouble(), s.nextDouble());
    }

    myTruss.print();
    myTruss.solve();
    myTruss.printForces();
    myTruss.display();
  }
}
