/*Alexandra Emerson*/
public class Node implements Comparable<Node> {
	public String currentCity; //think about changing this to data type city
	public double pathCost; //g(n)
	public Node parent;
	public double costFunct; //f(n) + g(n)
	public double heuristic; //h(n)
	
	public Node(String cc) {
		currentCity = cc;
	}

	public boolean equals(Node other) {
		return this.costFunct == other.costFunct;
	}
	
	public int compareTo(Node other) {
		if(this.equals(other)) {
			return 0;
		}
		
		else if (this.costFunct > other.costFunct) {
			return 1;
		}
		
		else {
			return -1;
		}
	}
}
 

