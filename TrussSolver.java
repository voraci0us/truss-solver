import java.util.*;
import java.io.*;

class Truss
{
  // adjacency list
  ArrayList<LinkedList<Character>> next;
  // adjanency matrix for storing the width of each member
  double[][] width;
  // x, y coords of each node
  int[][] loc;
  // x, y components of external forces
  double[][] ext;
  // # of nodes
  int n;
  // # of edges
  int e;

  boolean solved[][];

  public Truss(int n)
  {
      this.n = n;
      this.e = 0;
      // declare adjacency list
      this.next = new ArrayList<LinkedList<Character>>(n);
      for (int i = 0; i < n; i++)
        this.next.add(new LinkedList<Character>());
      loc = new int[n][2];
      ext = new double[n][2];
      for (int i = 0; i < n; i++)
      {
        ext[i][0] = 0;
        ext[i][1] = 0;
      }
      solved = new boolean[n][n];
      width = new double[n][n];
      for (int i = 0; i < n; i++)
      {
        for (int j = 0; j < n; j++)
        {
          solved[i][j] = false;
          width[i][j] = -1;
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

  public void addNode(char c, int x, int y)
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

  public void addEdge(char a, char b, double width)
  {
    this.next.get(index(a)).add(b);
    this.next.get(index(b)).add(a);
    this.width[index(a)][index(b)] = width;
    this.width[index(b)][index(a)] = width;
    this.e++;
  }

  public void addExt(char c, double x, double y)
  {
    int index = index(c);
    this.ext[index][0] = x;
    this.ext[index][1] = y;
  }

  public void print()
  {
    System.out.println("Given a truss defined by the following:");
    for (int i = 0; i < n; i++)
    {
      System.out.print(String.format("%c (%d, %d): ", character(i), loc[i][0], loc[1][1]));
      LinkedList<Character> thisList = this.next.get(i);
      for (int j = 0; j < thisList.size(); j++)
      {
        System.out.print(thisList.get(j));
        if (j != thisList.size() - 1)
          System.out.print(" -> ");
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

  public void solve()
  {
    System.out.println("Forces in members:");
    // make a min queue of joints by unknown edge count
    // initially, this count is just the size of the adjacency list
    PriorityQueue<Node> q = new PriorityQueue<Node>();
    int unsolvedEdges = this.e;
    for (int i = 0; i < this.n; i++)
    {
      Node tmp = new Node(character(i));
      for (int j = 0; j < this.next.get(i).size(); j++)
        tmp.addAdj(this.next.get(i).get(j));
      q.add(tmp);
    }

    for (int cnt = 0; cnt < this.n; cnt++)
    {
      // pop the top item off min queue
      // if it has more than 2 unknowns, it's statically indeterminate
      double m[][] = new double[3][2];
      Node thisNode = q.poll();
      //thisNode.print();

      if (thisNode.adj.size() > 2)
      {
        System.out.println(String.format("Node %c is unsolvable, breaking", thisNode.label));
        break;
      }
      else if (thisNode.adj.size() == 2)
      {
        for (int i = 0; i < thisNode.adj.size(); i++)
        {
          char thisAdj = thisNode.adj.get(i);
          double x = getX(thisAdj) - getX(thisNode.label);
          double y = getY(thisAdj) - getY(thisNode.label);
          double len = Math.sqrt(x * x + y * y);
          //System.out.println(String.format("%c to %c: %.2f, %.2f (%.2f)", thisNode.label, thisAdj, x, y, len));

          double cos = x / len;
          double sin = y / len;
          //System.out.println(String.format("cosX: %.2f, cosY: %.2f", cos, sin));

          if (i >= 2)
            break;

          m[i][0] = cos;
          m[i][1] = sin;
          m[2][i] = ext[index(thisNode.label)][i];
        }

        /**System.out.println("\nmaxtix:");
        for (int j = 0; j < 2; j++)
        {
          for (int i = 0; i < 3; i++)
          {
            System.out.print(m[i][j]+" ");
            if (i == 2)
              System.out.println("");
          }
        }
        System.out.println("");**/

        double f[] = new double[2];
        f[0] = m[2][0]*m[1][1] - m[1][0]*m[2][1];
        f[0] /= m[0][0]*m[1][1] - m[0][1]*m[1][0];

        f[1] = m[2][1]*m[0][0] - m[0][1]*m[2][0];
        f[1] /= m[1][1]*m[0][0] - m[0][1]*m[1][0];


        for (int i = 0; i < 2; i++)
        {
          int adjNode = index(thisNode.adj.get(i));
          if(this.solved[index(thisNode.label)][adjNode])
            continue;

          double formattedForce = f[i] * -1;
          if (Math.abs(formattedForce) < 0.001)
            formattedForce = 0;
          System.out.println(String.format("force %c%c : %.3f", thisNode.label, character(adjNode), formattedForce));
          ext[adjNode][0] += m[i][0] * f[i];
          ext[adjNode][1] += m[i][1] * f[i];

          Node tmp = new Node(character(adjNode));
          for (int j = 0; j < next.get(adjNode).size(); j++)
          {
            // add all joints except this one
            char c = next.get(adjNode).get(j);
            if(c != thisNode.label)
              tmp.addAdj(c);
          }
          q.add(tmp);

          this.solved[index(thisNode.label)][adjNode] = true;
          this.solved[adjNode][index(thisNode.label)] = true;
        }

        unsolvedEdges -= 2;


        // when we solve for a force, sub it from ext for the connected node
      }
      else
      {
        // 1 unknown edge, just use sum of Fx
        char thisAdj = thisNode.adj.get(0);
        double x = getX(thisAdj) - getX(thisNode.label);
        double y = getY(thisAdj) - getY(thisNode.label);
        double len = Math.sqrt(x * x + y * y);
          //System.out.println(String.format("%c to %c: %.2f, %.2f (%.2f)", thisNode.label, thisAdj, x, y, len));

        double cos = x / len;
        //System.out.println(String.format("cosX: %.2f, cosY: %.2f", cos, sin));
        double constant = ext[index(thisNode.label)][0];

        double force = constant / cos;

        double formattedForce = force * -1;
        if (Math.abs(formattedForce) < 0.001)
            formattedForce = 0;
        System.out.println(String.format("force %c%c : %.3f", thisNode.label, thisAdj, formattedForce));
        this.solved[index(thisNode.label)][index(thisAdj)] = true;
        this.solved[index(thisAdj)][index(thisNode.label)] = true;

        /**System.out.println("\nmaxtix:");
        for (int j = 0; j < 2; j++)
        {
          for (int i = 0; i < 3; i++)
          {
            System.out.print(m[i][j]+" ");
            if (i == 2)
              System.out.println("");
          }
        }
        System.out.println("");**/

        unsolvedEdges -= 1;

      }
    }
  }
  double getX(char c)
  {
    return loc[index(c)][0];
  }
  double getY(char c)
  {
    return loc[index(c)][1];
  }

}

class Node implements Comparable<Node>
{
    char label;
    ArrayList<Character> adj;

    public Node(char c)
    {
      this.label = c;
      this.adj = new ArrayList<Character>();
    }
    public void addAdj(char c)
    {
      this.adj.add(c);
    }
    public void delAdj(char c)
    {
      this.adj.remove(c);
    }
    public int compareTo(Node n)
    {
      return this.adj.size() - n.adj.size();
    }
    public void print()
    {
      System.out.print("\n"+this.label+": ");
      for (int i = 0; i < this.adj.size(); i++)
      {
        System.out.print(this.adj.get(i));
        if (i != this.adj.size() - 1)
          System.out.print(" -> ");
      }
      System.out.println("");
    }
}

public class TrussSolver
{

  public static void main (String[] args)
  {

    /** // read input + store in adjacency list
    Truss myTruss = new Truss(5);

    // add nodes
    myTruss.addNode('A', 0, 0);
    myTruss.addNode('B', 30, 0);
    myTruss.addNode('C', 5, 10);
    myTruss.addNode('D', 25, 10);
    myTruss.addNode('E', 15, 0);

    // add members between nodes
    myTruss.addEdge('A', 'C');
    myTruss.addEdge('A', 'E');
    myTruss.addEdge('C', 'E');
    myTruss.addEdge('C', 'D');
    myTruss.addEdge('D', 'B');
    myTruss.addEdge('B', 'E');
    myTruss.addEdge('D', 'E');

    // add external forces
    myTruss.addExt('A', 0, 10);
    myTruss.addExt('B', 0, 10);
    myTruss.addExt('E', 0, -20);

    myTruss.print();
    myTruss.solve();**/

    Truss myTruss = new Truss(6);
    myTruss.addNode('A', 0, 0);
    myTruss.addNode('B', 9, 0);
    myTruss.addNode('C', 3, 0);
    myTruss.addNode('D', 3, 3);
    myTruss.addNode('E', 9, 3);
    myTruss.addNode('F', 12, 3);

    myTruss.addEdge('A', 'D', 2);
    myTruss.addEdge('A', 'C', 2);
    myTruss.addEdge('B', 'C', 2);
    myTruss.addEdge('B', 'D', 2);
    myTruss.addEdge('B', 'E', 2);
    myTruss.addEdge('B', 'F', 2);
    myTruss.addEdge('E', 'D', 2);
    myTruss.addEdge('E', 'F', 2);

    myTruss.addExt('D', 0, -10);
    myTruss.addExt('A', 0, 6.66666667);
    myTruss.addExt('B', 0, 3.3333333);

    myTruss.print();
    myTruss.solve();

    //System.out.println(index('C'));

    /**Truss myTruss = new Truss(4);

    myTruss.addNode('A', 0, 0);
    myTruss.addNode('B', 30, 0);
    myTruss.addNode('C', 15, 15);
    myTruss.addNode('D', 15, 0);

    myTruss.addEdge('A', 'C');
    myTruss.addEdge('A', 'D');
    myTruss.addEdge('B', 'C');
    myTruss.addEdge('B', 'D');
    myTruss.addEdge('D', 'C');

    myTruss.addExt('D', 0, -20);
    myTruss.addExt('A', 0, 10);
    myTruss.addExt('B', 0, 10);

    //myTruss.print();
    System.out.println("");
    myTruss.solve();**/



  }
}
