

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
            if (p.decision_point) {
                g.setColor(Color.red);
            }
//            else {
//                g.setColor(Color.black);
//            }
            g.drawOval((int) p.x + (int) drone.startPoint.x - 10, (int) p.y + (int) drone.startPoint.y - 10, 20, 20);
            g.setColor(Color.black);

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
    double max_risky_distance = 150;
    boolean try_to_escape = false;


    boolean front_risk = false;
    boolean right_risk = false;
    boolean left_risk = false;
    boolean start_left_turn = false;
    boolean start_right_turn = false;
    boolean have_turn = false;
    double risky_dis = 0;
    double spin_by = 0;
    double iteration = 0;

    boolean high_priority = false;
    Point last_point;

    int decision_point_limit = 0;

    int right_turn_counter = 0;
    int left_turn_counter = 0;

    int right_angle_counter = 0;
    int left_angle_counter = 0;

    int have_turn_counter = 0;

    boolean return_home = false;

    Point last_turn_point;
    MyGraph myGraph = new MyGraph();


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
            if (Tools.getDistanceBetweenPoints(getLastPoint(), dronePoint) < max_distance_between_points / 3) {
                if (points.size() <= 1 && Tools.getDistanceBetweenPoints(getLastPoint(), dronePoint) < max_distance_between_points / 5) {
                    speedDown();
                } else {
                    removeLastPoint();
                }
            }
        } else {
            if (Tools.getDistanceBetweenPoints(getLastPoint(), dronePoint) >= max_distance_between_points) {
                points.add(dronePoint);
                mGraph.addVertex(dronePoint);
//                if (dronePoint.decision_point || points.size() == 0) {
//                    myGraph.addPoint(dronePoint);
//                }
            }
        }


        ////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////Algorithm///////////////////////////////////
        ////////////////////////////////////////////////////////////////////////////////

        Point crossroad_point = inDecisionPoint(dronePoint);

        if (last_point == null && crossroad_point != null) {
            last_point = crossroad_point;
            myGraph.addPointToGraph(crossroad_point);
        }

        if (crossroad_point == null) {
            last_point = null;
        }

        iteration++;

        double[] lidars_distance = getLidarsDistances();
        double front_sensor_dist = lidars_distance[0];
        double right_sensor_dist = lidars_distance[1];
        double left_sensor_dist = lidars_distance[2];

        if (!is_risky) {
            checkSpeedAccelerate(front_sensor_dist);
            checkRisks(front_sensor_dist, right_sensor_dist, left_sensor_dist, deltaTime);
        }


        if (crossroad_point != null && !return_home && last_turn_point == null) {

            double turn_angle = 0;
            MovingInfo movingInfo = crossroad_point.moving_info;

            //drone come from front way of decision point
            if (comeFromWay(movingInfo.direction, drone.getGyroRotation())) {
                System.out.print(ConsoleColors.GREEN_BOLD + "COME FROM FRONT WAY ");

//                if (last_point == null && crossroad_point != null) {
//                    last_point = crossroad_point;
//                    double angle = 90 - (crossroad_point.moving_info.direction % 90);
//                    double xx = Math.sin(Math.toRadians(angle)) * 20;
//                    double yy = Math.cos(Math.toRadians(90 - angle)) * 20;
//                    Point point = new Point(crossroad_point.x + xx, crossroad_point.y + yy);
//                    myGraph.addPointToGraph(point);
//                }

                if (!movingInfo.right_way_is_checked) {

                    //left moving turn
                    turn_angle = ((movingInfo.right_way - drone.getGyroRotation()) - 360) % 360;
                    movingInfo.rightWayIsChecked();
                    System.out.print(ConsoleColors.GREEN_BOLD + "-> TURN LEFT -> ");

                } else if (!movingInfo.left_way_is_checked) {

                    //right moving turn
                    turn_angle = ((movingInfo.left_way - drone.getGyroRotation()) + 360) % 360;
                    movingInfo.leftWayIsChecked();
                    System.out.print(ConsoleColors.GREEN_BOLD + "-> TURN RIGHT -> ");

                } else if (movingInfo.right_way_is_checked && movingInfo.left_way_is_checked) {

                    //fix angle to go home
                    turn_angle = ((movingInfo.direction + 180) % 360) - drone.getGyroRotation();
                    System.out.print(ConsoleColors.GREEN_BOLD + "-> GO BACK -> ");

                }
            }
            //drone come from right way of decision point
            else if (comeFromWay(movingInfo.right_way, drone.getGyroRotation())) {

                System.out.print(ConsoleColors.GREEN_BOLD + "COME FROM RIGHT WAY ");
                movingInfo.rightWayIsChecked();

                if (!movingInfo.left_way_is_checked) {

                    turn_angle = movingInfo.right_way - ((drone.getGyroRotation() + 180) % 360);

                    if (turn_angle < -200) {
                        turn_angle = 360 - turn_angle;
                    }

                    System.out.print(ConsoleColors.GREEN_BOLD + "-> KEEP FORWARD TO LEFT WAY -> ");

                    movingInfo.leftWayIsChecked();

//                } else if (movingInfo.left_way == -1 && movingInfo.right_way == -1) {
                } else if (movingInfo.right_way_is_checked && movingInfo.left_way_is_checked) {

                    //fix angle to go home
                    turn_angle = ((movingInfo.direction + 180) - 360 - drone.getGyroRotation()) % 360;
                    System.out.print(ConsoleColors.GREEN_BOLD + "-> GO BACK -> ");

                }

            }
            //drone come from left way of decision point
            else if (comeFromWay(movingInfo.left_way, drone.getGyroRotation())) {

                System.out.print(ConsoleColors.GREEN_BOLD + "COME FROM LEFT WAY ");
                movingInfo.leftWayIsChecked();

                if (!movingInfo.right_way_is_checked) {

                    //create right calculation

                    turn_angle = ((movingInfo.right_way + 180) % 360) - ((drone.getGyroRotation() + 180) % 360);

                    if (turn_angle < -200) {
                        turn_angle = 360 - turn_angle;
                    }

                    System.out.print(ConsoleColors.GREEN_BOLD + " -> KEEP FORWARD TO RIGHT WAY -> ");

                    movingInfo.rightWayIsChecked();

//                } else if (movingInfo.left_way == -1 && movingInfo.right_way == -1) {
                } else if (movingInfo.right_way_is_checked && movingInfo.left_way_is_checked) {

                    //fix angle to go home
                    turn_angle = ((movingInfo.direction + 180) + 360 - drone.getGyroRotation()) % 360;
                    System.out.print(ConsoleColors.GREEN_BOLD + "-> GO BACK -> ");

                }
            }
            //from back
            else if (comeFromWay(movingInfo.direction + 180, drone.getGyroRotation())) {
//            else {

                System.out.print(ConsoleColors.GREEN_BOLD + "COME FROM BACK WAY ");

                if (!movingInfo.right_way_is_checked) {
                    turn_angle = ((movingInfo.direction - drone.getGyroRotation()) + 360) % 360;
                    System.out.print(ConsoleColors.GREEN_BOLD + "-> TURN RIGHT -> ");
                } else if (!movingInfo.left_way_is_checked) {
                    turn_angle = ((movingInfo.direction - drone.getGyroRotation()) - 360) % 360;
                    System.out.print(ConsoleColors.GREEN_BOLD + "-> TURN LEFT -> ");
                } else if (movingInfo.right_way_is_checked && movingInfo.left_way_is_checked) {
                    //fix angle to go home
                    turn_angle = (movingInfo.direction - 180);
                    System.out.print(ConsoleColors.GREEN_BOLD + "-> GO BACK -> ");
                }
            }

            if (turn_angle != 0) {
                System.out.println(ConsoleColors.GREEN_BOLD + "TURN ANGLE: " + Tools.round(turn_angle));
                speedDown();
                last_turn_point = crossroad_point;

                spinBy(turn_angle, true, new Func() {
                    @Override
                    public void method() {
                        if (front_sensor_dist > 100) {
                            speedUp();
                        }
                    }
                });
            }
        } else if (crossroad_point != null && return_home) {

            double turn_angle = 0;
//            double temp_turn_angle = 0;
//            temp_turn_angle = ((crossroad_point.moving_info.direction + 180) % 360) - drone.getGyroRotation();
//            turn_angle = Math.abs(crossroad_point.moving_info.direction + 180 - drone.getGyroRotation()) > 20 ? temp_turn_angle : 0;

            MovingInfo movingInfo = crossroad_point.moving_info;
//            System.out.println(movingInfo.toString());
            System.out.print(ConsoleColors.RED_BOLD + "RETURN HOME FROM -> ");

            if (comeFromWay(movingInfo.direction, drone.getGyroRotation())) {

//                turn_angle = ((movingInfo.direction + 180) % 360) - drone.getGyroRotation();

                turn_angle = drone.getGyroRotation() - ((movingInfo.direction + 180) % 360);
                if (turn_angle < -300) {
                    turn_angle = 360 + turn_angle;
                }

                System.out.print(ConsoleColors.RED_BOLD + "FRONT WAY -> ");

            } else if (comeFromWay(movingInfo.right_way, drone.getGyroRotation())) {

//                turn_angle = ((movingInfo.direction + 180) % 360) - drone.getGyroRotation();

                turn_angle = (movingInfo.direction + 180) % 360 - drone.getGyroRotation();
                if (turn_angle > 0) {
                    turn_angle -= 360;
                }

                System.out.print(ConsoleColors.RED_BOLD + "RIGHT WAY -> ");

            } else if (comeFromWay(movingInfo.left_way, drone.getGyroRotation())) {

//                turn_angle = (drone.getGyroRotation() + (movingInfo.direction + 180)) % 360;

                turn_angle = (movingInfo.direction + 180) % 360 - drone.getGyroRotation();
                if (turn_angle < 0) {
                    turn_angle = 360 + turn_angle;
                }

                System.out.print(ConsoleColors.RED_BOLD + "LEFT WAY -> ");

            } else if (comeFromWay(movingInfo.direction + 180, drone.getGyroRotation())) {

                turn_angle = (drone.getGyroRotation() + 180) % 360;
                System.out.print(ConsoleColors.RED_BOLD + "BACK WAY -> ");

            }


            if (turn_angle != 0) {

                boolean priority = true;
                speedDown();

                if (turn_angle < 0) {
                    if (left_sensor_dist < 50) {
                        priority = false;
                    }
                }

                if (turn_angle > 0) {
                    if (right_sensor_dist < 50) {
                        priority = false;
                    }
                }

                System.out.println(ConsoleColors.RED_BOLD + " TURN " + Tools.round(turn_angle) + " DEGREES");
                spinBy(turn_angle, priority, new Func() {
                    @Override
                    public void method() {
                        if (front_sensor_dist > 100) {
                            speedUp();
                        }
                    }
                });

                points.remove(crossroad_point);
            } else {
                System.err.println(ConsoleColors.RED_BOLD + "BAD CALCULATION");
            }

        } else {

            if (iteration % 1000 == 0) {
                iteration = 0;
                String s = return_home ? "Enable" : "Disable";
                System.out.println(ConsoleColors.WHITE_BOLD + "FREE MOVING -> RETURN HOME MODE IS -> " + s);
                System.out.println(ConsoleColors.WHITE_BOLD + "f: " + front_sensor_dist +
                        ", r: " + right_sensor_dist +
                        ", l: " + left_sensor_dist +
                        ", speed: " + Tools.round(drone.getSpeed()));

            }

            if (have_turn) {
                have_turn_counter++;
                have_turn = false;
            }


            if (!checkIfThereOtherRedPoint(dronePoint)) {
                checkDecisionPoint(front_sensor_dist, right_sensor_dist, left_sensor_dist, dronePoint);
            }

            if (!is_risky) {
                checkSpeedAccelerate(front_sensor_dist);
                checkRisks(front_sensor_dist, right_sensor_dist, left_sensor_dist, deltaTime);
            }

            if (!is_risky) {
                checkChangeTurn();
            }

            if (is_risky) {
                solveRisk(right_sensor_dist, left_sensor_dist);
            }


            if (have_turn) {
                have_turn = false;

                spinBy(spin_by, false, new Func() {
                    @Override
                    public void method() {
                        try_to_escape = false;
                        is_risky = false;
                    }
                });
                spin_by = 0;
            }
        }
    }

    private double[] getLidarsDistances() {
        double[] distances = {
                drone.lidars.get(0).current_distance,
                drone.lidars.get(1).current_distance,
                drone.lidars.get(2).current_distance
        };
        return distances;
    }

    private void checkSpeedAccelerate(double front_sensor_dist) {
        if (front_sensor_dist < 300) {
            drone.speedDown(0.01);
        } else {
            drone.speedUp(0.0001);
        }
    }

    private void checkRisks(double front_dist, double right_dist, double left_dist, int deltaTime) {
        if (front_dist < max_risky_distance && front_dist != 0) {
            if (front_dist < 30) {
                drone.speedDown(deltaTime * 10000);
            }
            front_risk = true;
            is_risky = true;
        }

        if (right_dist < max_risky_distance / 2 && right_dist != 0) {
            right_risk = true;
            is_risky = true;
        }

        if (left_dist < max_risky_distance / 2 && left_dist != 0) {
            left_risk = true;
            is_risky = true;
        }
    }

    private void checkChangeTurn() {
        if (spin_by < 0) {
            left_angle_counter++;
            right_angle_counter = 0;
            if (left_angle_counter > 80) {
                left_turn_counter = left_turn_counter + 1;
                if (right_turn_counter == 3) {
                    //loop
                    spin_by = 1;
                    left_turn_counter = 0;
                }
            }
        } else if (spin_by > 0) {
            right_angle_counter++;
            left_angle_counter = 0;
            if (right_angle_counter > 80) {
                right_turn_counter = right_turn_counter + 1;
                if (right_turn_counter == 3) {
                    //loop
                    spin_by = -1;
                    right_turn_counter = 0;
                }
            }
        }
    }

    private void solveRisk(double right_sensor_dist, double left_sensor_dist) {
        if (!try_to_escape) {
            try_to_escape = true;
            have_turn = true;
            if (front_risk && !right_risk && !left_risk) {
                if (right_sensor_dist > left_sensor_dist - 5) {
                    spin_by = 1;
                }
//                else if (left_sensor_dist > right_sensor_dist - 5) {
//                    spin_by = -1;
//                }
                else {
                    spin_by = -1;
                }
            } else if (front_risk && right_risk && !left_risk) {
                spin_by = -1;
            } else if (front_risk && !right_risk && left_risk) {
                spin_by = 1;
            }

            front_risk = false;
            right_risk = false;
            left_risk = false;
        }
    }

    private void checkDecisionPoint(double front_sensor_dist, double right_sensor_dist, double left_sensor_dist, Point p) {
        int open_ways = 0;

        MovingInfo moving_info = new MovingInfo(drone.getGyroRotation());

        if (front_sensor_dist > 299) {
            open_ways++;
            moving_info.setFrontWay();
        }
        if (right_sensor_dist > 299) {
            open_ways++;
            moving_info.setRightWay(drone.getGyroRotation());
        }
        if (left_sensor_dist > 299) {
            open_ways++;
            moving_info.setLeftWay(drone.getGyroRotation());
        }

        if (open_ways > 1) {

            p.decision_point = true;
            p.moving_info = moving_info;
            if (!checkIfThereOtherRedPoint(p)) {//if no other red point in environment then add point
                points.add(p);
                mGraph.addVertex(p);
                myGraph.addPointToGraph(p);
            }
        }

    }

    private boolean checkIfThereOtherRedPoint(Point current) {

        for (Point p : points) {
            if (p.decision_point) {
                if (Tools.getDistanceBetweenPoints(p, current) < max_distance_between_points) {
                    return true;
                }
            }
        }

        return false;
    }

    private synchronized Point inDecisionPoint(Point current) {

        if (getLastPoint().decision_point && !return_home) {
            return null;
        }

        for (Point previous_point : points) {
            if (previous_point.decision_point) {
                if (return_home) {
                    if (Tools.getDistanceBetweenPoints(previous_point, current) < 40) {
                        return previous_point;
                    }
                } else {
                    if (Tools.getDistanceBetweenPoints(previous_point, current) < 50) {
                        return previous_point;
                    }
                }
            }
        }

        last_turn_point = null;
        return null;
    }

//    private boolean updateDecisionPoint(Point current_point) {
//
//        if (last_turn_point == null) {
//            return false;
//        }
//
//        if (Tools.getDistanceBetweenPoints(last_turn_point, current_point) < 100) {
//            return true;
//        }
//
//        last_turn_point = null;
//        return false;
//    }


    private boolean comeFromWay(double open_way_angle, double current_angle) {

        current_angle = (current_angle + 180) % 360;

        if (Math.abs(current_angle - open_way_angle) < 45) {
            return true;
        }

        return false;
    }


    private boolean comeFromSide(double open_way_angle, double current_angle) {
        current_angle = (current_angle + 180) % 360;

        if ((open_way_angle + 135) % 360 < current_angle && current_angle < (open_way_angle + 225) % 360) {
            return true;
        }
        return false;
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

        double direction = (degrees_left_to_rotate / Math.abs(degrees_left_to_rotate));
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
