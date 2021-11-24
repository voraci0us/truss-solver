public class Node implements Comparable<Node>
{
  int unknown;
  int index;

  public Node(int index, int unknown)
  {
    this.index = index;
    this.unknown = unknown;
  }

  public int compareTo(Node n)
  {
    return this.unknown - n.unknown;
  }
}
