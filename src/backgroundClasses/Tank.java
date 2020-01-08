package backgroundClasses;
import java.awt.Graphics;
import java.io.Serializable;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;


public class Tank extends Sprite implements Serializable{

	private BufferedImage image;
	private static BufferedImage[] tankPics = new BufferedImage[7];
	private static String arrayErrorMessage = "You didn't input an array of 8 BufferedImages.";
	
	private int type;//1,2,3,4 correspond to image numbers
	private boolean blueTeam;//if false then on red team
	private double size;
	private double a;
	private double dTheta;
	private static double friction = 0.9;
	private double maxSpeed;
	private int damage;
	private int health;
	private int lives = 3;
	private boolean respawning;
	
	protected Point[] t3Lasers = new Point[2];

	private int reloadCounter = 0;
	private int reloadTime;
	private boolean firing = false;
	
	private AffineTransform at = new AffineTransform();

	public Tank(int xStart, int yStart, int kind, int ang, boolean side, double scale){
		xPos=xStart;
		yPos=yStart;
		size = scale;
		type = kind;
		blueTeam = side;
		angle = ang;

		resetParams();
		

		loadShape();
		
		t3Lasers[0] = new Point((int) (this.xPos+20*size),(int) (this.yPos+10*size));
		t3Lasers[1] = new Point((int) (this.xPos+20*size),(int) (this.yPos-10*size));
	}
	public Tank(int xStart, int yStart, int kind, boolean side, double scale){
		this(xStart,yStart,kind,0,side,scale);
	}
	public Tank(double[] array){
		this((int)array[1],(int)array[2],(int)array[5],(int)array[3],(array[6]==1),array[7]);
		isAlive = array[0]==1;
		d = array[4];
		reloadCounter = (int)array[8];
		firing = array[9]==1;
		health = (int)array[10];
		lives = (int)array[11];
		respawning = array[12]==1;
		
		xVel=d*Math.cos(angle/180.0*Math.PI);
		yVel=d*Math.sin(angle/180.0*Math.PI);
	}
	public Tank (BufferedImage icon, int xStart, int yStart, int kind, boolean side){
		this(xStart,yStart,kind,side,1);
	}
	public static void setPics(BufferedImage[] pics)throws Exception{
		if(pics.length!=8){
			throw new Exception(arrayErrorMessage);
		}
		tankPics = pics;
	}
	private  void resetParams(){
		if(blueTeam){
			image = tankPics[type-1];
		}else{
			image = tankPics[type+3];
		}
		if(type==1){
			maxSpeed = 2.5;
			reloadTime = 40;
			damage = 20;
			health = 100;
		}else if(type==2){
			maxSpeed = 4;
			reloadTime = 60;
			damage = 25;
			health = 75;
		}else if(type==3){
			maxSpeed = 2;
			reloadTime = 80;
			damage = 20;
			health = 120;
		}else if(type==4){
			maxSpeed = 2.5;
			reloadTime = 120;
			damage = 20;
			health = 100;
		}else{
			maxSpeed = 0;
		}
	}
	public void decodeUpdate(double[] array){
		isAlive = array[0]==1;
		this.setPos((int)array[1],(int)array[2]);
		this.setAngle((int)array[3]);
		d = array[4] = this.getD();
		this.changeType((int)array[5]);
		blueTeam = array[6]==1;
		size = array[7];
		reloadCounter = (int)array[8];
		firing = array[9]==1;
		
		resetParams();
		loadShape();
		
		xVel=d*Math.cos(angle/180.0*Math.PI);
		yVel=d*Math.sin(angle/180.0*Math.PI);
		
		health = (int)array[10];
		lives = (int)array[11];
		respawning = array[12]==1;
		
		t3Lasers[0] = new Point((int) (this.xPos+20*size),(int) (this.yPos+10*size));
		t3Lasers[1] = new Point((int) (this.xPos+20*size),(int) (this.yPos-10*size));
		
		AffineTransform pt = new AffineTransform();
		pt.rotate(angle/180.0*Math.PI,this.xPos,this.yPos);

		for(int i=0;i<2;i++){
			pt.transform(t3Lasers[i],t3Lasers[i]);
		}
	}
	public Point[] getT3Lasers(){
		return t3Lasers;
	}
	public int getLives(){
		return lives;
	}
	public void setLives(int a){
		lives = a;
	}
	public void loseLife(){
		lives--;
	}
	public int getHealth(){
		return health;
	}
	public boolean isRespawning(){
		return respawning;
	}
	public void setRespawning(boolean b){
		respawning = b;
	}
	public void injure(int hits){
		health-=hits;
		if(health<=0){
			this.die();
			health = 0;
			lives--;
		}
	}
	@Override
	public void revive(){
		isAlive = true;
		resetParams();
	}
	public int getType(){
		return type;
	}
	public void changeType(int t){
		type = t;
		resetParams();
	}
	public double getSize(){
		return size;
	}

	public int getReloadCounter(){
		return reloadCounter;
	}
	public void incReload(){
		if(reloadCounter<reloadTime){
			reloadCounter++;
		}
	}
	public void resetReload(){
		reloadCounter=0;
	}
	public boolean fireReady(){
		return reloadCounter>=reloadTime;
	}
	public boolean firing(){
		return firing;
	}
	public void setFiring(boolean a){
		firing = a;
	}
	public void setTeam(boolean a){
		blueTeam = a;
		
		if(blueTeam){
			image = tankPics[type-1];
		}else{
			image = tankPics[type+3];
		}
	}
	public boolean getTeam(){
		return blueTeam;
	}
	public boolean sameTeam(Tank other){
		return this.blueTeam==other.blueTeam;
	}
	public int getDamage(){
		return damage;
	}
	public void turnTo(){
		xVel=d*Math.cos(angle/180.0*Math.PI);
		yVel=d*Math.sin(angle/180.0*Math.PI);
	}
	public void friction(){
		d*=friction;
		xVel=d*Math.cos(angle/180.0*Math.PI);
		yVel=d*Math.sin(angle/180.0*Math.PI);
		if(Math.abs(d)<=0.2){
			stop();
		}
	}
	public void setAccel(double acc){
		a = acc;
	}
	public void accel(){
		this.accel(a);
	}
	public void setDT(double dt){
		dTheta = dt;
	}
	public void turn(){
		this.incAngle((int)dTheta);
	}
	public void turnBack(){
		this.incAngle(-1*(int)dTheta);
	}
	public void accel(double acc){
		d+=acc;
		xVel+=acc*Math.cos(angle/180.0*Math.PI);
		yVel+=acc*Math.sin(angle/180.0*Math.PI);
		if(Math.abs(d)>maxSpeed){
			d*=(maxSpeed/Math.abs(d));
			xVel=d*Math.cos(angle/180.0*Math.PI);
			yVel=d*Math.sin(angle/180.0*Math.PI);
		}
	}
	protected void draw(Graphics gvar){
		if(image==null){
			return;
		}
		Graphics2D g2 = (Graphics2D) gvar;

		at.setToIdentity();
		at.rotate(angle/180.0*Math.PI,this.xPos,this.yPos);
		at.translate(this.xPos-(int)(image.getWidth()/2*size), this.yPos-(int)(image.getHeight()/2*size));
		at.scale(size, size);

		g2.drawImage(image, at,null);

		loadShape();
		
		t3Lasers[0] = new Point((int) (this.xPos+20*size),(int) (this.yPos+10*size));
		t3Lasers[1] = new Point((int) (this.xPos+20*size),(int) (this.yPos-10*size));
		
		AffineTransform pt = new AffineTransform();
		pt.rotate(angle/180.0*Math.PI,this.xPos,this.yPos);

		for(int i=0;i<2;i++){
			pt.transform(t3Lasers[i],t3Lasers[i]);
		}
	}

	public void loadShape(){
		if(image==null){
			this.setShape(new Rectangle((int)xPos,(int)yPos,1,1));
			return;
		}
		at.setToIdentity();
		at.rotate(angle/180.0*Math.PI,this.xPos,this.yPos);
		at.translate(this.xPos-(int)(image.getWidth()/2*size), this.yPos-(int)(image.getHeight()/2*size));
		at.scale(size, size);
		
		Shape hitBox = at.createTransformedShape(image.getData().getBounds());
		this.setShape(hitBox);
	}
	public double[] encode(){
		double[] array = new double[13];
		if(this.isAlive()){
			array[0] = 1;
		}else{
			array[0] = 0;
		}
		array[1] = this.getX();
		array[2] = this.getY();
		array[3] = this.getAng();
		array[4] = this.getD();
		array[5] = this.getType();
		if(this.getTeam()){
			array[6] = 1;
		}else{
			array[6] = 0;
		}
		array[7] = this.getSize();
		array[8] = this.getReloadCounter();
		if(this.firing){
			array[9] = 1;
		}else{
			array[9] = 0;
		}
		array[10] = health;
		array[11] = lives;
		if(respawning){
			array[12] = 1;
		}else{
			array[12] = 0;
		}
		
		return array;
		
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2793667050354268071L;
}
