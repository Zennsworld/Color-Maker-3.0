import com.jogamp.opengl.math.FloatUtil;

public class Component {
	
	float x;
	float y;
	float z;
	double xr;
	double yr;
	double zr;
	float [] positionMatrix = new float[16];
	float [] rotationX = new float[16];
	float [] rotationY = new float[16];
	float [] rotationZ = new float[16];
	float [] finalRotation = new float[16];
	float width;
	float height;
	float eventx;
	float eventy;
	float eventwidth;
	float eventheight;
	boolean events;
	int modelID;
	Model model;
	int textureID;
	String type;
	
	boolean pressedAndHeld = false;
	
	//texture ID needs to be the GL ID, not the array ID (for names pass in true)
	public Component (float x, float y, float z, double xr, double yr, double zr, float width, float height, boolean events, int modelID, int textureID, String type){
		this.x = x;
		this.y = y;
		this.z = z;
		this.xr = xr;
		this.yr = yr;
		this.zr = zr;
		this.width = width;
		this.height = height;
		eventx = x;
		eventy = y;
		eventwidth = width;
		eventheight = height;
		this.events = events;
		this.modelID = modelID;
		model = ModelManager.getModel(modelID);
		this.textureID = textureID;
		this.type = type;
		updateLocalPositionMatrix();
		rotateX(xr);
		rotateY(yr);
		rotateZ(zr);
		updateFinalRotation();
	}
	
	public void pressed (float x, float y){
		pressedAndHeld = true;
	}
	
	public void released (float x, float y){
		if (pressedAndHeld){
			
		}
		pressedAndHeld = false;
	}
	
	public void updateLocalPositionMatrix (){
		float[] positionMatrix = {1,0,0,0,0,1,0,0,0,0,1,0,x*2,y*2,-z*2,1};
		this.positionMatrix = positionMatrix;
	}
	
	public void rotateX (double xinc){
		xr+=xinc;
		float [] rotationX = {1,0,0,0,0,(float) Math.cos(xr),(float) Math.sin(xr),0,0,(float) -Math.sin(xr),(float) Math.cos(xr),0,0,0,0,1};
		this.rotationX = rotationX;
	}
	
	public void rotateY (double yinc){
		yr+=yinc;
		float [] rotationY = {(float) Math.cos(yr),0,(float) Math.sin(yr),0,0,1,0,0,(float) -Math.sin(yr),0,(float) Math.cos(yr),0,0,0,0,1};
		this.rotationY = rotationY;
	}
	
	public void rotateZ (double zinc){
		zr+=zinc;
		float [] rotationZ = {1,0,0,0,0,(float) Math.cos(zr),(float) Math.sin(zr),0,0,(float) -Math.sin(zr),(float) Math.cos(zr),0,0,0,0,1};
		this.rotationZ = rotationZ;
	}
	
	public void updateFinalRotation (){
		finalRotation = new float [16];
		finalRotation = FloatUtil.multMatrix(rotationX,rotationY,finalRotation);
		finalRotation = FloatUtil.multMatrix(finalRotation,rotationZ);
	}
	
	public void update (long ns){
		
	}
	
	public void setMouseEventSize (float x, float y, float width, float height){
		eventx = x;
		eventy = y;
		eventwidth = width;
		eventheight = height;
	}
	
}
