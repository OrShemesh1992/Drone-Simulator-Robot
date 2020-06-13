import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class MyGraph extends JPanel {

    HashMap<Point, ArrayList<Point>> graph;
    Point current_point;

    public MyGraph() {
        graph = new HashMap<>();
    }

    public void addPointToGraph(Point point) {
//        System.out.println("ADD POINT");

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
        JFrame new_window = new JFrame();
        new_window.setSize(1000, 800);
        new_window.setTitle("Graph Viewer");
        new_window.getContentPane().add(new MyGraph());

//        new_window.pack();
//        new_window.setLocationByPlatform(true);
        new_window.setVisible(true);
    }


    public void paint(Graphics g) {
//        g.drawLine(0, 0, 100, 100);
        for (Point p : graph.keySet()) {
            for (Point neighbor : graph.get(p)) {
                g.drawLine((int) (p.x / 2), (int) (p.y / 2), (int) (neighbor.x / 2), (int) (neighbor.y / 2));
                System.err.println(p.toString() + " -> " + neighbor.toString());
            }
        }
//        g.drawOval(100, 100, 100, 100);
    }

    public double calculateDistance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
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
