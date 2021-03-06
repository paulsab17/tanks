package unused;

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
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.Timer;

import backgroundClasses.Missile;
import backgroundClasses.Picture;
import backgroundClasses.Sprite;
import backgroundClasses.SpriteImage;
import backgroundClasses.Tank;
import backgroundClasses.TankServer;
import backgroundClasses.TextBox;
import backgroundClasses.Wall;

public class ServerTwoPlayer extends JFrame implements KeyListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 517044870223898103L;
	//addresses for pictures used
	private static String b1PicAddress = "Pictures/b1.png";
	private static String b2PicAddress = "Pictures/b2.png";
	private static String b3PicAddress = "Pictures/b3.png";
	private static String b4PicAddress = "Pictures/b4.png";
	private static String bRPicAddress = "Pictures/bMissile.png";
	private static String bBPicAddress = "Pictures/bBall.png";
	private static String r1PicAddress = "Pictures/r1.png";
	private static String r2PicAddress = "Pictures/r2.png";
	private static String r3PicAddress = "Pictures/r3.png";
	private static String r4PicAddress = "Pictures/r4.png";
	private static String rRPicAddress = "Pictures/rMissile.png";
	private static String rBPicAddress = "Pictures/rBall.png";
	private static String bIPicAddress = "Pictures/blueTankIcon.png";
	private static String rIPicAddress = "Pictures/redTankIcon.png";
	private static String explosionPicAddress = "Pictures/explosion.png";

	//server  stuff
	private static TankServer serv;
	private static int socketNum = 5005;
	private static boolean[] keyValues = new boolean[6];
	private static boolean[]enemyKeyValues = new boolean[6];
	// maps to: 0-W, 1-S, 2-A, 3-D, 4-SPACE, 5-Q

	//game status
	private static boolean preGame = true;
	private static boolean gameOn = false;
	private static boolean gameOver = false;
	private static boolean blueReady = false;
	private static boolean redReady = false;

	//frame dimensions
	private static final int FRAME_WIDTH = 1350;
	private static final int FRAME_HEIGHT = 725;
	private static Color background = Color.YELLOW;
	private static Color wallColor = Color.black;

	//holds images from the computer
	private static SpriteImage image;
	private static BufferedImage[] tankPics = new BufferedImage[8];
	private static BufferedImage blueTankIcon;
	private static BufferedImage redTankIcon;
	private static BufferedImage explosionPic;


	private static Tank p1;
	private static Tank p2;
	private static Tank demoBlue;
	private static Tank demoRed;
	private static int blueLives = 3;
	private static int redLives = 3;
	private static boolean blueRespawning = false;
	private static boolean redRespawning = false;
	private static int respawningTimer = 0;
	private static boolean blueChanging = false;
	private static boolean redChanging = false;

	private static ArrayList<Sprite> array = new ArrayList<Sprite>();
	private static ArrayList<Missile> missiles = new ArrayList<Missile>();
	private static ArrayList<Wall> walls = new ArrayList<Wall>();
	private static ArrayList<Picture> lifeIcons = new ArrayList<Picture>();
	private static ArrayList<Wall> healthBars = new ArrayList<Wall>();
	private static TextBox redHealthBox;
	private static TextBox blueHealthBox;
	private static TextBox centerTextBox;
	private static TextBox blueControls;
	private static TextBox redControls;
	private static TextBox blueStats;
	private static TextBox redStats;
	private static TextBox blueSpecial;
	private static TextBox redSpecial;
	private static TextBox redReadyMessage;
	private static TextBox blueReadyMessage;
	private static String[] t1StatText;
	private static String[] t2StatText;
	private static String[] t3StatText;
	private static String[] t4StatText;


	public ServerTwoPlayer(){
		Scanner kb = new Scanner(System.in);
		System.out.print("Are you Hosting or Joining(Type H/J)");
		String str = kb.next().toUpperCase();
		while(!(str.equals("H") || str.equals("J"))){
			System.out.print("You can't follow simple directions.");
			System.out.print("Are you Hosting or Joining(Type H/J)");
			str = kb.nextLine().toUpperCase();
		}
		if(str.equals("H")){
			try{
				serv = new TankServer(socketNum);
			}catch(IOException e){
				e.printStackTrace();
			}
		}else{
			kb.nextLine();
			System.out.println("Type in the IP address to connect to:");
			String address = kb.nextLine();
			try{
				serv = new TankServer(socketNum,address);
			}catch(IOException e){
				e.printStackTrace();
			}
		}

		try{
			tankPics[0] = ImageIO.read(new File(b1PicAddress));
			tankPics[1] = ImageIO.read(new File(b2PicAddress));
			tankPics[2] = ImageIO.read(new File(b3PicAddress));
			tankPics[3] = ImageIO.read(new File(b4PicAddress));
			tankPics[4] = ImageIO.read(new File(r1PicAddress));
			tankPics[5] = ImageIO.read(new File(r2PicAddress));
			tankPics[6] = ImageIO.read(new File(r3PicAddress));
			tankPics[7] = ImageIO.read(new File(r4PicAddress));
			Missile.setBRPic(ImageIO.read(new File(bRPicAddress)));
			Missile.setBBPic(ImageIO.read(new File(bBPicAddress)));
			Missile.setRRPic(ImageIO.read(new File(rRPicAddress)));
			Missile.setRBPic(ImageIO.read(new File(rBPicAddress)));
			blueTankIcon = ImageIO.read(new File(bIPicAddress));
			redTankIcon = ImageIO.read(new File(rIPicAddress));
			explosionPic = ImageIO.read(new File(explosionPicAddress));
		}catch(IOException e){
			e.printStackTrace();
		}

		try {
			Tank.setPics(tankPics);
		} catch (Exception e) {
			e.printStackTrace();
		}		

		resetGame();

		image = new SpriteImage(array);
		addKeyListener(this);
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		
		kb.close();

	}
	private void resetGame(){
		blueLives = 3;
		redLives = 3;
		blueReady = false;
		redReady = false;
		respawningTimer = 0;

		loadWalls();
		setStatText();

		p1 = new Tank(200,200,1,0,true,1);
		p2 = new Tank(FRAME_WIDTH-200,FRAME_HEIGHT-200,1,180,false,1);

		demoBlue = new Tank(FRAME_WIDTH/4,FRAME_HEIGHT/2+25,1,0,true,3);
		demoRed = new Tank(FRAME_WIDTH*3/4,FRAME_HEIGHT/2+25,1,180,false,3);

		blueHealthBox = new TextBox(""+p1.getHealth(),450,25,Color.blue);
		redHealthBox = new TextBox(""+p2.getHealth(),FRAME_WIDTH-450,25,Color.red);

		String[] blueControlTextArray = {"Controls:","W -Forwards","A -Left","S -Backwards","D -Right","","F -Shoot","Q -Switch Tank","   (after respawn)"};
		blueControls = new TextBox(blueControlTextArray,FRAME_WIDTH/4,150,Color.BLUE,25);

		String[] redControlTextArray = {"Controls:","I -Forwards","J -Left","K -Backwards","L -Right","","; -Shoot","U -Switch Tank","   (after respawn)"};
		redControls = new TextBox(redControlTextArray,FRAME_WIDTH*3/4,150,Color.red,25);

		String[] readyTextArray = {"Click Shoot","When Ready"};
		blueReadyMessage = new TextBox(readyTextArray,120,FRAME_HEIGHT/2+25,Color.DARK_GRAY,Color.blue,30);
		redReadyMessage = new TextBox(readyTextArray,FRAME_WIDTH-120,FRAME_HEIGHT/2+25,Color.DARK_GRAY,Color.red,30);

		centerTextBox = new  TextBox("Choose your Tank!",FRAME_WIDTH/2,FRAME_HEIGHT/2+25,Color.DARK_GRAY,Color.GREEN);

		blueStats = new TextBox(t1StatText,FRAME_WIDTH/4,FRAME_HEIGHT-175,Color.blue,30);
		redStats = new TextBox(t1StatText,FRAME_WIDTH*3/4,FRAME_HEIGHT-175,Color.red,30);

		blueSpecial = new TextBox("",FRAME_WIDTH/4,FRAME_HEIGHT-75,Color.blue,30);
		redSpecial = new TextBox("",FRAME_WIDTH*3/4,FRAME_HEIGHT-75,Color.red,30);
	}
	private void loadWalls(){
		walls.add(new Wall(0,FRAME_HEIGHT/2,false,FRAME_HEIGHT,10,wallColor));
		walls.add(new Wall(FRAME_WIDTH/2,0,true,FRAME_WIDTH,10,wallColor));
		walls.add(new Wall(FRAME_WIDTH-7,FRAME_HEIGHT/2,false,FRAME_HEIGHT,10,wallColor));
		walls.add(new Wall(FRAME_WIDTH/2,FRAME_HEIGHT-30,true,FRAME_WIDTH,10,wallColor));
		walls.add(new Wall(375,400,true,400,10,wallColor));
		walls.add(new Wall(975,400,true,400,10,wallColor));
		walls.add(new Wall(580,400,false,400,10,wallColor));
		walls.add(new Wall(770,400,false,400,10,wallColor));
	}
	private void setStatText(){
		String[] a = {"Health: 100","Damage: 20","Speed: 5","Fire Rate: 6"};
		t1StatText = a;

		String[] b = {"Health: 75","Damage: 25","Speed: 10","Fire Rate: 4"};
		t2StatText = b;

		String[] c = {"Health: 120","Damage: 20","Speed: 2","Fire Rate: 3"};
		t3StatText = c;

		String[] d = {"Health: 100","Damage: 20","Speed: 5","Fire Rate: 2"};
		t4StatText = d;

	}

	public static void main(String[] args) {

		/* a weird voodoo magic-y way to allow mouse inputs that creates
		 * code that will run once and involves instantiating an object
		 * of this class within the main method of this class for some reason
		 */
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run(){
				ServerTwoPlayer frame = new ServerTwoPlayer();
				frame.setTitle("Testing, Testing");
				frame.setResizable(false);
				frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
				frame.setMinimumSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.getContentPane().setBackground(background);
				frame.getContentPane().add(image);
				frame.pack();
				frame.setVisible(true); 

				Timer mover = new Timer(10,frame.stuffMover);
				mover.start();
			}
		});
	}
	private ActionListener stuffMover = new ActionListener(){
		public void actionPerformed(ActionEvent evt) {
			
			/*
			sendValues();
			getValues();
			*/
			
			if(preGame){
				if(redReady && blueReady){
					respawningTimer++;
				}
				if(respawningTimer>=50){
					preGame = false;
					gameOn = true;
					redReady = false;
					blueReady = false;
				}
				if(respawningTimer%3==0){
					changeTanks();
				}
				updateDemoStats();
				array.clear();
				array.add(demoBlue);
				array.add(demoRed);
				array.add(blueControls);
				array.add(redControls);
				array.add(blueStats);
				array.add(redStats);
				array.add(blueSpecial);
				array.add(redSpecial);
				array.add(blueReadyMessage);
				array.add(redReadyMessage);
				array.add(centerTextBox);
				image.repaint();
			}else if(gameOn){
				updatePlayer(p1);
				updatePlayer(p2);
				
				if(respawningTimer%3==0){
					changeTanks();
				}

				moveMissiles();
				checkMissileWalls();
				checkMissileHits();

				updateHealthCounters();
				updateHealthBars();
				updateLifeIcons();

				if(respawningTimer<500){
					respawningTimer++;
				}

				array.clear();
				if(p1.isAlive()){
					array.add(p1);
				}else{
					blueLives--;
					if(blueLives>0){
						respawnTank(p1);
					}else{
						endGame();
					}
				}
				if(p2.isAlive()){
					array.add(p2);
				}else{
					redLives--;
					if(redLives>0){
						respawnTank(p2);
					}else{
						endGame();
					}
				}
				array.addAll(missiles);
				array.addAll(walls);
				array.add(blueHealthBox);
				array.add(redHealthBox);
				array.addAll(healthBars);
				array.addAll(lifeIcons);
				image.repaint();

			}else if(gameOver){

				moveMissiles();
				checkMissileWalls();

				updateHealthCounters();
				updateHealthBars();
				updateLifeIcons();

				array.clear();
				if(p1.isAlive()){
					array.add(p1);
				}
				if(p2.isAlive()){
					array.add(p2);
				}
				array.addAll(missiles);
				array.addAll(walls);
				array.add(blueHealthBox);
				array.add(redHealthBox);
				array.addAll(healthBars);
				array.addAll(lifeIcons);
				array.add(centerTextBox);
				image.repaint();
			}

		}
	};
	/*
	private void sendValues(){
		serv.print(keyValues);
	}
	private void getValues(){
		try {
		enemyKeyValues = serv.get();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(enemyKeyValues[0]){
			p2.setAccel(1);
			if(redRespawning){
				redRespawning = false;
			}
		}else{
			p2.setAccel(0);
		}
		
		if(enemyKeyValues[1]){
			p2.setAccel(-1);
			if(redRespawning){
				redRespawning = false;
			}
		}else{
			p2.setAccel(0);
		}
		
		if(enemyKeyValues[2]){
			if(p2.getType()==3){
				p2.setDT(-1);
			}else{
				p2.setDT(-2);
			}
			if(redRespawning){
				redRespawning = false;
			}
		}else{
			p2.setDT(0);
		}
		
		if(enemyKeyValues[3]){
			if(p2.getType()==3){
				p2.setDT(1);
			}else{
				p2.setDT(2);
			}
			if(redRespawning){
				redRespawning = false;
			}
		}else{
			p2.setDT(0);
		}
		
		if(enemyKeyValues[4]){
			p2.setFiring(true);
			if(redRespawning){
				redRespawning = false;
			}else if(preGame){
				redReady = true;
				String[] a = {"Ready!"};
				redReadyMessage.setText(a);
			}
		}else{
			p2.setFiring(false);
		}
		
		if(enemyKeyValues[5]){
			redChanging = true;
		}else{
			redChanging = false;
		}
	}
	*/
	private void updateDemoStats(){
		if(demoBlue.getType()==1){
			blueStats.setText(t1StatText);
			blueSpecial.setText("");
		}else if(demoBlue.getType()==2){
			blueStats.setText(t2StatText);
			blueSpecial.setText("");
		}else if(demoBlue.getType()==3){
			blueStats.setText(t3StatText);
			blueSpecial.setText("Fires two shots");
		}else if(demoBlue.getType()==4){
			blueStats.setText(t4StatText);
			blueSpecial.setText("Shots bounce off walls");
		}
		if(demoRed.getType()==1){
			redStats.setText(t1StatText);
			redSpecial.setText("");
		}else if(demoRed.getType()==2){
			redStats.setText(t2StatText);
			redSpecial.setText("");
		}else if(demoRed.getType()==3){
			redStats.setText(t3StatText);
			redSpecial.setText("Fires two shots");
		}else if(demoRed.getType()==4){
			redStats.setText(t4StatText);
			redSpecial.setText("Shots bounce off walls");
		}
	}
	private void updatePlayer(Tank a){
		a.friction();
		a.accel();
		if(checkMoveWall(a)){
			a.moveNow();
		}
		if(checkTurnWall(a)){
			a.turn();
			a.turnTo();
		}

		a.incReload();
		fire(a);
	}
	private boolean checkMoveWall(Tank a){
		boolean hitwall = false;
		if(!walls.isEmpty()){
			a.moveNow();
			a.loadShape();
			for(Object m :walls){
				Wall w = (Wall) m;
				if(a.intersects(w.getShape())){
					hitwall = true;
					break;
				}
			}
			if(a==p1){
				if(a.intersects(p2.getShape()) && p2.isAlive()){
					hitwall = true;
				}
			}else{
				if(a.intersects(p1.getShape()) && p1.isAlive()){
					hitwall = true;
				}
			}
			a.moveBackNow();
			a.loadShape();
			if(hitwall){
				a.stop();
			}
		}
		return !hitwall;
	}
	private boolean checkTurnWall(Tank a){
		boolean hitwall = false;
		if(!walls.isEmpty()){
			a.turn();
			a.loadShape();
			for(Object m :walls){
				Wall w = (Wall) m;
				if(a.intersects(w.getShape())){
					hitwall = true;
					break;
				}
			}
			if(a==p1){
				if(a.intersects(p2.getShape()) && p2.isAlive()){
					hitwall = true;
				}
			}else{
				if(a.intersects(p1.getShape()) && p1.isAlive()){
					hitwall = true;
				}
			}

			a.turnBack();
			a.loadShape();
		}
		return !hitwall;
	}
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
								i--;
							}
						}else{
							missiles.get(i).die();
							missiles.remove(i);
							i--;
						}
						break;
					}
				}
			}
		}
	}
	private void fire(Tank a){
		if(a.firing() && a.isAlive() && a.fireReady()){
			if(a.getType()==1 ||a.getType()==2){
				missiles.add(new Missile(a.getType(),a.getDamage(),(int)a.getX(),(int)a.getY(),a.getTeam(),a.getAng()));	
			}else if(a.getType()==3){
				for(int i=0;i<a.getT3Lasers().length;i++){
					missiles.add(new Missile(a.getType(),a.getDamage(),(int)a.getT3Lasers()[i].getX(),(int)a.getT3Lasers()[i].getY(),a.getTeam(),a.getAng()));
				}
			}else{
				missiles.add(new Missile(a.getType(),a.getDamage(),(int)a.getX(),(int)a.getY(),a.getTeam(),a.getAng()));
			}
			a.resetReload();
		}
	}
	private void checkMissileHits(){
		if(!missiles.isEmpty() && !walls.isEmpty()){
			for(int i=0;i<missiles.size();i++){
				if(missiles.get(i).intersects(p1.getShape()) && !missiles.get(i).sameTeam(p1) && !blueRespawning){
					p1.injure(missiles.get(i).getDamage());
					missiles.get(i).die();
					missiles.remove(i);
				}else if(missiles.get(i).intersects(p2.getShape()) && !missiles.get(i).sameTeam(p2) && !redRespawning){
					p2.injure(missiles.get(i).getDamage());
					missiles.get(i).die();
					missiles.remove(i);
				}
			}
		}
	}
	private void updateHealthCounters(){
		blueHealthBox.setText(""+p1.getHealth());
		redHealthBox.setText(""+p2.getHealth());
	}
	private void updateHealthBars(){
		healthBars.clear();
		healthBars.add(new Wall(200,25,true,(int) (p1.getHealth()*2.9),15,Color.blue));
		healthBars.add(new Wall(FRAME_WIDTH-200,25,true,(int) (p2.getHealth()*2.9),15,Color.red));
	}
	private void updateLifeIcons(){
		lifeIcons.clear();
		for(int i=0;i<blueLives;i++){
			lifeIcons.add(new Picture(blueTankIcon,100+100*i,50,0.2));
		}
		if(blueRespawning && respawningTimer%20<10){
			lifeIcons.add(new Picture(explosionPic,100+100*(blueLives),50,0.05));
		}
		for(int i=0;i<redLives;i++){
			lifeIcons.add(new Picture(redTankIcon,FRAME_WIDTH-100-100*i,50,0.2));
		}
		if(redRespawning && respawningTimer%20<10){
			lifeIcons.add(new Picture(explosionPic,FRAME_WIDTH-100-100*(redLives),50,0.05));
		}
	}
	private void changeTanks(){
		if(blueChanging){
			int type = p1.getType()%4+1;			
			demoBlue.changeType(type);	
			p1.changeType(type);
		}
		if(redChanging){
			int type = p2.getType()%4+1;			
			demoRed.changeType(type);	
			p2.changeType(type);
		}
	}
	private void respawnTank(Tank a){
		a.revive();
		if(a==p1){
			if(p2.getX()>FRAME_WIDTH/2 ){
				if(p2.getY()>FRAME_HEIGHT/2){
					a.setPos(200, 200);
				}else{
					a.setPos(200, FRAME_HEIGHT-200);
				}
				a.setAngle(0);
			}else{
				if(p2.getY()>FRAME_HEIGHT/2){
					a.setPos(FRAME_WIDTH-200, 200);
				}else{
					a.setPos(FRAME_WIDTH-200, FRAME_HEIGHT-200);
				}
				a.setAngle(180);
			}
			p1.stop();
			p1.setFiring(false);
			p2.stop();
			p2.setFiring(false);

			blueRespawning = true;
			respawningTimer = 0;
		}else{
			if(p1.getX()>FRAME_WIDTH/2 ){
				if(p1.getY()>FRAME_HEIGHT/2){
					a.setPos(200, 200);
				}else{
					a.setPos(200, FRAME_HEIGHT-200);
				}
				a.setAngle(0);
			}else{
				if(p1.getY()>FRAME_HEIGHT/2){
					a.setPos(FRAME_WIDTH-200, 200);
				}else{
					a.setPos(FRAME_WIDTH-200, FRAME_HEIGHT-200);
				}
				a.setAngle(180);
			}
			redRespawning = true;
			respawningTimer = 0;
		}

	}
	private void endGame(){
		gameOn = false;
		gameOver = true;
		if(redLives>blueLives){
			centerTextBox = new  TextBox(new String[]{"Red is Victorious!!!","Click space to play again."},FRAME_WIDTH/2,FRAME_HEIGHT/2,Color.GREEN,Color.red,40);
		}else{
			centerTextBox = new  TextBox(new String[]{"Blue is Victorious!!!","Click space to play again."},FRAME_WIDTH/2,FRAME_HEIGHT/2,Color.GREEN,Color.blue,40);
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode()== KeyEvent.VK_W && p1.isAlive() && respawningTimer>=50 && (!redRespawning || blueRespawning)){
			p1.setAccel(1);
			if(blueRespawning){
				blueRespawning = false;
			}
			keyValues[0] = true;
		}else if(e.getKeyCode()== KeyEvent.VK_S && p1.isAlive()&& respawningTimer>=50 && (!redRespawning || blueRespawning)){
			p1.setAccel(-1);
			if(blueRespawning){
				blueRespawning = false;
			}
			keyValues[1] = true;
		}else if(e.getKeyCode()== KeyEvent.VK_D && p1.isAlive()&& respawningTimer>=50 && (!redRespawning || blueRespawning)){
			if(p1.getType()==3){
				p1.setDT(1);
			}else{
				p1.setDT(2);
			}
			if(blueRespawning){
				blueRespawning = false;
			}
			keyValues[2] = true;
		}else if(e.getKeyCode()== KeyEvent.VK_A && p1.isAlive()&& respawningTimer>=50 && (!redRespawning || blueRespawning)){
			if(p1.getType()==3){
				p1.setDT(-1);
			}else{
				p1.setDT(-2);
			}
			if(blueRespawning){
				blueRespawning = false;
			}
			keyValues[3] = true;
		}else if(e.getKeyCode()== KeyEvent.VK_SPACE && p1.isAlive()&& (respawningTimer>=50 || preGame)&& (!redRespawning || blueRespawning)){
			p1.setFiring(true);
			if(blueRespawning){
				blueRespawning = false;
			}else if(preGame){
				blueReady = true;
				String[] a = {"Ready!"};
				blueReadyMessage.setText(a);
			}
			keyValues[4] = true;
		}else if(e.getKeyCode()==KeyEvent.VK_Q && (blueRespawning || preGame)){
			blueChanging = true;
			keyValues[5] = true;
		}

	}

	@Override
	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode()== KeyEvent.VK_W){
			p1.setAccel(0);
			keyValues[0] = false;
		}else if(e.getKeyCode()== KeyEvent.VK_S){
			p1.setAccel(0);
			keyValues[1] = false;
		}else if(e.getKeyCode()== KeyEvent.VK_D){
			p1.setDT(0);
			keyValues[2] = false;
		}else if(e.getKeyCode()== KeyEvent.VK_A){
			p1.setDT(0);
			keyValues[3] = false;
		}else if(e.getKeyCode()== KeyEvent.VK_SPACE && !gameOver){
			p1.setFiring(false);
			keyValues[4] = false;
		}else if(e.getKeyCode()==KeyEvent.VK_Q){
			blueChanging = false;
			keyValues[5] = false;
		}else if(e.getKeyCode()==KeyEvent.VK_SPACE && gameOver){
			gameOver = false;
			preGame = true;
			resetGame();
		}else if (e.getKeyCode()==KeyEvent.VK_7){
			double[] arr2 = p2.encode();
			double[] arr1 = p1.encode();
			p1.decodeUpdate(arr2);
			p2.decodeUpdate(arr1);
			System.out.println("Ooooo Spooky!");
		}

	}

	@Override
	public void keyTyped(KeyEvent arg0) {		
	}

}
