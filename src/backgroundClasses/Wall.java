package backgroundClasses;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Wall extends Sprite{
	private boolean horizontal;
	private int length;
	private int width;
	
	public Wall(int x,int y,boolean horz,int len,int wid, Color col){
		this.setPos(x, y);
		horizontal = horz;
		length = len;
		width = wid;
		color = col;
		this.setShape(loadBody());
	}
	public boolean isHoriz(){
		return horizontal;
	}
	public void draw(Graphics gvar){
		Rectangle rect = loadBody();
		this.setShape(rect);
		
		Graphics2D g2 = (Graphics2D) gvar;
		g2.setColor(color);
		g2.fill(rect);
	}
	private Rectangle loadBody(){
		Rectangle rect;
		if(horizontal){
			rect = new Rectangle((int)(xPos-length/2),(int)(yPos-width/2),length,width);
		}else{
			rect = new Rectangle((int)(xPos-width/2),(int)(yPos-length/2),width,length);
		}
		return rect;
	}

}
