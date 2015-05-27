package org.opencv.samples.colorblobdetect;
import org.opencv.core.Scalar;
public class Beacon {
	public int x;
	public int y;
	public Scalar colorTop;
	public Scalar colorBot;
	
	public Beacon(int x, int y, String colorTop, String colorBot){
		this.x=x;
		this.y=y;
		
		switch (colorTop){
			case "red":
				this.colorTop= new Scalar(255,205,141,0); break;
			case "green":
				this.colorTop= new Scalar(106,255,74,0); break;
			case "yellow":
				this.colorTop= new Scalar(39, 255, 207, 0); break;
			case "blue":
				this.colorTop= new Scalar(151,255,125,0); break;
		}
		
		switch (colorBot){
		case "red":
			this.colorBot= new Scalar(255,205,141,0); break;
		case "green":
			this.colorBot= new Scalar(106,255,74,0); break;
		case "yellow":
			this.colorBot= new Scalar(39, 255, 207, 0); break;
		case "blue":
			this.colorBot= new Scalar(151,255,125,0); break;
		}
	}
}
