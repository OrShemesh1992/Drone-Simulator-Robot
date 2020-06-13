import java.util.ArrayList;
import java.util.HashMap;

public class MyGraph {

    HashMap<Point, ArrayList<Point>> graph;
    Point current_point;

    public MyGraph() {
        graph = new HashMap<>();
    }

    public void addPointToGraph(Point point) {
        if (!graph.containsKey(point)) {

            //create neighbors list of this point and add current point to neighbors of point
            ArrayList<Point> neighbors = new ArrayList<>();
            if (current_point != null) {
                neighbors.add(current_point);

                //add this point to neighbors of current point
                if (!graph.get(current_point).contains(point)) {
                    graph.get(current_point).add(point);
                }
            }

            graph.put(point, neighbors);

        } else {

            if (!graph.get(current_point).contains(point)) {
                graph.get(current_point).add(point);
            }

            if (!graph.get(point).contains(current_point)) {
                graph.get(point).add(current_point);
            }

        }

        current_point = point;
    }

    public void drawGraph() {
        MyGraphGUI gui = new MyGraphGUI(graph);
    }

    public int getGraphSize() {
        int size = 0;
        for (Point p : graph.keySet()) {
            size++;
        }
        return size;
    }

    public String toString() {
        String neighbors = "";
        for (Point p : graph.keySet()) {
            neighbors += p.toString() + " : ";
            for (Point neighbor : graph.get(p)) {
                neighbors += neighbor.toString() + ", ";
            }
            neighbors += " ; ";
        }
        return neighbors;
    }

}
