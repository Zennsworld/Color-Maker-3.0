import com.jogamp.opengl.math.FloatUtil;

public class Camara {
	
	float xoff;
	float yoff;
	float zoff;
	double xr;
	double yr;
	double zr;
	
	public float[] translation = { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };
	public float[] rotationX = { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };
	public float[] rotationY = { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };
	public float[] rotationZ = { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };
	public float[] finalRotation = new float[16];
	
	public Camara (float xoff, float yoff, float zoff, double xr, double yr, double zr){
		this.xoff = xoff;
		this.yoff = yoff;
		this.zoff = zoff;
		this.xr = xr;
		this.yr = yr;
		this.zr = zr;
		moveTranslation(xoff, yoff, zoff);
		rotateX(xr);
		rotateY(yr);
		rotateZ(zr);
		updateFinalRotation();
	}
	
	public void moveTranslation (float xinc, float yinc, float zinc){
		xoff+=xinc;
		yoff+=yinc;
		zoff+=zinc;
		translation[12] = xoff;
		translation[13] = yoff;
		translation[14] = zoff;
	}
	
	public void rotateX (double xinc){
		xr+=xinc;
		float [] rotationX = {1,0,0,0,0,(float) Math.cos(xr),(float) Math.sin(xr),0,0,(float) -Math.sin(xr),(float) Math.cos(xr),0,0,0,0,1};
		this.rotationX = rotationX;
		updateFinalRotation();
	}
	
	public void rotateY (double yinc){
		yr+=yinc;
		float [] rotationY = {(float) Math.cos(yr),0,(float) -Math.sin(yr),0,0,1,0,0,(float)Math.sin(yr),0,(float) Math.cos(yr),0,0,0,0,1};
		this.rotationY = rotationY;
		updateFinalRotation();
	}
	
	public void rotateZ (double zinc){
		zr+=zinc;
		float [] rotationZ = {1,0,0,0,0,(float) Math.cos(zr),(float) Math.sin(zr),0,0,(float) -Math.sin(zr),(float) Math.cos(zr),0,0,0,0,1};
		this.rotationZ = rotationZ;
		updateFinalRotation();
	}
	
	public void updateFinalRotation (){
		finalRotation = new float [16];
		finalRotation = FloatUtil.multMatrix(rotationX,rotationY,finalRotation);
		finalRotation = FloatUtil.multMatrix(finalRotation,rotationZ);
	}
	
	
}
