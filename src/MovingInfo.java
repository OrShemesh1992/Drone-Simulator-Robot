public class MovingInfo {

    public double direction;
    public double left_way;
    public double right_way;
    public double front_way;


    public MovingInfo(double dir) {
        direction = dir;
        front_way = -1;
        right_way = -1;
        left_way = -1;
    }

    public void setFrontWay(){
        front_way = -1;
    }

    public void setRightWay(double direction){
        right_way = (direction + 90) % 360;
    }

    public void setLeftWay(double direction){
        left_way = (direction + 270) % 360;
    }

    public void frontWayIsChecked(){
        front_way = -1;
    }

    public void rightWayIsChecked(){
        right_way = -1;
    }

    public void leftWayIsChecked(){
        left_way = -1;
    }

    public String toString(){
        return "f: " + front_way + ", r: " + right_way + ", l: " + left_way;
    }
}
