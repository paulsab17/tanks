package gameRunners;

import backgroundClasses.Missile;
import backgroundClasses.Sprite;
import backgroundClasses.SpriteImage;
import backgroundClasses.Tank;
import backgroundClasses.Wall;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.Timer;

public class TankTesting extends JFrame implements KeyListener{

	private static String b1PicAddress = "/b1.png";
	private static String b2PicAddress = "/b2.png";
	private static String b3PicAddress = "/b3.png";
	private static String b4PicAddress = "/b4.png";
	private static String bRPicAddress = "/bMissile.png";
	private static String bBPicAddress = "/bBall.png";

	//frame dimensions
	private static final int FRAME_WIDTH = 1350;
	private static final int FRAME_HEIGHT = 750;
	private static Color background = Color.YELLOW;
	private static Color wallColor = Color.black;

	//holds images from the computer
	private static SpriteImage image;
	private static BufferedImage[] tankPics = new BufferedImage[8];

	private static Tank player;
	private static ArrayList<Missile> missiles = new ArrayList<Missile>();
	private static ArrayList<Sprite> array = new ArrayList<Sprite>();
	private static ArrayList<Wall> walls = new ArrayList<Wall>();

	public TankTesting(){
		try{
			tankPics[0] = ImageIO.read(TwoPlayerGame.class.getResource(b1PicAddress));
			tankPics[1] = ImageIO.read(TwoPlayerGame.class.getResource(b2PicAddress));
			tankPics[2] = ImageIO.read(TwoPlayerGame.class.getResource(b3PicAddress));
			tankPics[3] = ImageIO.read(TwoPlayerGame.class.getResource(b4PicAddress));
			Missile.setBRPic(ImageIO.read(TwoPlayerGame.class.getResource(bRPicAddress)));
			Missile.setBBPic(ImageIO.read(TwoPlayerGame.class.getResource(bBPicAddress)));
		}catch(IOException e){
			e.printStackTrace();
		}
		
		try {
			Tank.setPics(tankPics);
		} catch (Exception e) {
			e.printStackTrace();
		}

		player = new Tank(200,200,1,true,1);
		
		loadWalls();

		image = new SpriteImage(array);
		addKeyListener(this);
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);

	}

	public static void main(String[] args) {

		/* a weird voodoo magic-y way to allow mouse inputs that creates
		 * code that will run once and involves instantiating an object
		 * of this class within the main method of this class for some reason
		 */
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run(){
				TankTesting frame = new TankTesting();
				frame.setTitle("Testing, Testing");
				frame.setResizable(false);
				frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
				frame.setMinimumSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.getContentPane().setBackground(background);
				frame.getContentPane().add(image);
				frame.pack();
				frame.setVisible(true); 

				Timer mover = new Timer(20,frame.stuffMover);
				mover.start();
			}
		});
	}

	private ActionListener stuffMover = new ActionListener(){
		public void actionPerformed(ActionEvent evt) {
			player.friction();
			player.accel();
			if(checkMoveWall()){
				player.moveNow();
			}
			if(checkTurnWall()){
				player.turn();
				player.turnTo();
			}

			player.incReload();
			fire();

			moveMissiles();
			checkMissileWalls();

			array.clear();
			array.add(player);
			array.addAll(missiles);
			array.addAll(walls);
			image.repaint();
		}
	};

	private void moveMissiles(){
		if(!missiles.isEmpty()){
			for(int i=0;i<missiles.size();i++){
				missiles.get(i).moveNow();
				double x = missiles.get(i).getX();
				double y = missiles.get(i).getY();
				if(x>FRAME_WIDTH+50 || x<-50 || y>FRAME_HEIGHT+50 || y<-50){
					missiles.remove(i);
				}
			}
		}
	}
	private boolean checkMoveWall(){
		boolean hitwall = false;
		if(!walls.isEmpty()){
			player.moveNow();
			player.loadShape();
			for(Object m :walls){
				Wall w = (Wall) m;
				if(player.intersects(w.getShape())){
					hitwall = true;
					break;
				}
			}
			if(player.getX()<0 || player.getX()>FRAME_WIDTH || player.getY()<0||player.getY()>FRAME_HEIGHT-25){
				hitwall = true;
			}
			player.moveBackNow();
			player.loadShape();
			if(hitwall){
				player.stop();
			}
		}
		return !hitwall;
	}
	private boolean checkTurnWall(){
		boolean hitwall = false;
		if(!walls.isEmpty()){
			player.turn();
			player.loadShape();
			for(Object m :walls){
				Wall w = (Wall) m;
				if(player.intersects(w.getShape())){
					hitwall = true;
					break;
				}
			}
			player.turnBack();
			player.loadShape();
		}
		return !hitwall;
	}
	private void checkMissileWalls(){
		if(!missiles.isEmpty() && !walls.isEmpty()){
			for(int i=0;i<missiles.size();i++){
				missiles.get(i).incBounceDelay();
				for(int j=0;j<walls.size();j++){
					if(missiles.get(i).intersects(walls.get(j).getShape())){
						if(missiles.get(i).getType()==4){
							missiles.get(i).bounce(walls.get(j));
							if(!missiles.get(i).isAlive()){
								missiles.remove(i);
							}
						}else{
							missiles.get(i).die();
							missiles.remove(i);
						}
						break;
					}
				}
			}
		}
	}
	private void fire(){
		if(player.firing() && player.isAlive() && player.fireReady()){
			if(player.getType()==1 ||player.getType()==2){
				missiles.add(new Missile(player.getType(),player.getDamage(),(int)player.getX(),(int)player.getY(),true,player.getAng()));	
			}else if(player.getType()==3){
				for(int i=0;i<player.getT3Lasers().length;i++){
					missiles.add(new Missile(player.getType(),player.getDamage(),(int)player.getT3Lasers()[i].getX(),(int)player.getT3Lasers()[i].getY(),true,player.getAng()));
				}
			}else{
				missiles.add(new Missile(player.getType(),player.getDamage(),(int)player.getX(),(int)player.getY(),true,player.getAng()));
			}
			player.resetReload();
		}
	}
	private void loadWalls(){
		walls.add(new Wall(0,FRAME_HEIGHT/2,false,FRAME_HEIGHT,10,wallColor));
		walls.add(new Wall(FRAME_WIDTH/2,0,true,FRAME_WIDTH,10,wallColor));
		walls.add(new Wall(FRAME_WIDTH-7,FRAME_HEIGHT/2,false,FRAME_HEIGHT,10,wallColor));
		walls.add(new Wall(FRAME_WIDTH/2,FRAME_HEIGHT-30,true,FRAME_WIDTH,10,wallColor));
		walls.add(new Wall(400,400,true,600,10,wallColor));
		walls.add(new Wall(700,400,false,600,10,wallColor));
		walls.add(new Wall(750,400,false,600,10,wallColor));
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode()== KeyEvent.VK_W && player.isAlive()){
			player.setAccel(1);
		}else if(e.getKeyCode()== KeyEvent.VK_S && player.isAlive()){
			player.setAccel(-1);
		}else if(e.getKeyCode()== KeyEvent.VK_D && player.isAlive()){
			if(player.getD()>=0){
				player.setDT(4);
			}else{
				player.setDT(-4);
			}
		}else if(e.getKeyCode()== KeyEvent.VK_A && player.isAlive()){
			if(player.getD()>=0){
				player.setDT(-4);
			}else{
				player.setDT(4);
			}
		}else if(e.getKeyCode()== KeyEvent.VK_F && player.isAlive() ){
			player.setFiring(true);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode()== KeyEvent.VK_W && player.isAlive()){
			player.setAccel(0);
		}else if(e.getKeyCode()== KeyEvent.VK_S && player.isAlive()){
			player.setAccel(0);
		}else if(e.getKeyCode()== KeyEvent.VK_D && player.isAlive()){
			player.setDT(0);
		}else if(e.getKeyCode()== KeyEvent.VK_A && player.isAlive()){
			player.setDT(0);
		}else if(e.getKeyCode()== KeyEvent.VK_F){
			player.setFiring(false);
		}else if(e.getKeyCode()== KeyEvent.VK_Q && player.isAlive()){
			int type = player.getType()%4+1;			
			player.changeType(type);
		}

	}

	@Override
	public void keyTyped(KeyEvent e) {

	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 7909210048122294150L;

}
