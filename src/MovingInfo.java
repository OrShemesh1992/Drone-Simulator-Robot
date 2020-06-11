public class MovingInfo {

    public double direction;

    public double left_way;
    public double right_way;
    public double front_way;

    public boolean front_way_is_checked = true;
    public boolean right_way_is_checked = true;
    public boolean left_way_is_checked = true;

    public MovingInfo(double dir) {
        direction = dir;
        front_way = -1;
        right_way = -1;
        left_way = -1;
    }

    public void setFrontWay() {
        front_way = direction;
        front_way_is_checked = true;
    }

    public void setRightWay(double direction) {
        right_way = (direction + 90) % 360;
        right_way_is_checked = false;
    }

    public void setLeftWay(double direction) {
        left_way = (direction + 270) % 360;
        left_way_is_checked = false;
    }

    public void frontWayIsChecked() {

//        front_way = -1;
        front_way_is_checked = true;
    }

    public void rightWayIsChecked() {
        right_way_is_checked = true;
//        right_way = -1;
    }

    public void leftWayIsChecked() {
        left_way_is_checked = true;
//        left_way = -1;
    }

    public String toString() {
        return "DECISION POINT INFO: f: " + Tools.round(front_way) + ", r: " + Tools.round(right_way) + ", l: " + Tools.round(left_way);
    }
}
