import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;

import javax.swing.*;

public class TrussSolver extends JPanel
{
  static Truss myTruss;

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
    myTruss = new Truss(n, len);

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

    JFrame window = new JFrame("Hello world");
    TrussSolver panel = new TrussSolver();

    // This takes the panel component and places it in the
    // JFrame ... it is transparent so can't really be seen.
    window.add(panel);

    // When the user clicks exit to terminate the window.
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    // How big should the window be.
    window.setSize(400, 400);

    // Make the windows render to the screen.
    window.setVisible(true);

    myTruss.print();
    myTruss.solve();
    myTruss.printForces();
  }

    public void paintComponent(Graphics g) {
      super.paintComponent(g);

      double s = 60.0;
      double sH = 50.0;
      double sV = 50.0;

      double min[] = myTruss.getMin();
      double max[] = myTruss.getMax();

      int h = this.getBounds().height;
      int w = this.getBounds().width;

      double xScale = (w - 2*sH) / (max[0] - min[0]);
      double yScale = (h - 2*sV) / (max[1] - min[1]);
      s = Math.min(xScale, yScale);

      sH += Math.abs(min[0]);
      sV += s * (Math.abs(min[1]) + Math.abs(max[1]));


      myTruss.drawEdges(g, s, sH, sV);
      myTruss.drawNodes(g, s, sH, sV, 10);
      myTruss.drawForces(g, s, sH, sV);
    }
}
