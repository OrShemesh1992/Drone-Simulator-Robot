import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class MyGraphGUI extends JFrame {

    private static final int YFIX = 50;


    HashMap<Point, ArrayList<Point>> graph;

    public MyGraphGUI(HashMap<Point, ArrayList<Point>> graph) {
        this.graph = graph;
        setTitle("Graph");
        setSize(1200, 800);
        setVisible(true);
    }

    public void paint(Graphics g) {
        paintComponent(g);
    }

    private void paintComponent(Graphics g) {

        if (graph.size() != 0) {
            printLines(g);
        }

        repaint();
    }

    private void printLines(Graphics g) {
        HashMap<Point, ArrayList<Point>> temp_graph = new HashMap<>(graph);
        for (Point p : temp_graph.keySet()) {
            if (p.decision_point) {
                g.setColor(Color.red);
            } else {
                g.setColor(Color.blue);
            }
            g.drawOval((int) p.x - 10, (int) p.y - 10 + YFIX, 20, 20);
            g.setColor(Color.black);
//            drawString(g, "x: " + (int) p.x + "\ny: " + (int) p.y, (int) p.x + 10, (int) p.y + YFIX);
            g.drawString("x: " + (int) p.x + "\ny: " + (int) p.y, (int) p.x, (int) p.y + YFIX);
            for (Point neighbor : temp_graph.get(p)) {
                int x = (int) (p.x + neighbor.x) / 2;
                int y = (int) (p.y + neighbor.y) / 2;
                g.drawString("dist: " + Tools.round(Tools.getDistanceBetweenPoints(p, neighbor)), x, y + YFIX);
                g.drawLine((int) p.x, (int) p.y + YFIX, (int) neighbor.x, (int) neighbor.y + YFIX);
            }
        }
    }

    private void drawString(Graphics g, String text, int x, int y) {
        for (String line : text.split("\n"))
            g.drawString(line, x, y += g.getFontMetrics().getHeight());
    }
}
