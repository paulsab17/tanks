package gameRunners;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import backgroundClasses.Missile;
import backgroundClasses.Picture;
import backgroundClasses.Sprite;
import backgroundClasses.SpriteImage;
import backgroundClasses.Tank;
import backgroundClasses.TankServer;
import backgroundClasses.TextBox;
import backgroundClasses.Wall;

public class TwoPlayerServer extends JFrame implements KeyListener{

	//addresses for pictures used
	private static String b1PicAddress = "/b1.png";
	private static String b2PicAddress = "/b2.png";
	private static String b3PicAddress = "/b3.png";
	private static String b4PicAddress = "/b4.png";
	private static String bRPicAddress = "/bMissile.png";
	private static String bBPicAddress = "/bBall.png";
	private static String r1PicAddress = "/r1.png";
	private static String r2PicAddress = "/r2.png";
	private static String r3PicAddress = "/r3.png";
	private static String r4PicAddress = "/r4.png";
	private static String rRPicAddress = "/rMissile.png";
	private static String rBPicAddress = "/rBall.png";
	private static String bIPicAddress = "/blueTankIcon.png";
	private static String rIPicAddress = "/redTankIcon.png";
	private static String explosionPicAddress = "/boom.png";

	//server  stuff
	private static TankServer serv;
	private static int socketNum = 5012;
	private static double[] tankInfo = new double[11];
	private static double[]enemyTankInfo = new double[11];
	private static boolean host;

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
	private static int respawningTimer = 0;

	private static ArrayList<Sprite> array = new ArrayList<Sprite>();
	private static ArrayList<Missile> missiles = new ArrayList<Missile>();
	private static ArrayList<Wall> walls = new ArrayList<Wall>();
	private static ArrayList<Picture> lifeIcons = new ArrayList<Picture>();
	private static ArrayList<Wall> healthBars = new ArrayList<Wall>();
	private static TextBox redHealthBox;
	private static TextBox blueHealthBox;
	private static TextBox centerTextBox;
	private static TextBox blueControls;
	private static TextBox blueStats;
	private static TextBox blueSpecial;
	private static TextBox redReadyMessage;
	private static TextBox blueReadyMessage;
	private static TextBox rulesTextBox;
	private static String[] t1StatText;
	private static String[] t2StatText;
	private static String[] t3StatText;
	private static String[] t4StatText;


	public TwoPlayerServer(){
		String str = JOptionPane.showInputDialog(null, "Are you Hosting or Joining(Type H/J)").toUpperCase();
		while(!(str.equals("H") || str.equals("J"))){
			str = JOptionPane.showInputDialog(null, "You can't follow simple directions. Are you Hosting or Joining(Type H/J)").toUpperCase();
		}
		if(str.equals("H")){
			host = true;
			try{
				serv = new TankServer(socketNum);
			}catch(IOException e){
				e.printStackTrace();
			}
		}else{
			host = false;
			String address = JOptionPane.showInputDialog(null, "Type in the IP address to connect to","10.170.xx.xxx").toUpperCase();
			try{
				serv = new TankServer(socketNum,address);
			}catch(IOException e){
				e.printStackTrace();
			}
		}

		try{
			tankPics[0] = ImageIO.read(TwoPlayerGame.class.getResource(b1PicAddress));
			tankPics[1] = ImageIO.read(TwoPlayerGame.class.getResource(b2PicAddress));
			tankPics[2] = ImageIO.read(TwoPlayerGame.class.getResource(b3PicAddress));
			tankPics[3] = ImageIO.read(TwoPlayerGame.class.getResource(b4PicAddress));
			tankPics[4] = ImageIO.read(TwoPlayerGame.class.getResource(r1PicAddress));
			tankPics[5] = ImageIO.read(TwoPlayerGame.class.getResource(r2PicAddress));
			tankPics[6] = ImageIO.read(TwoPlayerGame.class.getResource(r3PicAddress));
			tankPics[7] = ImageIO.read(TwoPlayerGame.class.getResource(r4PicAddress));
			Missile.setBRPic(ImageIO.read(TwoPlayerGame.class.getResource(bRPicAddress)));
			Missile.setBBPic(ImageIO.read(TwoPlayerGame.class.getResource(bBPicAddress)));
			Missile.setRRPic(ImageIO.read(TwoPlayerGame.class.getResource(rRPicAddress)));
			Missile.setRBPic(ImageIO.read(TwoPlayerGame.class.getResource(rBPicAddress)));
			blueTankIcon = ImageIO.read(TwoPlayerGame.class.getResource(bIPicAddress));
			redTankIcon = ImageIO.read(TwoPlayerGame.class.getResource(rIPicAddress));
			explosionPic = ImageIO.read(TwoPlayerGame.class.getResource(explosionPicAddress));
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

	}
	private void resetGame(){
		blueReady = false;
		redReady = false;
		respawningTimer = 0;

		loadWalls();
		setStatText();

		if(host){
			p1 = new Tank(200,200,1,0,true,1);
			p2 = new Tank(FRAME_WIDTH-200,FRAME_HEIGHT-200,1,180,false,1);
		}else{
			p2 = new Tank(200,200,1,0,false,1);
			p1 = new Tank(FRAME_WIDTH-200,FRAME_HEIGHT-200,1,180,true,1);
		}

		p1.setLives(3);
		p2.setLives(3);

		demoBlue = new Tank(FRAME_WIDTH/2,FRAME_HEIGHT/4+25,1,0,true,3);

		blueHealthBox = new TextBox(""+p1.getHealth(),450,25,Color.blue);
		redHealthBox = new TextBox(""+p2.getHealth(),FRAME_WIDTH-450,25,Color.red);

		String[] blueControlTextArray = {"Controls:","W -Forwards","A -Left","S -Backwards","D -Right","","SPACE -Shoot","Q -Switch Tank","   (after respawn)"};
		blueControls = new TextBox(blueControlTextArray,FRAME_WIDTH/4,150,Color.BLUE,25);

		String[] readyTextArray = {"Click Shoot","When Ready"};
		blueReadyMessage = new TextBox(readyTextArray,150,FRAME_HEIGHT/2+25,Color.DARK_GRAY,Color.blue,30);
		redReadyMessage = new TextBox(readyTextArray,FRAME_WIDTH-150,FRAME_HEIGHT/2+25,Color.DARK_GRAY,Color.red,30);

		centerTextBox = new  TextBox("Choose your Tank!",FRAME_WIDTH/2,FRAME_HEIGHT/2+25,Color.DARK_GRAY,Color.GREEN);

		blueStats = new TextBox(t1StatText,FRAME_WIDTH*3/4,100,Color.blue,30);

		blueSpecial = new TextBox("",FRAME_WIDTH*3/4,200,Color.blue,30);
		
		String[] rules = {"Each player has three lives.","You may switch tanks after dying and respawning.","May the best tank win!"};
		rulesTextBox = new TextBox(rules,FRAME_WIDTH/2,FRAME_HEIGHT-175,Color.DARK_GRAY,30);
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

		String[] b = {"Health: 75","Damage: 25","Speed: 8","Fire Rate: 4"};
		t2StatText = b;

		String[] c = {"Health: 120","Damage: 20","Speed: 4","Fire Rate: 3"};
		t3StatText = c;

		String[] d = {"Health: 100","Damage: 20","Speed: 5","Fire Rate: 2"};
		t4StatText = d;

	}

	public static void main(String[] args) {
		 Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
		        public void run() {
		            serv.end();
		        }
		    }, "Shutdown-thread"));

		/* a weird voodoo magic-y way to allow mouse inputs that creates
		 * code that will run once and involves instantiating an object
		 * of this class within the main method of this class for some reason
		 */
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run(){
				TwoPlayerServer frame = new TwoPlayerServer();
				frame.setTitle("Testing, Testing");
				frame.setResizable(false);
				frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
				frame.setMinimumSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.getContentPane().setBackground(background);
				frame.getContentPane().add(image);
				frame.pack();
				frame.setVisible(true); 

				Timer mover = new Timer(2,frame.stuffMover);
				mover.start();
			}
		});
	}

	private ActionListener stuffMover = new ActionListener(){
		public void actionPerformed(ActionEvent evt) {

			sendInfo();
			getInfo();

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
				updateDemoStats();
				array.clear();
				array.add(demoBlue);
				array.add(blueControls);
				array.add(blueStats);
				array.add(blueSpecial);
				array.add(blueReadyMessage);
				array.add(redReadyMessage);
				array.add(centerTextBox);
				array.add(rulesTextBox);
				image.repaint();

			}else if(gameOn){
				updatePlayer(p1);
				updatePlayer(p2);

				moveMissiles();
				checkMissileWalls();
				checkMissileHits();
				checkMissileMissile();

				updateHealthCounters();
				updateHealthBars();
				updateLifeIcons();

				if(respawningTimer<500){
					respawningTimer++;
				}

				array.clear();
				if(p1.isAlive()){
					array.add(p1);
				}else if(p1.getLives()>0){
					respawnTank(p1);
				}
				if(p2.isAlive()){
					array.add(p2);
				}else if(p2.getLives()>0){
					respawnTank(p2);
				}
				if(p1.getLives()<=0 || p2.getLives() <=0){
					endGame();
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
	private void sendInfo(){

		tankInfo = p1.encode();
		serv.print(tankInfo);

	}
	private void getInfo(){
		boolean a = p2.isAlive();
		try {
			enemyTankInfo = serv.get();
		} catch (IOException e) {
			e.printStackTrace();
		}

		p2.decodeUpdate(enemyTankInfo);
		p2.setTeam(false);
		p2.setAlive(a);

		if(p2.firing()){

			if(preGame){
				redReady = true;
				String[] str = {"Ready!"};
				redReadyMessage.setText(str);
			}

			if(p2.isRespawning()){
				p2.setRespawning(false);;
			}
		}
	}
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
	private void checkMissileMissile(){
		if(!missiles.isEmpty()){
			ArrayList<Missile> toRemove = new  ArrayList<Missile>();
			for(int i=0;i<missiles.size();i++){
				for(int j=0;j<missiles.size();j++){
					if(missiles.get(j).intersects(missiles.get(i).getShape()) && i!=j){
						missiles.get(i).die();
						missiles.get(j).die();
						toRemove.add(missiles.get(i));
						toRemove.add(missiles.get(j));
					}
				}
			}

			missiles.removeAll(toRemove);
		}
	}
	private void fire(Tank a){
		if(a.firing() && a.isAlive() && a.fireReady()){
			if(a.getType()==1 ||a.getType()==2){
				missiles.add(new Missile(a.getType(),a.getDamage(),(int)a.getX(),(int)a.getY(),a.getTeam(),a.getAng()));	
				if(a.getType()==2){
					a.accel(-3);
				}
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
		if(!missiles.isEmpty()){
			for(int i=0;i<missiles.size();i++){
				if(missiles.get(i).intersects(p1.getShape()) && !missiles.get(i).sameTeam(p1) && !p1.isRespawning()){
					p1.injure(missiles.get(i).getDamage());
					missiles.get(i).die();
					missiles.remove(i);
				}else if(missiles.get(i).intersects(p2.getShape()) && !missiles.get(i).sameTeam(p2) && !p2.isRespawning()){
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
		for(int i=0;i<p1.getLives();i++){
			lifeIcons.add(new Picture(blueTankIcon,100+100*i,50,0.2));
		}
		if(p1.isRespawning() && respawningTimer%20<10){
			lifeIcons.add(new Picture(explosionPic,100+100*(p1.getLives()),50,0.05));
		}
		for(int i=0;i<p2.getLives();i++){
			lifeIcons.add(new Picture(redTankIcon,FRAME_WIDTH-100-100*i,50,0.2));
		}
		if(p2.isRespawning() && respawningTimer%20<10){
			lifeIcons.add(new Picture(explosionPic,FRAME_WIDTH-100-100*(p2.getLives()),50,0.15));
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
		}

		p1.stop();
		p1.setFiring(false);
		p2.stop();
		p2.setFiring(false);

		respawningTimer = 0;
		a.setRespawning(true);

	}
	private void endGame(){
		gameOn = false;
		gameOver = true;
		if(p2.getLives()>p1.getLives()){
			centerTextBox = new  TextBox(new String[]{"Red is Victorious!!!","Click R to play again."},FRAME_WIDTH/2,FRAME_HEIGHT/2,Color.GREEN,Color.red,40);
		}else{
			centerTextBox = new  TextBox(new String[]{"Blue is Victorious!!!","Click R to play again."},FRAME_WIDTH/2,FRAME_HEIGHT/2,Color.GREEN,Color.blue,40);
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode()== KeyEvent.VK_W && p1.isAlive() && respawningTimer>=50 && !(p2.isRespawning() || p1.isRespawning())){
			p1.setAccel(1);
		}else if(e.getKeyCode()== KeyEvent.VK_S && p1.isAlive()&& respawningTimer>=50 && !(p2.isRespawning() || p1.isRespawning())){
			p1.setAccel(-1);
		}else if(e.getKeyCode()== KeyEvent.VK_D && p1.isAlive()&& respawningTimer>=50 && !(p2.isRespawning() || p1.isRespawning())){
			if(p1.getType()==3){
				p1.setDT(1);
			}else{
				p1.setDT(2);
			}
		}else if(e.getKeyCode()== KeyEvent.VK_A && p1.isAlive()&& respawningTimer>=50 && !(p2.isRespawning() || p1.isRespawning())){
			if(p1.getType()==3){
				p1.setDT(-1);
			}else{
				p1.setDT(-2);
			}
		}else if(e.getKeyCode()== KeyEvent.VK_SPACE && (respawningTimer>=50 || preGame)){
			p1.setFiring(true);
			if(p1.isRespawning()){
				p1.setRespawning(false);
			}else if(preGame){
				blueReady = true;
				String[] a = {"Ready!"};
				blueReadyMessage.setText(a);
			}

		}
		/*
		else if(e.getKeyCode()== KeyEvent.VK_I && p2.isAlive()&& respawningTimer>=50 && (!blueRespawning || redRespawning)){
			p2.setAccel(1);
			if(redRespawning){
				redRespawning = false;
			}
		}else if(e.getKeyCode()== KeyEvent.VK_K && p2.isAlive()&& respawningTimer>=50 && (!blueRespawning || redRespawning)){
			p2.setAccel(-1);
			if(redRespawning){
				redRespawning = false;
			}
		}else if(e.getKeyCode()== KeyEvent.VK_L && p2.isAlive()&& respawningTimer>=50 && (!blueRespawning || redRespawning)){
			if(p2.getType()==3){
				p2.setDT(1);
			}else{
				p2.setDT(2);
			}
			if(redRespawning){
				redRespawning = false;
			}
		}else if(e.getKeyCode()== KeyEvent.VK_J && p2.isAlive()&& respawningTimer>=50 && (!blueRespawning || redRespawning)){
			if(p2.getType()==3){
				p2.setDT(-1);
			}else{
				p2.setDT(-2);
			}
			if(redRespawning){
				redRespawning = false;
			}
		}else if(e.getKeyCode()== KeyEvent.VK_SEMICOLON && p2.isAlive()&& (respawningTimer>=50 ||preGame) && (!blueRespawning || redRespawning)){
			p2.setFiring(true);
			if(redRespawning){
				redRespawning = false;
			}else if(preGame){
				redReady = true;
				String[] a = {"Ready!"};
				redReadyMessage.setText(a);
			}
		}
		 */

	}

	@Override
	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode()== KeyEvent.VK_W){
			p1.setAccel(0);
		}else if(e.getKeyCode()== KeyEvent.VK_S){
			p1.setAccel(0);
		}else if(e.getKeyCode()== KeyEvent.VK_D){
			p1.setDT(0);
		}else if(e.getKeyCode()== KeyEvent.VK_A){
			p1.setDT(0);
		}else if(e.getKeyCode()== KeyEvent.VK_SPACE){
			p1.setFiring(false);
		}else if(e.getKeyCode()==KeyEvent.VK_Q && (p1.isRespawning() || preGame)){
			int type = p1.getType()%4+1;			
			demoBlue.changeType(type);	
			p1.changeType(type);
		}

		/*else if(e.getKeyCode()== KeyEvent.VK_I){
			p2.setAccel(0);
		}else if(e.getKeyCode()== KeyEvent.VK_K){
			p2.setAccel(0);
		}else if(e.getKeyCode()== KeyEvent.VK_L){
			p2.setDT(0);
		}else if(e.getKeyCode()== KeyEvent.VK_J){
			p2.setDT(0);
		}else if(e.getKeyCode()== KeyEvent.VK_SEMICOLON){
			p2.setFiring(false);
		}else if(e.getKeyCode()==KeyEvent.VK_U && (redRespawning || preGame)){
			int type = p2.getType()%4+1;			
			demoRed.changeType(type);			
			p2.changeType(type);
		}
		 */
		else if(e.getKeyCode()==KeyEvent.VK_R && gameOver){
			gameOver = false;
			preGame = true;
			resetGame();
		}else if (e.getKeyCode()==KeyEvent.VK_7){
			double[] arr2 = p2.encode();
			double[] arr1 = p1.encode();
			p1.decodeUpdate(arr2);
			p2.decodeUpdate(arr1);
		}

	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 427467093173005502L;

}
