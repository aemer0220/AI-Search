/*Alexandra Emerson*/
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.PriorityQueue;

public class Search {
	/*Declare and instantiate (only the default values) the variables*/
	public static String fileName;
	public static String initCityName;
	public static String goalCityName;
	public static String strat = "a-star"; //sets the default to a-star
	public static String heuristicFunct = "haversine"; //sets the default heuristic function to haversine;
	public static Boolean reached = true; //sets the default to true;
	public static int verbos = 0; //sets the default to 0;
	
	/*The below declarations are for the readFile method*/
	public BufferedReader reader;
	public String line = "";
	public List<String[]> data = new ArrayList<String[]>(); //data will be held in an ArrayList of String arrays consisting of each line in the file
	
	/*The below declarations are for the search strategies*/
	HashMap <String, Node> reachedTable; //maps a Node's state (string) to a Node;
	List<Node> expanded = new ArrayList<Node>();
	List<Node> added = new ArrayList<Node>();
	List<Node> childNodes = new ArrayList<Node>();
	Node root;
	int nodeCount = 0;
	int frontierCount = 0;
	long dur1;
	long dur2;

	public static void main(String[] args) {
		Search search = new Search();
		try {
			List<String> options = Arrays.asList(args); //converts args to a list so it's easier to work with
			int fileOptIndex = options.indexOf("-f");
			fileName = options.get(fileOptIndex + 1);
			int initCityIndex = options.indexOf("-i");
			initCityName = options.get(initCityIndex+1);
			int goalCityIndex = options.indexOf("-g");
			goalCityName = options.get(goalCityIndex+1);	
			//System.out.println(fileName); //test
			search.readFile(fileName); //Read the csv file
			Boolean citiesVerified = search.verifyCities();
			
			if (!citiesVerified) { //quit the program if either of specified cities was not found
				System.out.println("Specified initial city and/or goal city not found in file.");
				System.exit(-1);
			}
			/*The following if statements check for the optional values and reassigns variables as applicable*/
			if (options.contains("-s")) {
				int stratOptIndex = options.indexOf("-s");
				strat = options.get(stratOptIndex + 1).toLowerCase();
			}	
			if (options.contains("-h")) {
				int heurOptIndex = options.indexOf("-h");
				heuristicFunct = options.get(heurOptIndex + 1).toLowerCase();
			}
			if (options.contains("--noreached")) {
				reached = false;
			}
			if (options.contains("-v")) {
				int vOptIndex = options.indexOf("-v");
				verbos = Integer.parseInt(options.get(vOptIndex + 1));
			}
			
			search.doSearch();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	List<City> cities = new ArrayList<City>();
	List<String[]> distances = new ArrayList<String[]>();
	
	/** The readFile method takes in the fileName,
	 * reads one line at a time with a BufferedReader,
	 * and adds that line, as an array split by the
	 * commas, to an ArrayList
	 * 
	 * @param fileName
	 */
	private void readFile(String fileName) {
		String cityName;
		double latitude;
		double longitude;
		
		String cn1;
		String cn2;
		String distance;
		
		try {
			reader = new BufferedReader(new FileReader(fileName));
			while((line = reader.readLine())!= null) {
				data.add(line.split(",")); //data is a list of arrays
			}
			int listIndex = 1; //starts at 1 because we don't need to read the header
			String[] line = data.get(listIndex);
			
			//storing cities in an ArrayList of City objects
			while(!(line[0].contains("#"))) { //while we don't read the next header (distances)
				cityName = line[0].strip();
				latitude = Double.parseDouble(line[1].strip());
				longitude = Double.parseDouble(line[2].strip());
				City newCity = new City(cityName, latitude, longitude);
				cities.add(newCity);
		
				listIndex++;
				line = data.get(listIndex);
			}	
			listIndex++;
			while(listIndex < data.size()) {
				line = data.get(listIndex);
				cn1 = line[0].strip();
				cn2 = line[1].strip();
				distance = line[2].strip(); //in string format
				String[] dArray = {cn1, cn2, distance};
				distances.add(dArray);
				listIndex++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/** Verifies whether the initial and goal city names are contained in the
	 * specified file.
	 * 
	 * @return true if both cities are in our program; false if one or more is not
	 */
	private boolean verifyCities() {
		Boolean verifyIC = false;
		Boolean verifyGC = false;
		
		for(int i=0; i<cities.size(); i++) {
			City c = cities.get(i);
			if (c.cityName.equals(initCityName)) verifyIC=true;
			if (c.cityName.equals(goalCityName)) verifyGC=true;
		}
		if (verifyIC && verifyGC) {
			return true;
		}
		else{
			return false;
		}
	}
	
	/**
	 * doSearch() continues the main flow of our program.
	 * Splits off based on the strategy and then moves to
	 * printOutput method.
	 */
	private void doSearch() {
		dur1 = System.currentTimeMillis();
		solPath = new ArrayList<Node>();
		Node result;
		
		if (strat.equals("uniform")) {
			result = uniformCostSearch();
		}
		else if (strat.equals("greedy")) {
			result = greedyBestFirstSearch();
		}
		else { //do a-star
			result = aStarSearch();
			
		}
		dur2 = System.currentTimeMillis();
		printOutput(result);
	}
	
	/**
	 * expand expands parent Node n according
	 * to the specified search strategy.
	 * @param n (parent)
	 * @return a list of n's children
	 */
	public List<Node> expand(Node n) {
		Node child;
		ArrayList<Node> children = new ArrayList<>();
		String[] line;
		for(int i=0;i<distances.size(); i++) {
			line = distances.get(i);
			if (line[0].equals(n.currentCity)) {
				double pathCost = Double.parseDouble(line[2]) + n.pathCost;
				child = new Node(line[1]);
				child.pathCost = pathCost;
				child.heuristic = heuristicFunct.equals("haversine") ?  haversineEval(line[1]) : euclideanEval(line[1]);
				child.parent = n;
				childNodes.add(child);
				children.add(child);
			}
			else if(line[1].equals(n.currentCity)) { //accounting for going both ways in the columns
				double pathCost = Double.parseDouble(line[2]) + n.pathCost;
				child = new Node(line[0]);
				child.pathCost = pathCost; //g(n)
				child.heuristic = heuristicFunct.equals("haversine") ?  haversineEval(line[0]) : euclideanEval(line[0]);	
				child.parent = n;
				childNodes.add(child);
				children.add(child);	
			}		
		}
		return children;
	}
	
	/**
	 * uniformCostSearch() does UCS with a costFunct of f(n) = g(n)
	 * which is only based on the distances between cities,
	 * as specified in the file.
	 * @return final path node (with parent references), or null (failure)
	 */
	private Node uniformCostSearch() {
		root = new Node(initCityName);
		root.heuristic = heuristicFunct.equals("haversine") ?  haversineEval(initCityName) : euclideanEval(initCityName);
		root.costFunct = 0;
		root.pathCost = 0;
		nodeCount++;
		PriorityQueue<Node> frontier = new PriorityQueue<Node>();
		frontier.add(root);
		added.add(root);
		frontierCount++;
		if (reached == true) reachedTable = new HashMap<>();
		while (!frontier.isEmpty()) {
			Node n = frontier.remove();
			frontierCount--;
			if (n.currentCity.equals(goalCityName)) return n;
			expanded.add(n);
			for (Node child : expand(n)) {
				String childState = child.currentCity;
				child.costFunct = child.pathCost; 	//f(n) = g(n)
				if(reached) {
					if ((!reachedTable.containsKey(childState)) || child.costFunct < reachedTable.get(childState).costFunct) {
						reachedTable.put(childState, child);
						frontier.add(child);
						added.add(child);
						frontierCount++;
						nodeCount++;
					}
				}
				else { //no reached table
					frontier.add(child); //adds to frontier regardless
					added.add(child);
					frontierCount++;
					nodeCount++;
				}
			}
		}	
		return null; //only reaches here if it fails
	}
	

	/**
	 * greedyBestFirstSearch() does GBFS with a costFunct of f(n) = h(n)
	 * which is based on the specified heuristic function
	 * as specified (or not) in the command line arguments.
	 * @return final path node (with parent references), or null (failure)
	 */
	private Node greedyBestFirstSearch() {
		PriorityQueue<Node> frontier = new PriorityQueue<Node>();
		root = new Node(initCityName);
		root.pathCost = 0;
		root.heuristic = heuristicFunct.equals("haversine") ?  haversineEval(initCityName) : euclideanEval(initCityName);
		root.costFunct = root.heuristic;
		nodeCount++;
		frontier.add(root);
		frontierCount++;
		added.add(root);
		if (reached) reachedTable = new HashMap<>();
		while (!frontier.isEmpty()) {
			Node n = frontier.remove();
			frontierCount--;
			if (reached) {
				reachedTable.put(initCityName, n);
			}			
			expanded.add(n);
			if (n.currentCity.equals(goalCityName)) {
				nodeCount++;
				n.pathCost = n.pathCost + n.parent.pathCost;
				return n;
			}
			for (Node child : expand(n)) {
				child.costFunct = child.heuristic;//f(n) = h(n)		
				String childState = child.currentCity;
				if(reached) {
					if ((!reachedTable.containsKey(childState)) && !frontier.contains(child)) {
						frontier.add(child);
						frontierCount++;
						added.add(child);
					}
				}	
				else { //reached=false
					frontier.add(child); //adds to frontier regardless
					frontierCount++;
					added.add(child);
				}	
			}
		}
		return null; //only reaches here if it fails
	}
	
	/**
	 * aStarSearch() does A* Search with a costFunct of f(n) = g(n) + h(n)
	 * which is based on the specified heuristic function
	 * as specified (or not) in the command line arguments.
	 * @return final path node (with parent references), or null (failure)
	 */
	private Node aStarSearch() {
		PriorityQueue<Node> frontier = new PriorityQueue<Node>();
		root = new Node(initCityName);
		root.pathCost = 0;
		root.heuristic = heuristicFunct.equals("haversine") ?  haversineEval(initCityName) : euclideanEval(initCityName);
		root.costFunct = root.pathCost + root.heuristic;
		frontier.add(root);
		frontierCount++;
		nodeCount++;
		added.add(root);
		if (reached) reachedTable = new HashMap<>();
		while (!frontier.isEmpty()) {
			Node n = frontier.remove();
			frontierCount--;
			if (reached) {
				reachedTable.put(initCityName, n);
			}
			expanded.add(n);
			if (n.currentCity.equals(goalCityName)) {
				n.costFunct = n.pathCost + n.heuristic;
				nodeCount++;
				return n;
			}
			nodeCount++;
			for (Node child : expand(n)) {
				child.costFunct = child.pathCost + child.heuristic;
				String childState = child.currentCity;
				if(reached) {
					if ((!reachedTable.containsKey(childState)) && !frontier.contains(child)) {
						frontier.add(child);
						frontierCount++;
						added.add(child);
					}
				}
				else {
					frontier.add(child);
					frontierCount++;
					added.add(child);
				}	
			}
		}
		return null; //only reaches here if it fails
	}
	
	public final double R = 3958.8; //Radius of Earth in miles
	
	/** Utilizes the Haversine formula to determine the
	 * arc length between the specified city and the goal
	 * city.
	 * 
	 * @param city name
	 * @return arc length (result)
	 */
	private double haversineEval(String city) {
		double lon1 = findLon(city) * (Math.PI/180);
		double lon2 = findLon(goalCityName) * (Math.PI/180);
		double changeLon = lon2-lon1; //in radians
		
		double lat1 = findLat(city)* (Math.PI/180);
		double lat2 = findLat(goalCityName) * (Math.PI/180);
		double changeLat = lat2-lat1; //in radians
		
		double a = (Math.pow(Math.sin(changeLat/2), 2)) + Math.cos(lat1)*Math.cos(lat2) * (Math.pow(Math.sin(changeLon/2), 2));
		double c = 2*Math.atan2(Math.sqrt(a), Math.sqrt((1-a)));
		
		double result = R*c;
		return result;
	}
	
	/** Utilizes the Euclidean distance formula to determine the
	 * arc length between the specified city and the goal
	 * city.
	 * Euclidean distance = squareRoot((lat2-lat1)^2 + (lon2-lon2)^2)
	 * 
	 * @param city name
	 * @return Euclidean distance
	 */
	private double euclideanEval(String city) {
		
		double lon1 = findLon(city);
		double lon2 = findLon(goalCityName);
		double changeLon = lon2-lon1;
		
		double lat1 = findLat(city);
		double lat2 = findLat(goalCityName);
		double changeLat = lat2-lat1;
		
		double eucDist = Math.sqrt(Math.pow(changeLat, 2) + Math.pow(changeLon, 2));
		return eucDist;
	}
	
	/** Finds the longitude of the specified city 
	 * from the ArrayList of cities
	 * 
	 * @param city name
	 * @return Longitude (still in degrees)
	 */
	private double findLon(String city) {	
		for(int i=0; i<cities.size();i++) {
			City c = cities.get(i);
			if (c.cityName.equals(city)) {
				return c.longitude;
			}
		}
		return 0; //a dummy return value
	}
	
	/** Finds the latitude of the specified city 
	 * from the ArrayList of cities
	 * 
	 * @param city name
	 * @return Latitude (still in degrees)
	 */
	private double findLat(String city) {
		for(int i=0; i<cities.size();i++) {
			City c = cities.get(i);
			if (c.cityName.equals(city)) {
				return c.latitude;
			}
		}
		return 0; //a dummy return value
	}
	
	/*Solution path*/
	public List<Node> solPathReversed;
	public List<Node> solPath;
	
	/*Print function*/
	private void printOutput(Node result) {
		String searchStrat;
		if(strat.equals("a-star")) searchStrat = "A-star";
		else if (strat.equals("greedy")) searchStrat = "Greedy Best First";
		else searchStrat = "Uniform Cost";
		
		if(result==null) {
			System.out.print("NO PATH FOUND");
		}
		else {
			DecimalFormat df = new DecimalFormat("#.#");
			long duration = dur2-dur1;
			solPathReversed = new ArrayList<Node>(); //is later reversed after we're done moving backwards
			Node temp = result;
			while (temp.parent!=null) {
				solPathReversed.add(temp);
				temp = temp.parent;
			}
			solPathReversed.add(temp);
			solPath = solPathReversed.reversed();
		
			if (verbos == 1) {
			System.out.println("* Reading data from [" + fileName + "]");
			System.out.println("* Number of cities: " + cities.size());
			System.out.println("* Searching for path from " + initCityName + " to " + goalCityName + " using " + searchStrat + " Search");
			System.out.print("* Goal found : " + goalCityName + " (p-> " + result.parent.currentCity + ") ");
			System.out.println("[f= " + df.format(result.costFunct) + "; g= " + df.format(result.pathCost) + "; h= " + df.format(result.heuristic) + "]");
			System.out.println("* Search took " + duration + "ms");
			System.out.println();
			}
			else if (verbos == 2) {
				String parent;
				System.out.println("* Reading data from [" + fileName + "]");
				System.out.println("* Number of cities: " + cities.size());
				System.out.println("* Searching for path from " + initCityName + " to " + goalCityName + " using " + searchStrat + " Search");
			
				int j;
				for(j = 0; j <expanded.size(); j++) {
					Node temp1 = expanded.get(j);
					if (temp1.parent == null) {
						parent = "null";
					}
					else {
						parent = temp1.parent.currentCity;
					}
					System.out.print("Expanding  : " + temp1.currentCity + "   p-> " + parent);
					System.out.println(" f= " + df.format(temp1.costFunct) + "; g= " + df.format(temp1.pathCost) + "; h= " + df.format(temp1.heuristic));
				}
				
				System.out.print("* Goal found : " + goalCityName + " (p-> " + result.parent.currentCity + ") ");
				System.out.println("[f= " + df.format(result.costFunct) + "; g= " + df.format(result.pathCost) + "; h= " + df.format(result.heuristic) + "]");
				System.out.println("* Search took " + duration + "ms");
				System.out.println();
			}
			else if (verbos == 3) {
				String parent;
				System.out.println("* Reading data from [" + fileName + "]");
				System.out.println("* Number of cities: " + cities.size());
				System.out.println("* Searching for path from " + initCityName + " to " + goalCityName + " using " + searchStrat + " Search");
			
				int j;
				for(j = 0; j <expanded.size(); j++) {
					Node temp1 = expanded.get(j);
					if (temp1.parent == null) {
						parent = "null";
					}
					else {
						parent = temp1.parent.currentCity;
					}
					System.out.print("Expanding   : " + temp1.currentCity + "   (p-> " + parent);
					System.out.println(" [f= " + df.format(temp1.costFunct) + "; g= " + df.format(temp1.pathCost) + "; h= " + df.format(temp1.heuristic)+ "]");
					for(Node child : findChildren(temp1)) {
						if (added.contains(child)) {
							System.out.print("   Adding   : " + child.currentCity + "   (p-> " + child.parent.currentCity);
							System.out.println(" [f= " + df.format(child.costFunct) + "; g= " + df.format(child.pathCost) + "; h= " + df.format(child.heuristic)+ "]");
						}
						else {
							System.out.print("   NOT adding: " + child.currentCity + "   (p-> " + child.parent.currentCity);
							System.out.println("   [f= " + df.format(child.costFunct) + "; g= " + df.format(child.pathCost) + "; h= " + df.format(child.heuristic) + "]");
						}
					}
				}
				
				System.out.print("* Goal found : " + goalCityName + " (p-> " + result.parent.currentCity + ") ");
				System.out.println("[f= " + df.format(result.costFunct) + "; g= " + df.format(result.pathCost) + "; h= " + df.format(result.heuristic) + "]");
				System.out.println("* Search took " + duration + "ms");
				System.out.println();
			}
			
			System.out.print("Route found: ");
			int i;
			for(i = 0; i <solPath.size()-1; i++) {
				System.out.print(solPath.get(i).currentCity + "-->");
			}
			System.out.println(solPath.get(i).currentCity);
			System.out.println("Distance: " + result.costFunct);
			System.out.println();
			System.out.println("Total nodes generated      : " + nodeCount);
			System.out.println("Nodes remaining on frontier: " + frontierCount);
		}
	}
	
	/**
	 * takes in a parent node n and returns its
	 * children if there is a match in the childNodes
	 * ArrayList
	 * 
	 * @param Node n
	 * @return ArrayList<Node>
	 */
	public ArrayList<Node> findChildren(Node n){
		ArrayList<Node> children = new ArrayList<Node>();
		Node child;
		for(int i=0;i<childNodes.size(); i++) {
			child = childNodes.get(i);
			if (child.parent.equals(n)) {
				children.add(child);
			}
		}
		
		return children;
		
	}
}
