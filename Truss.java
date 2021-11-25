import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;

import javax.swing.*;

public class Truss extends Frame
{
  // # of nodes
  final int n;
  // # of edges
  int e;

  // n x n adjacency matrices
  boolean[][] adj; // if true, there is an edge between i and j
  boolean[][] solved; // if true, we have solved for forces here

  double[][] length; // calculated by node locations

  // n x 2 adjacency matrices
  double[][] loc; // x, y coords of each node
  double[][] ext; // x, y components of external forces

  double maxMemberLength; // defines the longest beam allowed
  double allowableStress;

  HashMap<String, Double> forces;

  // if no member length is specified, set an arbitrarily large one
  public Truss(int n)
  {
    this(n, 9999999.0);
  }

  public Truss(int n, double maxMemberLength)
  {
      super("TrussSolver");
      this.n = n;
      this.e = 0;
      this.maxMemberLength = maxMemberLength;
      this.allowableStress = 0.25;

      adj = new boolean[n][n];
      solved = new boolean[n][n];
      length = new double[n][n];
      for (int i = 0; i < n; i++)
      {
        for (int j = 0; j < n; j++)
        {
          adj[i][j] = false;
          solved[i][j] = false;
          length[i][j] = 0;
        }
      }

      loc = new double[n][2];
      ext = new double[n][2];
      for (int i = 0; i < n; i++)
      {
        ext[i][0] = 0.0;
        ext[i][1] = 0.0;
      }

      forces = new HashMap<String, Double>();

      setSize(480, 200);
      //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setLocationRelativeTo(null);
  }


  public void display(){
     setVisible(true);
     addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent windowEvent){
           System.exit(0);
        }
     });
  }

  public void paint(Graphics g) {
    super.paint(g);

    double s = 60.0;
    double sH = 50.0;
    double sV = 50.0;

    double xMin = loc[0][0];
    double xMax = loc[0][0];
    double yMin = -loc[0][1];
    double yMax = -loc[0][1];
    for (int i = 0; i < n; i++)
    {
     if (loc[i][0] < xMin)
       xMin = loc[i][0];
     if (loc[i][0] > xMax)
       xMax = loc[i][0];
     if (-loc[i][1] < yMin)
       yMin = -loc[i][1];
     if (-loc[i][1] > yMax)
       yMax = -loc[i][1];
    }

    double xSize = (xMax - xMin) * s + sH * 2;
    double ySize = (yMax - yMin) * s + sV * 2;
    setSize((int) xSize, (int) ySize);
    sH -= xMin * s;
    sV -= yMin * s;

    drawEdges(g, s, sH, sV);
    drawNodes(g, s, sH, sV, 10);
    drawForces(g, s, sH, sV);
  }

  void drawEdges(Graphics g, double s, double sH, double sV)
  {
   Graphics2D g2 = (Graphics2D)g;

   for (int i = 0; i < n; i++)
   {
      for (int j = 0; j <= i; j++)
      {
        if (adj[i][j])
        {
         int x1 = (int) (loc[i][0] * s + sH);
         int y1 = (int) (-loc[i][1] * s + sV);
         int x2 = (int) (loc[j][0] * s + sH);
         int y2 = (int) (-loc[j][1] * s + sV);
         g2.drawLine(x1, y1, x2, y2);
        }
      }
    }
  }

  void drawNodes(Graphics g, double s, double sH, double sV, int size)
  {
    Graphics2D g2 = (Graphics2D)g;
    for (int i = 0; i < n; i++)
    {
      int x = (int) (loc[i][0] * s + sH) - (size / 2);
      int y = (int) (-loc[i][1] * s + sV) - (size / 2);
      g2.fillOval(x, y, size, size);

      int textShift = -2;
      if (loc[i][1] < 0)
        textShift = 22;
      g2.drawString(Character.toString(Truss.character(i)), x, y + textShift);

    }
  }

  void drawForces(Graphics g, double s, double sH, double sV)
  {
    Graphics2D g2 = (Graphics2D)g;
    for (int i = 0; i < n; i++)
    {
      for (int j = 0; j <= i; j++)
      {
        String edge = Truss.character(i) + "" + Truss.character(j);
        Double force = this.forces.get(edge);
        if (force != null)
        {
          int x = (int) (((loc[i][0] + loc[j][0]) / 2) * s + sH);
          int y = (int) (-((loc[i][1] + loc[j][1]) / 2) * s + sV);
          g2.setColor(Color.RED);
          String display;
          if (force == 0)
            display = "Zero-force";
          else if (force > 0)
            display = String.format("%.2fkN (T)", force);
          else
            display = String.format("%.2fkN (C)", force);
          g2.drawString(display, x, y);
        }
      }
    }
  }


  public static int index(char c)
  {
    return (int) c - 'A';
  }

  public static char character(int index)
  {
    return (char) (index + (int) 'A');
  }

  public void addNode(char c, double x, double y)
  {
    int index = index(c);
    if (index >= 0 && index < this.n)
    {
      loc[index(c)][0] = x;
      loc[index(c)][1] = y;
    }
    else
    {
      System.out.println("Failed to add node "+c+", out of bounds");
    }
  }

  public void printAdj()
  {
    System.out.println("\nADJ[][]");
    for (int i = 0; i < n; i++)
    {
      for (int j = 0; j < n; j++)
        System.out.print(adj[i][j]+" ");
      System.out.println();
    }
    System.out.println();
  }

  public void printSolved()
  {
    System.out.println("\nSOVLED[][]");
    for (int i = 0; i < n; i++)
    {
      for (int j = 0; j < n; j++)
        System.out.print(solved[i][j]+" ");
      System.out.println();
    }
    System.out.println();
  }

  public void addEdge(char a, char b)
  {
    int A = index(a);
    int B = index(b);
    this.adj[A][B] = true;
    this.adj[B][A] = true;
    this.e++;
  }

  public void addExt(char c, double x, double y)
  {
    int i = index(c);
    this.ext[i][0] = x;
    this.ext[i][1] = y;
  }

  public void print()
  {
    System.out.println("Given a truss defined by the following:");
    for (int i = 0; i < n; i++)
    {
      System.out.print(String.format("%c (%.3f, %.3f): ", character(i), loc[i][0], loc[i][1]));
      for (int j = 0; j < n; j++)
      {
        if (this.adj[i][j])
        {
          System.out.print(character(j)+" ");
        }
      }
      System.out.println("");
    }
    System.out.println("");

    System.out.println("Given external forces:");
    for(int i = 0; i < n; i++)
    {
      System.out.println(String.format("%c: (%.2f, %.2f)", character(i), ext[i][0], ext[i][1]));
    }
    System.out.println("");
  }



  public void calculateLengths()
  {
    for (int i = 0; i < this.n; i++)
    {
      for (int j = 0; j < this.n; j++)
      {
        if (this.adj[i][j] && this.length[i][j] == 0)
        {
          double X = this.loc[j][0] - this.loc[i][0];
          double Y = this.loc[j][1] - this.loc[i][1];
          double L = Math.sqrt(X * X + Y * Y);
          this.length[i][j] = L;
          if (L > this.maxMemberLength)
          {
            System.out.println(String.format("***MEMBER %c%c IS %.3f LONG, MUST NOT EXCEED %.3f***", character(i), character(j), L, this.maxMemberLength));
          }
        }
      }
    }

    //printLengths();
  }

  public void printLengths()
  {
    for (int i = 0; i < n; i++)
    {
      for (int j = 0; j < n; j++)
      {
        System.out.print(String.format("%.2f ", length[i][j]));
      }
      System.out.println();
    }
  }

  public int countUnknowns(int index)
  {
    int unknown = 0;
    for (int i = 0; i < this.n; i++)
      if (this.adj[index][i] && !this.solved[index][i])
        unknown++;
    return unknown;
  }

  public void handleForce(double f, int a, int b)
  {

    double formattedForce = f * -1;
    if (Math.abs(formattedForce) < 0.001)
        formattedForce = 0;
    this.forces.put(character(a)+""+character(b), formattedForce);
    this.forces.put(character(b)+""+character(a), formattedForce);

    this.solved[a][b] = true;
    this.solved[b][a] = true;
  }


  public void printMatrix(double[][] matrix)
  {
    for (int i = 0; i < 2; i++)
    {
      for (int j = 0; j < 3; j++)
      {
        System.out.print(String.format("%.3f ", matrix[j][i]));
      }
      System.out.println("");
    }
  }

  public void solve()
  {
    calculateLengths();
    this.forces.clear();

    // make a min queue of joints by unknown edge count
    // initially, this count is just the size of the adjacency list
    PriorityQueue<Node> q = new PriorityQueue<Node>();
    for (int i = 0; i < this.n; i++)
    {
      q.add(new Node(i, countUnknowns(i)));
      //System.out.println(String.format("Adding joint %c with %d unknowns", character(i), countUnknowns(i)));

    }

    for (int cnt = 0; cnt < this.n; cnt++)
    {
      // pop the top item off min queue
      Node thisNode = q.poll();
      int ind = thisNode.index;

      int unknowns = countUnknowns(ind);
      //System.out.println(String.format("Solving joint %c with %d unknowns", character(ind), unknowns));
      //System.out.println(String.format("Node %c has %d unknowns", character(ind), unknowns));
      // if it has more than 2 unknowns, it's statically indeterminate
      if (unknowns > 2)
      {
        System.out.println(String.format("Node %c is unsolvable, breaking", character(ind)));
        break;
      }
      else if (unknowns == 2)
      {
        // find the two unknowns and build our matrix to solve for them
        double m[][] = new double[3][2]; // matrix
        int label[] = new int[2]; // members each force is in
        int i = 0;
        for (int j = 0; j < this.n; j++)
        {
          if (ind == j)
            continue;

          if(this.adj[ind][j] && !this.solved[ind][j])
          {
            double x = getX(j) - getX(ind);
            double y = getY(j) - getY(ind);
            double len = Math.sqrt(x * x + y * y);
            //System.out.println(String.format("%d:%d", ind, j));
            //System.out.println(String.format("%.3f,%.3f", x, y));
            m[i][0] = x / len;
            m[i][1] = y / len;
            m[2][i] = ext[ind][i];
            label[i] = j;
            i++;
          }
        }

        // solve the matrix
        double f[] = new double[2];
        f[0] = m[2][0]*m[1][1] - m[1][0]*m[2][1];
        f[0] /= m[0][0]*m[1][1] - m[0][1]*m[1][0];

        f[1] = m[2][1]*m[0][0] - m[0][1]*m[2][0];
        f[1] /= m[1][1]*m[0][0] - m[0][1]*m[1][0];

        for (i = 0; i < 2; i++)
        {
          // print + solve for weight + mark as solved
          handleForce(f[i], ind, label[i]);
          //System.out.println(String.format("Force %c%c - %.3f", character(ind), character(label[i]), f[i]));

          // apply the force we solved for to the adjacent
          // member's external force
          ext[label[i]][0] += m[i][0] * f[i];
          ext[label[i]][1] += m[i][1] * f[i];

          // add the adjaceny node to the queue again,
          // with an updated "unknown" count
          q.add(new Node(label[i], countUnknowns(label[i])));
        }
      }
      else if (unknowns == 1)
      {
        // 1 unknown edge, just use sum of Fx

        // find the index of the unknown edge
        int u = -1; // index of unknown force
        for (int k = 0; k < this.n; k++)
        {
          if (this.adj[ind][k] && !this.solved[ind][k])
          {
            u = k;
            break;
          }
        }
        if (u == -1)
        {
          System.out.println("COIULNDT FIND FORCE");
          break;
        }

        double x = getX(u) - getX(ind);
        double y = getY(u) - getY(ind);
        double len = Math.sqrt(x * x + y * y);
          //System.out.println(String.format("%c to %c: %.2f, %.2f (%.2f)", thisNode.label, thisAdj, x, y, len));

        double cos = x / len;
        double sin = y / len;
        //System.out.println(String.format("cosX: %.2f, cosY: %.2f", cos, sin));
        double force = ext[ind][0] / cos;
        if (cos == 0)
          force = 0;
        handleForce(force, ind, u);

        // apply the force we solved for to the adjacent
        // member's external force
        ext[u][0] += cos * force;
        ext[u][1] += sin * force;
      }
      else
      {
        return;
      }
    }
    printForces();
  }

  void printForces()
  {
    double totalVolume = 0;

    for (int i = 0; i < n; i++)
    {
      for (int j = 0; j <= i; j++)
      {
        if(forces.containsKey(character(i)+""+character(j)))
        {
          double force = forces.get(character(i)+""+character(j));
          double length = this.length[i][j];
          System.out.println("Member "+character(i)+""+character(j));
          System.out.println(String.format("\tLength: %.2f m", length));
          System.out.print(String.format("\tForce: %.2f kN", force));

          double area = -1;

          if (Math.abs(force) < 0.01)
          {
            System.out.println("\n\tZero-force member");
            // zero force members must be designed for 2% of the meximum force of any attached members
            double maxForce = 0.0;
            int maxSrc = '!';
            int  maxEnd = '!';
            for (int k = 0; k < n; k++)
            {
              Double iF = forces.get(character(i)+""+character(k));
              Double jF = forces.get(character(j)+""+character(k));
              if (iF != null && Math.abs(iF) > Math.abs(maxForce))
              {
                maxForce = iF;
                maxSrc = character(i);
                maxEnd = character(k);
              }
              if (jF != null && Math.abs(jF) > Math.abs(maxForce))
              {
                maxForce = jF;
                maxSrc = character(j);
                maxEnd = character(k);
              }
            }

            area = 0.02 * Math.abs(maxForce / this.allowableStress);
            System.out.println(String.format("\tMaximum attached member:  %c%c (%.3f kN)", maxSrc, maxEnd, maxForce));
          }
          else if (force > 0)
          {
            System.out.println(" (T)");
            area = Math.abs(force / this.allowableStress);
          }
          else
          {
            System.out.println(" (C)");
            double bucklingFactor = 10.0 / (length + 10.0);
            area = Math.abs(force / (this.allowableStress * bucklingFactor));
          }


          System.out.println(String.format("\tArea: %.2f mm^2", area));

          double volume = area * length;
          System.out.println(String.format("\tVolume: %.3f mm^3", volume));
          System.out.println(String.format("\tWeight: %.3f kN", volume * 0.000078));
          totalVolume += volume;
        }
      }
    }

    System.out.println(String.format("\nTotal Volume: %.3f mm^3", totalVolume));
    double totalWeight = totalVolume * 0.000078;
    System.out.println(String.format("Total Weight: %.3f kN", totalWeight));

  }

  double getX(int i)
  {
    return loc[i][0];
  }
  double getY(int i)
  {
    return loc[i][1];
  }


}
