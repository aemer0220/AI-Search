# AI-Search
This program reads in an input csv file describing a set of cities and then searches for paths between two user-specified cities using various search strategies (Uninformed and Informed)

### How to run from the Command Prompt (terminal)

1. Download the project folder
2. Open your Command Prompt (terminal)
3. cd to project's src file containing the .java files
4. type (without quotes) 'javac Search.java
5. Next, type (without quotes) 'java Search' followed by your command line arguments. See "Allowed Command Line Arguments" for the required and optional arguments

### Allowed Command Line Arguments
#### The -f, -i, and -g options (with arguments) are required; the remaining ones are optional
* -f <FILENAME>: Reads city data from the text file named <FILENAME> (specified as a String);
* -i <STRING>: Specifies the initial city as a String; multi-word city names must be enclosed with quotation marks
* -g <STRING>: Specifies the goal city as a String; multi-word city names must be enclosed
* -s <STRING>: Specifies the search strategy to be used; <STRING> should be one of:
  * a-star: Use the A* search strategy (Default if -s is not provided)
  * greedy: Use the greedy best-first search strategy
  * uniform: Use the uniform-cost search strategy
* -h <STRING>: Specifies the heuristic function to be used (if applicable); <STRING> should be
one of:
  * haversine: Use the Haversine formula (Default if -h is not provided)
  * euclidean: Use the Euclidean distance
If the -h argument is not provided, then the Haversine formula is used by default;
* --no-reached: Disables the use of a reached table in the search algorithm, resulting in a search with redundant paths (including cycles). If this argument is not provided,
then the search algorithm uses a reached table for removing redundant paths.
* -v <INTEGER>: Specifies a verbosity level (0-3) indicating how much output the program should produce (Default is 0 if -v is not provided)
