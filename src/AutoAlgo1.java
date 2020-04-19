import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public class AutoAlgo1 {

    enum PixelState {blocked, explored, unexplored, visited}

    int map_size = 3000;
    PixelState map[][];
    Drone drone;
    Point droneStartingPoint;

    ArrayList<Point> points;

    int isRotating;
    ArrayList<Double> degrees_left;
    ArrayList<Func> degrees_left_func;

    boolean isSpeedUp = false;

    Graph mGraph = new Graph();

    CPU ai_cpu;

    public AutoAlgo1(Map realMap) {
        degrees_left = new ArrayList<>();
        degrees_left_func = new ArrayList<>();
        points = new ArrayList<Point>();

        drone = new Drone(realMap);
        drone.addLidar(0);
        drone.addLidar(90);
        drone.addLidar(-90);

        initMap();

        isRotating = 0;
        ai_cpu = new CPU(200, "Auto_AI");
        ai_cpu.addFunction(this::update);
    }

    public void initMap() {
        map = new PixelState[map_size][map_size];
        for (int i = 0; i < map_size; i++) {
            for (int j = 0; j < map_size; j++) {
                map[i][j] = PixelState.unexplored;
            }
        }

        droneStartingPoint = new Point(map_size / 2, map_size / 2);
    }

    public void play() {
        drone.play();
        ai_cpu.play();
    }


    public void update(int deltaTime) {
        updateVisited();
        updateMapByLidars();

        ai(deltaTime);

        if (isRotating != 0) {
            updateRotating(deltaTime);
        }

        if (isSpeedUp) {
            drone.speedUp(deltaTime);
        } else {
            drone.speedDown(deltaTime);
        }
    }

    public void speedUp() {
        isSpeedUp = true;
    }

    public void speedDown() {
        isSpeedUp = false;
    }

    public void updateMapByLidars() {
        Point dronePoint = drone.getOpticalSensorLocation();
        Point fromPoint = new Point(dronePoint.x + droneStartingPoint.x, dronePoint.y + droneStartingPoint.y);

        for (int i = 0; i < drone.lidars.size(); i++) {
            Lidar lidar = drone.lidars.get(i);
            double rotation = drone.getGyroRotation() + lidar.degrees;
            //rotation = Drone.formatRotation(rotation);
            for (int distanceInCM = 0; distanceInCM < lidar.current_distance; distanceInCM++) {
                Point p = Tools.getPointByDistance(fromPoint, rotation, distanceInCM);
                setPixel(p.x, p.y, PixelState.explored);
            }

            if (lidar.current_distance > 0 && lidar.current_distance < WorldParams.lidarLimit - WorldParams.lidarNoise) {
                Point p = Tools.getPointByDistance(fromPoint, rotation, lidar.current_distance);
                setPixel(p.x, p.y, PixelState.blocked);
                //fineEdges((int)p.x,(int)p.y);
            }
        }
    }

    public void updateVisited() {
        Point dronePoint = drone.getOpticalSensorLocation();
        Point fromPoint = new Point(dronePoint.x + droneStartingPoint.x, dronePoint.y + droneStartingPoint.y);

        setPixel(fromPoint.x, fromPoint.y, PixelState.visited);
    }

    public void setPixel(double x, double y, PixelState state) {
        int xi = (int) x;
        int yi = (int) y;

        if (state == PixelState.visited) {
            map[xi][yi] = state;
            return;
        }

        if (map[xi][yi] == PixelState.unexplored) {
            map[xi][yi] = state;
        }
    }
	/*

	public void fineEdges(int x,int y) {
		int radius = 6;

		for(int i=y-radius;i<y+radius;i++) {
			for(int j=x-radius;j<x+radius;j++) {
				if(Math.abs(y-i) <= 1 && Math.abs(x-j) <= 1) {
					continue;
				}
				if(map[i][j] == PixelState.blocked) {
					blockLine(x,y,j,i);
				}
			}
		}
	}
	*/
	/*
	public void blockLine(int x0,int y0,int x1,int y1) {
		if(x0 > x1) {
			int tempX = x0;
			int tempY = y0;
			x0 = x1;
			y0 = y1;
			x1 = tempX;
			y1 = tempY;
		}

	     double deltax = x1 - x0;
	     double deltay = y1 - y0;
	     double deltaerr = Math.abs(deltay / deltax);    // Assume deltax != 0 (line is not vertical),
	     double error = 0.0; // No error at start
	     int y = y0;
	     for (int x=x0;x<x1;x++) {
	    	 setPixel(x,y,PixelState.blocked);
	         error = error + deltaerr;
	         if( 2*error >= deltax ) {
                y = y + 1;
                error=error - deltax;
	        }
	     }

	}
	*/

    public void paintBlindMap(Graphics g) {
        Color c = g.getColor();

        int i = (int) droneStartingPoint.y - (int) drone.startPoint.x;
        int startY = i;
        for (; i < map_size; i++) {
            int j = (int) droneStartingPoint.x - (int) drone.startPoint.y;
            int startX = j;
            for (; j < map_size; j++) {
                if (map[i][j] != PixelState.unexplored) {
                    if (map[i][j] == PixelState.blocked) {
                        g.setColor(Color.RED);
                    } else if (map[i][j] == PixelState.explored) {
                        g.setColor(Color.YELLOW);
                    } else if (map[i][j] == PixelState.visited) {
                        g.setColor(Color.BLUE);
                    }
                    g.drawLine(i - startY, j - startX, i - startY, j - startX);
                }
            }
        }
        g.setColor(c);
    }

    public void paintPoints(Graphics g) {
        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);
            g.drawOval((int) p.x + (int) drone.startPoint.x - 10, (int) p.y + (int) drone.startPoint.y - 10, 20, 20);
        }

    }

    public void paint(Graphics g) {
        if (SimulationWindow.toogleRealMap) {
            drone.realMap.paint(g);
        }

        paintBlindMap(g);
        paintPoints(g);

        drone.paint(g);
    }

    //----------------------ALGORITHM----------------

    boolean is_init = true;
    //    double lastFrontLidarDis = 0;
//    boolean isRotateRight = false;
//    double changedRight = 0;
//    double changedLeft = 0;
//    boolean tryToEscape = false;
    int leftOrRight = 1;

    double max_rotation_to_direction = 20;
    boolean is_finish = true;
    boolean isLeftRightRotationEnable = true;

    boolean is_risky = false;
    int max_risky_distance = 100;
    boolean try_to_escape = false;


    boolean front_risk = false;
    boolean right_risk = false;
    boolean left_risk = false;
    boolean start_left_turn = false;
    boolean have_turn = false;

    double risky_dis = 0;
    int spin_by = 0;

//    double save_point_after_seconds = 3;

    double max_distance_between_points = 100;

    Point init_point;

    public void ai(int deltaTime) {
        if (!SimulationWindow.toogleAI) {
            return;
        }

        //initialize of the start point
        if (is_init) {
            speedUp();//true
            Point dronePoint = drone.getOpticalSensorLocation();//get actual position
            init_point = new Point(dronePoint);
            points.add(dronePoint);
            mGraph.addVertex(dronePoint);
            is_init = false;
        }

        if (isLeftRightRotationEnable) {
            //doLeftRight();
        }

        Point dronePoint = drone.getOpticalSensorLocation(); //get current place

        if (SimulationWindow.return_home) {
            //if between drone and last point less than 100 cm
            if (Tools.getDistanceBetweenPoints(getLastPoint(), dronePoint) < max_distance_between_points) {
                //if we have 0 or 1 point and distance to home point less than 100 cm so is good enough
                if (points.size() <= 1 && Tools.getDistanceBetweenPoints(getLastPoint(), dronePoint) < max_distance_between_points / 5) {
                    speedDown();
                } else {
                    //remove last point for searching the former point
                    removeLastPoint();
                }
            }
        } else {
            //if simulation does not in return home state so add points to map
            if (Tools.getDistanceBetweenPoints(getLastPoint(), dronePoint) >= max_distance_between_points) {
                points.add(dronePoint);
                mGraph.addVertex(dronePoint);
            }
        }


        Lidar lidar = drone.lidars.get(0);
        double front_sensor_dist = lidar.current_distance;
        Lidar lidar1 = drone.lidars.get(1);
        double right_sensor_dist = lidar1.current_distance;
        Lidar lidar2 = drone.lidars.get(2);
        double left_sensor_dist = lidar2.current_distance;


        int turn_coefficient = 1;

        if (drone.getGyroRotation() % 359 < 1 && !start_left_turn) {
            spin_by = 1;
            have_turn = true;
        }

//        System.out.println("f: " + front_sensor_dist + ", r: " + right_sensor_dist + ", l: " + left_sensor_dist);

        if (!is_risky) {
            System.out.println("no risk");

            drone.speedUp(deltaTime);

            if (front_sensor_dist < max_risky_distance && front_sensor_dist != 0) {
                if (front_sensor_dist < 30) {
                    drone.speedDown(deltaTime * 10000);
                }
                front_risk = true;
                is_risky = true;
                risky_dis = front_sensor_dist;
            }

            if (right_sensor_dist < max_risky_distance / 2 && right_sensor_dist != 0) {
                right_risk = true;
                is_risky = true;
                risky_dis = Math.min(risky_dis, right_sensor_dist);
            }

            if (left_sensor_dist < max_risky_distance / 2 && left_sensor_dist != 0) {
                left_risk = true;
                is_risky = true;
                risky_dis = Math.min(risky_dis, left_sensor_dist);
            }
        }


        //no risk and check if you far from right wall
        if (!is_risky) {

//            if (right_sensor_dist < 300 && right_sensor_dist != 0) {
//            boolean empty_space = (front_sensor_dist > 100) && (right_sensor_dist > 100) && (left_sensor_dist > 100);
//            if (right_sensor_dist != 0 && !empty_space) {

            if (right_sensor_dist != 0) {
                if (right_sensor_dist > 100) {
                    spin_by = 1;
                    have_turn = true;
                }
            }
        }


        if (is_risky) {

            System.out.println("risk");

            if (!try_to_escape) {
                try_to_escape = true;
                have_turn = true;


                if (front_risk && !right_risk && !left_risk) {
                    spin_by = -2;
                } else if (front_risk && right_risk && !left_risk) {
                    spin_by = -2;
                } else if (front_risk && !right_risk && left_risk) {
                    spin_by = 1;
                }
//                else if (right_risk) {
//                    spin_by = -1;
//                }


//                if (risky_dis < 20) {
//                    turn_coefficient *= 2;
//                }

                front_risk = false;
                right_risk = false;
                left_risk = false;

            }
        }

        if (have_turn) {
            have_turn = false;

            if (spin_by < 0) {
                start_left_turn = true;
            } else {
                start_left_turn = false;
            }

            spinBy(spin_by * turn_coefficient, true, new Func() {
                @Override
                public void method() {
                    try_to_escape = false;
                    is_risky = false;
                }
            });
        }
    }

    int counter = 0;

    public void doLeftRight() {
        if (is_finish) {
            leftOrRight *= -1;
            counter++;
            is_finish = false;

            spinBy(max_rotation_to_direction * leftOrRight, false, new Func() {
                @Override
                public void method() {
                    is_finish = true;
                }
            });
        }
    }


    double lastGyroRotation = 0;

    public void updateRotating(int deltaTime) {

        if (degrees_left.size() == 0) {
            return;
        }

        double degrees_left_to_rotate = degrees_left.get(0);
        boolean isLeft = true;
        if (degrees_left_to_rotate > 0) {
            isLeft = false;
        }

        double curr = drone.getGyroRotation();
        double just_rotated = 0;

        if (isLeft) {

            just_rotated = curr - lastGyroRotation;
            if (just_rotated > 0) {
                just_rotated = -(360 - just_rotated);
            }
        } else {
            just_rotated = curr - lastGyroRotation;
            if (just_rotated < 0) {
                just_rotated = 360 + just_rotated;
            }
        }


        lastGyroRotation = curr;
        degrees_left_to_rotate -= just_rotated;
        degrees_left.remove(0);
        degrees_left.add(0, degrees_left_to_rotate);

        if ((isLeft && degrees_left_to_rotate >= 0) || (!isLeft && degrees_left_to_rotate <= 0)) {
            degrees_left.remove(0);

            Func func = degrees_left_func.get(0);
            if (func != null) {
                func.method();
            }
            degrees_left_func.remove(0);


            if (degrees_left.size() == 0) {
                isRotating = 0;
            }
            return;
        }

        int direction = (int) (degrees_left_to_rotate / Math.abs(degrees_left_to_rotate));
        drone.rotateLeft(deltaTime * direction);

    }

    public void spinBy(double degrees, boolean isFirst, Func func) {
        lastGyroRotation = drone.getGyroRotation();
        if (isFirst) {
            degrees_left.add(0, degrees);
            degrees_left_func.add(0, func);


        } else {
            degrees_left.add(degrees);
            degrees_left_func.add(func);
        }

        isRotating = 1;
    }

    public void spinBy(double degrees, boolean isFirst) {
        lastGyroRotation = drone.getGyroRotation();
        if (isFirst) {
            degrees_left.add(0, degrees);
            degrees_left_func.add(0, null);


        } else {
            degrees_left.add(degrees);
            degrees_left_func.add(null);
        }

        isRotating = 1;
    }

    public void spinBy(double degrees) {
        lastGyroRotation = drone.getGyroRotation();

        degrees_left.add(degrees);
        degrees_left_func.add(null);
        isRotating = 1;
    }

    public Point getLastPoint() {
        if (points.size() == 0) {
            return init_point;
        }

        Point p1 = points.get(points.size() - 1);
        return p1;
    }

    public Point removeLastPoint() {
        if (points.isEmpty()) {
            return init_point;
        }

        return points.remove(points.size() - 1);
    }


    public Point getAvgLastPoint() {
        if (points.size() < 2) {
            return init_point;
        }

        Point p1 = points.get(points.size() - 1);
        Point p2 = points.get(points.size() - 2);
        return new Point((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
    }
}
