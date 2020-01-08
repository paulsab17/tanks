package backgroundClasses;


import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Missile extends Sprite{

	private int missileType; //corresponds to tank type
	private double speed;
	private int damage;
	private  double size;
	private boolean blueTeam;

	private  int bounceCount;
	private static final int MAX_BOUNCES = 3;
	private ArrayList<Wall> alreadyBouncedWalls = new ArrayList<Wall>();
	private int bounceDelay = 5;
	
	private BufferedImage image;
	private static BufferedImage bRPic;
	private static BufferedImage bBPic;
	private static BufferedImage rRPic;
	private static BufferedImage rBPic;

	AffineTransform at = new AffineTransform();

	public Missile(int type, int missileDamage, int x,int y, boolean team, int ang){
		xPos = x;
		yPos = y;
		blueTeam = team;
		if(team){
			color = Color.blue;
		}else{
			color = Color.red;
		}
		angle = ang;
		missileType = type;
		damage = missileDamage;

		if(missileType==1){
			speed = 10;
			size = 0.08;
			if(blueTeam){
				image = bBPic;
			}else{
				image = rBPic;
			}
		}else if(missileType==2){
			speed = 15;
			size = 0.2;
			if(blueTeam){
				image = bRPic;
			}else{
				image = rRPic;
			}
		}else if(missileType==3){
			speed = 10;
			size = 0.08;
			if(blueTeam){
				image = bBPic;
			}else{
				image = rBPic;
			}
		}else if(missileType==4){
			speed = 10;
			size = 1;
		}

		xVel = speed*Math.cos(angle*Math.PI/180.0);
		yVel = speed*Math.sin(angle*Math.PI/180.0);
		initiateBody();

	}
	public static void setBBPic(BufferedImage img){
		bBPic = img;
	}
	public static void setBRPic(BufferedImage img){
		bRPic = img;
	}
	public static void setRBPic(BufferedImage img){
		rBPic = img;
	}
	public static void setRRPic(BufferedImage img){
		rRPic = img;
	}
	public boolean getTeam(){
		return blueTeam;
	}
	public boolean sameTeam(Tank other){
		return this.getTeam()==other.getTeam();
	}
	public int getType(){
		return missileType;
	}
	public int getDamage(){
		return damage;
	}
	public void bounce(Wall obstacle){
		if(missileType==4 && !(alreadyBouncedWalls.indexOf(obstacle)>=0)){
			alreadyBouncedWalls.add(obstacle);
			if(bounceCount<MAX_BOUNCES){
				if(!obstacle.isHoriz()){
					xVel*=-1;
				}else{
					yVel*=-1;
				}
				bounceCount++;
				angle = (int) (Math.atan(yVel/xVel)/Math.PI*180);
				if(xVel<0){
					angle+=180;
				}
			}else{
				this.die();
			}
			
		}
	}
	public void incBounceDelay(){
		alreadyBouncedWalls.add(null);
		if(alreadyBouncedWalls.size()>bounceDelay){
			for(int i=alreadyBouncedWalls.size()-bounceDelay;i>=0;i--){
				alreadyBouncedWalls.remove(i);
			}
		}
	}
	protected void draw(Graphics gvar){
		initiateBody();
		if(missileType==4){
			drawLaser(gvar);
		}else{
			drawPic(gvar);
		}
	}
	private void drawPic(Graphics gvar){
		if(image==null){
			return;
		}
		Graphics2D g2 = (Graphics2D) gvar;

		at.setToIdentity();
		at.rotate((angle)/180.0*Math.PI,this.xPos,this.yPos);
		at.translate(this.xPos-(int)(image.getWidth()/2*size), this.yPos-(int)(image.getHeight()/2*size));
		at.scale(size, size);

		g2.drawImage(image, at,null);
	}
	private void drawLaser(Graphics gvar){
		Graphics2D g2 = (Graphics2D) gvar;

		Point[] points = new Point[4];
		points[0] = new Point((int)xPos-4,(int)yPos-8);
		points[1] = new Point((int)xPos+4,(int)yPos-8);
		points[2] = new Point((int)xPos+4,(int)yPos+8);
		points[3] = new Point((int)xPos-4,(int)yPos+8);

		for(int i=0;i<points.length;i++){
			double x = points[i].getX();
			double y = points[i].getY();
			double currentAng = Math.atan((y-yPos)/(x-xPos))*180/Math.PI;
			if(x-xPos < 0){
				currentAng+=180;
			}
			double rad = Math.sqrt(Math.pow((x-xPos),2)+Math.pow((y-yPos),2));
			double newAng = (currentAng+angle-90)*Math.PI/180;
			double newX = rad*Math.cos(newAng);
			double newY = rad*Math.sin(newAng);
			points[i].move((int)(newX+xPos),(int)(newY+yPos));
		}
		Polygon body = new Polygon();
		for(int i=0;i<4;i++){
			body.addPoint((int)points[i].getX(), (int)points[i].getY());
		}
		g2.setColor(color);
		g2.fill(body);
	}
	private void initiateBody(){
		if(missileType==4){
			initLaser();
		}else{
			initPic();
		}
	}
	private void initPic(){
		if(image==null){
			this.setShape(new Rectangle((int)xPos,(int)yPos,1,1));
			return;
		}
		at.setToIdentity();
		at.rotate((angle)/180.0*Math.PI,this.xPos,this.yPos);
		at.translate(this.xPos-(int)(image.getWidth()/2*size), this.yPos-(int)(image.getHeight()/2*size));
		at.scale(size, size);
		
		Shape hitBox = at.createTransformedShape(image.getData().getBounds());
		this.setShape(hitBox);
	}
	private void initLaser(){
		Point[] points = new Point[4];
		points[0] = new Point((int)xPos-4,(int)yPos-8);
		points[1] = new Point((int)xPos+4,(int)yPos-8);
		points[2] = new Point((int)xPos+4,(int)yPos+8);
		points[3] = new Point((int)xPos-4,(int)yPos+8);

		for(int i=0;i<points.length;i++){
			double x = points[i].getX();
			double y = points[i].getY();
			double currentAng = Math.atan((y-yPos)/(x-xPos))*180/Math.PI;
			if(x-xPos < 0){
				currentAng+=180;
			}
			double rad = Math.sqrt(Math.pow((x-xPos),2)+Math.pow((y-yPos),2));
			double newAng = (currentAng+angle-90)*Math.PI/180;
			double newX = rad*Math.cos(newAng);
			double newY = rad*Math.sin(newAng);
			points[i].move((int)(newX+xPos), (int)(newY+yPos));
		}
		Polygon body = new Polygon();
		for(int i=0;i<4;i++){
			body.addPoint((int)points[i].getX(), (int)points[i].getY());
		}
		setShape(body);
	}
}
