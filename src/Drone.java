import javax.imageio.ImageIO;

import java.awt.Graphics;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Drone {
	private double gyroRotation;
	private Point sensorOpticalFlow;
	
	private Point pointFromStart;
	public Point startPoint;
	public List<Lidar> lidars;
	private String drone_img_path = "D:\\Tests\\drone_3_pixels.png";
	public Map realMap;
	private double rotation;
	private double speed;
	private CPU cpu;
	
	public Drone(Map realMap) {
		this.realMap = realMap;
		
		this.startPoint = realMap.drone_start_point;
		pointFromStart = new Point();
		sensorOpticalFlow = new Point();
		lidars = new ArrayList<>();

		speed = 0.2;
		
		rotation = 0;
		gyroRotation = rotation;
		
		cpu = new CPU(100,"Drone");
	}
	
	public void play() {
		cpu.play();
	}
	
	public void stop() {
		cpu.stop();
	}
	
	
	public void addLidar(int degrees) {
		Lidar lidar = new Lidar(this,degrees);
		lidars.add(lidar);
		cpu.addFunction(lidar::getSimulationDistance);
	}
	
	public Point getPointOnMap() {
		double x = startPoint.x + pointFromStart.x;
		double y = startPoint.y + pointFromStart.y;
		return new Point(x,y);
	}
	
	public void update(int deltaTime) {

		double distancedMoved = (speed*100)*((double)deltaTime/1000);
		
		pointFromStart =  Tools.getPointByDistance(pointFromStart, rotation, distancedMoved);
		
		double noiseToDistance = Tools.noiseBetween(WorldParams.min_motion_accuracy,WorldParams.max_motion_accuracy,false);
		sensorOpticalFlow = Tools.getPointByDistance(sensorOpticalFlow, rotation, distancedMoved*noiseToDistance);
		
		double noiseToRotation = Tools.noiseBetween(WorldParams.min_rotation_accuracy,WorldParams.max_rotation_accuracy,false);
		double milli_per_minute = 60000;
		gyroRotation += (1-noiseToRotation)*deltaTime/milli_per_minute;
		gyroRotation = formatRotation(gyroRotation);
	}
	
	public static double formatRotation(double rotationValue) {
		rotationValue %= 360;
		if(rotationValue < 0) {
			rotationValue = 360 -rotationValue;
		}
		return rotationValue;
	}
	
	public double getRotation() {
		return rotation;
	}
	
	public double getGyroRotation() {
		return gyroRotation;
	}
	
	public Point getOpticalSensorLocation() {
		return new Point(sensorOpticalFlow);
	}

	
	public void rotateLeft(int deltaTime) {
		double rotationChanged = WorldParams.rotation_per_second*deltaTime/1000;
		
		rotation += rotationChanged;
		rotation = formatRotation(rotation);
		
		gyroRotation += rotationChanged;
		gyroRotation = formatRotation(gyroRotation);
	}
	
	public void rotateRight(int deltaTime) {
		double rotationChanged = -WorldParams.rotation_per_second*deltaTime/1000;
		
		rotation += rotationChanged;
		rotation = formatRotation(rotation);
		
		gyroRotation += rotationChanged;
		gyroRotation = formatRotation(gyroRotation);
	}
	
	public void speedUp(int deltaTime) {
		speed += (WorldParams.accelerate_per_second*deltaTime/1000);
		if(speed > WorldParams.max_speed) {
			speed =WorldParams.max_speed;
		}
	}
	
	public void slowDown(int deltaTime) {
		speed -= (WorldParams.accelerate_per_second*deltaTime/1000);
		if(speed < 0) {
			speed = 0;
		}
	}
	
	
	boolean initPaint = false;
	BufferedImage mImage;
	int j=0;
	public void paint(Graphics g) {
		if(!initPaint) {
			try {
				File f = new File(drone_img_path);
				mImage = ImageIO.read(f);
				initPaint = true;
			} catch(Exception ex) {
				
			}
		}
		//Point p = getPointOnMap();
		//g.drawImage(mImage,p.getX(),p.getY(),mImage.getWidth(),mImage.getHeight());
		
		
		
		
		for(int i=0;i<lidars.size();i++) {
			Lidar lidar = lidars.get(i);
			lidar.paint(g);
		}
	}
	
	public String getInfoHTML() {
		DecimalFormat df = new DecimalFormat("#.####");
		
		String info = "<html>";
		info += "Rotation: " + df.format(rotation) +"<br>";
		info += "Location: " + pointFromStart +"<br>";
		info += "gyroRotation: " + df.format(gyroRotation) +"<br>";
		info += "sensorOpticalFlow: " + sensorOpticalFlow +"<br>";
		info += "</html>";
		return info;
	}
}
