import java.text.DecimalFormat;

public class Point {
	public double x;
	public double y;

	public boolean decision_point;
	public MovingInfo moving_info;
	
	public Point(double x,double y) {
		this.x = x;
		this.y = y;
		decision_point = false;
	}
	
	public Point(Point p) {
		this.x = p.x;
		this.y = p.y;
	}
	
	public Point() {
		x = 0;
		y = 0;
	}
	
	@Override
	public String toString() {
		DecimalFormat df = new DecimalFormat("#.###");
		
		return "(" + df.format(x) + "," + df.format(y) + ")";
	}

}
