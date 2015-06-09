/* package whatever; // don't place package name! */

import java.util.*;
import java.lang.*;
import java.io.*;

/*
* Disclaimer. Everything here is public. Don't change data you are not
* supposed to change. Please be respectful of data, they have feelings too.
*/

/**
 * Represent a connection between 2 nodes.
 * Can be interpreted as a one-way or two-way link.
 */
class DijkstraConnection
{
	public int nodeA;
	public int nodeB;
	public int distance;
    
    public boolean hasNodes(int node1, int node2) {
        return (nodeA == node1 && nodeB == node2) || 
               (nodeB == node1 && nodeA == node2);
    }
}

/**
 * Composed by a collection of Connections
 */
class DijkstraGraph
{
    // When there is no link between 2 nodes (infinite distance)
    public static int NO_CONNECTION = -1;
    
	public int nextNodeId = 1;
	public ArrayList<DijkstraConnection> connections = new ArrayList<DijkstraConnection>();
    public ArrayList<Integer> nodeIds = new ArrayList<Integer>();
	
	public int createNode() {
		int requestedNodeId = nextNodeId;
        nodeIds.add(requestedNodeId); // Autoboxing. Ugh.
		nextNodeId += 1;
		return requestedNodeId;
	}
	public void createLink(int nodeA, int nodeB, int distance) {
		// TODO: Check nodeA and nodeB are valid
		DijkstraConnection conn = new DijkstraConnection();
		conn.nodeA = nodeA;
		conn.nodeB = nodeB;
		conn.distance = distance;
		connections.add(conn);
	}
    
    // Helper methods used by the walker. 
    // Lazy evaluation to avoid bookkeeping (and because I don't want to code that now).
    public LinkedList<Integer> getNeighbors(int nodeId)
    {
        // So much garbage collection *ugh*
        LinkedList<Integer> neighbors = new LinkedList<Integer>();
        
        // Iterate over all connections
        for (DijkstraConnection conn : connections) {
            if (conn.nodeA == nodeId) {
                neighbors.add(conn.nodeB);
            }
            else if (conn.nodeB == nodeId) {
                neighbors.add(conn.nodeA);
            }
        }
        
        return neighbors;
    }
   
    /**
    * Again, lazy evaluation here as well. We could cache the 
    * result, but that would require additional data structures
    * to maintain. Meh.
    */
    public int getDistance(int node1, int node2) {
        
        for (DijkstraConnection conn : connections) {
            if (conn.hasNodes(node1, node2)) {
                return conn.distance;
            }
        }
        
        // If no connection return -1
        return NO_CONNECTION;
    }
    
    /**
    * I sometime put braces on the same line, sometimes new line. 
    * I've clearly have lost control of my life (and my code).
    */
    public ArrayList<Integer> getNodeIds() 
    {
        // If this were C++ I would be returning a const ArrayList. Oh well.
        // Are there const functions in this language anyway??
        
        return nodeIds; // Not even a shallow copy. We trust our consumers.
    }
}

/**
 * Created by Walker class as a result for the shortest
 * path between 2 nodes
 */
class DijkstraPath
{
	public ArrayList<Integer> nodes = new ArrayList<Integer>();
    
    public int startNodeId;
    public int endNodeId;
    public int fullDistance;
    
    public void printResult() {
        if (fullDistance == -1) {
            System.out.println("There was not path between nodes " + startNodeId + " and " + endNodeId + ".\n");
        } else {
            System.out.println("Shortest distance between nodes " + startNodeId + " and " + endNodeId + " is: " + fullDistance);
            // I could use a StringBuilder, but who cares really? Surely Dijkstra doesn't.
            System.out.print("Full path is composed of nodes: [" );
            for (int nodeId : nodes)
            {
                if (nodeId == endNodeId) {
                    // last node, no extra comma
                    System.out.print(nodeId);
                } else {
                    // non-last node, add comma
                    System.out.print(nodeId + ", ");
                }
            }
            System.out.println("]\n");
        }
    }
    
    public static DijkstraPath createNoPath(int startNode, int endNode)
    {
        DijkstraPath noPath = new DijkstraPath();
        noPath.startNodeId = startNode;
        noPath.endNodeId = endNode;
        noPath.fullDistance = -1;
        return noPath;
    }
}

/**
 * Calculates shortest path given a DijkstraGraph
 */
class DijkstraWalker
{
	// Inner class used for bookkeeping
	class VisitData
	{
		int distanceSoFar;
        int currentNodeId;
        int fromNodeId;
	}
	
	public DijkstraPath getShortestPath(DijkstraGraph graph, int nodeA, int nodeB) 
	{
        // Initialize each node as "not visited"
        HashMap<Integer, VisitData> bookkeeping = new HashMap();
        ArrayList<Integer> allNodeIds = graph.getNodeIds();
        for (int id : allNodeIds)
        {
            VisitData visitData = new VisitData();
            visitData.distanceSoFar = 0;
            visitData.currentNodeId = id; // This value must not change
            visitData.fromNodeId = graph.NO_CONNECTION;
            bookkeeping.put(id, visitData);
        }
        
        // First node should have a distanceSoFar of 0
        VisitData dd = bookkeeping.get(nodeA);
        dd.distanceSoFar = 0;
        
        // Initialize queue with nodeA
		LinkedList<Integer> queue = new LinkedList<Integer>();
		queue.add(nodeA);
		
        // Iterative approach because recursion is for losers (not really)
		while (!queue.isEmpty())
		{
			int currNode = queue.removeFirst();
            VisitData currVisit = bookkeeping.get(currNode);
            LinkedList<Integer> neighbors = graph.getNeighbors(currNode);
            
            for (int nextNodeId : neighbors)
            {
                int distance = graph.getDistance(currNode, nextNodeId);
                VisitData nextData = bookkeeping.get(nextNodeId);
                
                boolean hasBeenVisited = (nextData.fromNodeId != graph.NO_CONNECTION);
                boolean isShorterPath = (currVisit.distanceSoFar + distance) < nextData.distanceSoFar;
                
                if (!hasBeenVisited || isShorterPath)
                {
                    // Override current shortest distance
                    nextData.distanceSoFar = currVisit.distanceSoFar + distance;
                    nextData.fromNodeId = currNode;
                    
                    // Enqueue nextId node
                    queue.addLast(nextNodeId);
                }
            }
		}
        
        // Queue is empty now *sad panda*, time to check whether the end node was reached
        VisitData endStep = bookkeeping.get(nodeB);
        if (endStep.fromNodeId == graph.NO_CONNECTION) {
            // Oh well, there was no connection between first and last node
            return DijkstraPath.createNoPath(nodeA, nodeB);
        }
        
        // Else, connection found!
        DijkstraPath resultPath = new DijkstraPath();
        
        resultPath.startNodeId = nodeA;
        resultPath.endNodeId = nodeB;
        resultPath.fullDistance = endStep.distanceSoFar;
        resultPath.nodes.add(endStep.currentNodeId);
        
        while (endStep != null)
        {
            resultPath.nodes.add(endStep.fromNodeId);
            if (endStep.fromNodeId == nodeA) {
                endStep = null;
            } else {
                endStep = bookkeeping.get(endStep.fromNodeId);
            }
        }
        
        // Reverse resultPath.nodes before leaving
        Collections.reverse(resultPath.nodes);
        
        return resultPath;
	}
}

/* Name of the class has to be "Main" only if the class is public. */
class Ideone
{
	public static void main (String[] args) throws java.lang.Exception
	{
		// Create nodes
        // Visual representation of the graph being built: http://snag.gy/YwhCr.jpg
		System.out.println("1. Create nodes");
		DijkstraGraph graph = new DijkstraGraph();
		int node1 = graph.createNode();
		int node2 = graph.createNode();
		int node3 = graph.createNode();
		int node4 = graph.createNode();
		int node5 = graph.createNode();
		int node6 = graph.createNode();
		int node7 = graph.createNode();
        // These next nodes will not connect to the previous ones
        int node8 = graph.createNode();
        int node9 = graph.createNode();
        int nodeA = graph.createNode();
        
		
		// Create links (nodeA, nodeB, distance)
		System.out.println("2. Create connections");
		graph.createLink(node1, node2, 20);
		graph.createLink(node1, node6, 99);
		graph.createLink(node2, node3, 20);
		graph.createLink(node2, node5, 10);
		graph.createLink(node3, node4, 30);
		graph.createLink(node5, node4, 30);
		graph.createLink(node4, node6,  5);
		graph.createLink(node1, node7,  5);
		graph.createLink(node7, node2,  5);
        // Connect the "extra" nodes
        graph.createLink(node8, node9,  21);
        graph.createLink(node9, nodeA,  21);
		
		// Walk the graph and get the shortest distance
		System.out.println("3. Walk the graph");
		DijkstraWalker walker = new DijkstraWalker();
		
        // Result should be [1, 7, 2, 5, 4, 6]. Total distance: 55
        DijkstraPath path = walker.getShortestPath(graph, node1, node6);
        path.printResult();
        
        // Result should be [6, 4, 5, 2, 7, 1]. Total distance: 55
        path = walker.getShortestPath(graph, node6, node1);
        path.printResult();
        
        // Result should be [3, 2, 5]. Total distance: 30
        path = walker.getShortestPath(graph, node3, node5);
        path.printResult();
        
        // Result should be [2, 5, 4, 6]. Total distance: 45
        path = walker.getShortestPath(graph, node2, node6);
        path.printResult();
        
        // There's no path between these 2 nodes
        path = walker.getShortestPath(graph, node1, nodeA);
        path.printResult();
        
        // Result should be [8, 9, 10]. Total distance: 42
        path = walker.getShortestPath(graph, node8, nodeA);
        path.printResult();
        
		System.out.println("THE END.");
        System.out.println("So long, and thanks for all the fish!");
	}
}