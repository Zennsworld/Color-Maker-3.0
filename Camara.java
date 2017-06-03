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
	public float[] finalRotationf = new float[16];
	
	public Camara (float xoff, float yoff, float zoff, double xr, double yr, double zr){
		this.xoff = xoff;
		this.yoff = yoff;
		this.zoff = zoff;
		this.xr = xr;
		this.yr = yr;
		this.zr = zr;
		translation[12] = xoff;
		translation[13] = yoff;
		translation[14] = zoff;
		rotateX(xr);
		rotateY(yr);
		rotateZ(zr);
		updateFinalRotation();
	}
	
	public void moveTranslation (float xinc, float yinc, float zinc){
		float [] pos = {xinc, yinc, zinc, 1};
		float [] temp = new float[4];
		finalRotationf = FloatUtil.invertMatrix(finalRotation, finalRotationf);
		temp = FloatUtil.multMatrixVec(finalRotationf, pos, temp);
		xoff+=temp[0];
		yoff+=temp[1];
		zoff+=temp[2];
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
		float [] rotationY = {(float) Math.cos(yr),0,(float) Math.sin(yr),0,0,1,0,0,(float) -Math.sin(yr),0,(float) Math.cos(yr),0,0,0,0,1};
		this.rotationY = rotationY;
		updateFinalRotation();
	}
	
	public void rotateZ (double zinc){
		zr+=zinc;
		float [] rotationZ = {(float) Math.cos(zr),(float) Math.sin(zr),0,0,(float) -Math.sin(zr),(float) Math.cos(zr),0,0,0,0,1,0,0,0,0,1};
		this.rotationZ = rotationZ;
		updateFinalRotation();
	}
	
	public void updateFinalRotation (){
		finalRotation = new float [16];
		finalRotation = FloatUtil.multMatrix(rotationX,rotationY,finalRotation);
		finalRotation = FloatUtil.multMatrix(finalRotation,rotationZ);
	}
	
	double amountToMove = 0;
	double amountToMoveX = 0;
	double amountToMoveZ = 0;
	double moveSpeed = 1d/100000000d;
	double moveAdd = 1d/500000000d;
	double mouseRotSpeed = 1d/40d;
	double angle45 = Math.sin(Math.PI/4);
	
	int direction = 0;
	//Right, Left, Up, Down
	int [] directions = {-3,3,2,-2};
	boolean [] directionDown = {false,false,false,false};
	
	public void update (long ns){
		if (direction != 0) {
			amountToMove = amountToMove + (ns)*moveAdd;
		}
		switch (direction){
			case(-3):
			amountToMoveX = ((ns)*moveSpeed*amountToMove);
			break;
			case(3):
			amountToMoveX = -((ns)*moveSpeed*amountToMove);
			break;		
			case(2):
			amountToMoveZ = ((ns)*moveSpeed*amountToMove);
			break;		
			case(-2):
			amountToMoveZ = -((ns)*moveSpeed*amountToMove);
			break;	
			case(-1):
			amountToMoveX = ((ns)*moveSpeed*amountToMove)*angle45;
			amountToMoveZ = ((ns)*moveSpeed*amountToMove)*angle45;
			break;
			case(1):
			amountToMoveX = -((ns)*moveSpeed*amountToMove)*angle45;
			amountToMoveZ = -((ns)*moveSpeed*amountToMove)*angle45;
			break;		
			case(5):
			amountToMoveX = -((ns)*moveSpeed*amountToMove)*angle45;
			amountToMoveZ = ((ns)*moveSpeed*amountToMove)*angle45;
			break;		
			case(-5):
			amountToMoveX = ((ns)*moveSpeed*amountToMove)*angle45;
			amountToMoveZ = -((ns)*moveSpeed*amountToMove)*angle45;
			break;
			case(0):
			amountToMoveX -= (ns)*moveSpeed*amountToMoveX*angle45;
			amountToMoveZ -= (ns)*moveSpeed*amountToMoveZ*angle45;
		}
		moveTranslation((float)(amountToMoveX), 0, (float) (amountToMoveZ));
		rotateY((float)(mouseRotSpeed * YMinc));
		rotateX((float)(mouseRotSpeed * XMinc));
		amountToMove -= (ns)*moveSpeed*amountToMove;
	}

	double XMinc, YMinc;
	double N, P;
	{
		N = -1;
		P = (1050-2d)/1050;
	}
	boolean setXM = false;
	
	public void mouseMoved (double mouseX, double mouseY){
		if (mouseX <= N){
			YMinc = 1;
			XMinc = mouseY;
			setXM = true;
		} else if (mouseX >= P){	
			YMinc = -1;
			XMinc = mouseY;
			setXM = true;
		} else {
			YMinc = 0;
		}
		if (mouseY <= N){
			XMinc = -1;
			YMinc = -mouseX;
		} else if (mouseY >= P){	
			XMinc = 1;
			YMinc = -mouseX;
		} else {
			if (setXM==false){
				XMinc = 0;
			}
		}
		setXM = false;
	}
	
	public void keyState (int key, boolean down){
		if (down&&directionDown[key]==false){
			direction += directions[key];
			directionDown [key] = true;
		} else if (down==false&&directionDown[key]) {
			direction -= directions[key];			
			directionDown [key] = false;
		}
	}
}
