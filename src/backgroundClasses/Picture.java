package backgroundClasses;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class Picture extends Sprite{
	private BufferedImage image;
	private double size;
	private AffineTransform at = new AffineTransform();
	
	public Picture(BufferedImage img,int x,int y,int ang,double scale){
		image = img;
		this.setPos(x, y);
		this.setAngle(ang);
		size = scale;
		loadShape();
	}
	public Picture(BufferedImage img,int x,int y,double scale){
		this(img,x,y,0,scale);
	}
	protected void draw(Graphics gvar){
		Graphics2D g2 = (Graphics2D) gvar;

		at.setToIdentity();
		at.rotate(angle/180.0*Math.PI,this.xPos,this.yPos);
		at.translate(this.xPos-(int)(image.getWidth()/2*size), this.yPos-(int)(image.getHeight()/2*size));
		at.scale(size, size);

		g2.drawImage(image, at,null);

		loadShape();
	}
	public void loadShape(){
		at.setToIdentity();
		at.rotate(angle/180.0*Math.PI,this.xPos,this.yPos);
		at.translate(this.xPos-(int)(image.getWidth()/2*size), this.yPos-(int)(image.getHeight()/2*size));
		at.scale(size, size);
		
		Shape hitBox = at.createTransformedShape(image.getData().getBounds());
		this.setShape(hitBox);
	}
}
