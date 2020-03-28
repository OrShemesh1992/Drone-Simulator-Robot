import java.io.File;
import java.io.IOException;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Window;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;


public class Map{
	private boolean[][] map;
	Drone drone;
	Point drone_start_point;

	public Map(String path,Point drone_start_point) {
		try {
			this.drone_start_point = drone_start_point;
			BufferedImage img_map = ImageIO.read(new File(path));
			this.map = render_map_from_image_to_boolean(img_map);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private boolean[][] render_map_from_image_to_boolean(BufferedImage map_img) {
		int w = map_img.getWidth();
		int h = map_img.getHeight();
		boolean[][] map = new boolean[w][h];
		for(int y=0;y<h;y++) {
			for(int x=0;x<w;x++) {
				int clr = map_img.getRGB(x, y);
				int red = (clr & 0x00ff0000) >> 16;
			    int green = (clr & 0x0000ff00) >> 8;
			    int blue = clr & 0x000000ff;
				if(red != 0 && green != 0 && blue != 0) { // think black
					map[x][y] = true;
				}
			}
		}
		
		return map;
	}
	
	boolean isCollide(int x,int y) {
		
		return !map[x][y];
	}
	
	public void paint(Graphics g) {
		Color c = g.getColor();
		g.setColor(Color.GRAY);
		for(int i=0;i<map.length;i++) {
			for(int j=0;j<map[0].length;j++) {
				if(!map[i][j])  {
					g.drawLine(i, j, i, j);
				}
			}
		}
		g.setColor(c);
	}

}

