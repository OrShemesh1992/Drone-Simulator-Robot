import java.util.Iterator;
import java.util.Set;

import javax.swing.JFrame;

import org.jgrapht.*;
import org.jgrapht.graph.*;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.alg.spanning.*;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.swing.mxGraphComponent;

public class Graph {

	DefaultDirectedGraph<Point, DefaultEdge> g;
    private DefaultWeightedEdge e1;
    
    public Graph() {
    	g = new DefaultDirectedGraph<Point, DefaultEdge>(DefaultEdge.class);
    }
    public void addVertex(Point name) {
        Point last_vertex = null;
        Set<Point> all = g.vertexSet();
        
        if(all.size() > 0) {
        	last_vertex = getLastElement(all);
        }
        g.addVertex(name);
        if(last_vertex != null) 
        	g.addEdge(last_vertex, name);
        //graph.addVertex(name);
    }
    
    public Point getLastElement(Set<Point> c) {
    	Point last = null;
    	if(c.size() > 0) {
    		for(Point x : c) {
    			last = x;
    		}
    	}
        return last;
    }
    public void addEdge(Point v1,Point v2) {
        g.addEdge(v1, v2);
    }


    public DefaultDirectedGraph<Point, DefaultEdge> getGraph() {
        return g;
    }
    
    public String getOutput() {
    	return g.toString();
    }
    
    public void drawGraph() {
    	JFrame new_window = new JFrame();
    	new_window.setSize(500,500);
    	new_window.setTitle("Graph Viewer");
    	JGraphXAdapter<Point, DefaultEdge> graphAdapter = new JGraphXAdapter<Point, DefaultEdge>(g);

        mxIGraphLayout layout = new mxCircleLayout(graphAdapter);
        layout.execute(graphAdapter.getDefaultParent());

        new_window.add(new mxGraphComponent(graphAdapter));

        new_window.pack();
        new_window.setLocationByPlatform(true);
        new_window.setVisible(true);
    }

    /*public SimpleWeightedGraph<String,DefaultWeightedEdge> getGraph() {
        return graph;
    }*/

    public void getSpanningTree() {
        KruskalMinimumSpanningTree k=new KruskalMinimumSpanningTree(g);
        System.out.println(k.getSpanningTree().toString());
        //KruskalMinimumSpanningTree k1=new KruskalMinimumSpanningTree(graph);
        //System.out.println(k1.getEdgeSet().toString());   
    }

    /*public void getSpanningTreeCost() {
        KruskalMinimumSpanningTree k=new KruskalMinimumSpanningTree(graph);
        System.out.println(k.getSpanningTreeCost());
    }*/ 
}