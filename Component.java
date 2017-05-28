public class Component {
	
	float x;
	float y;
	float z;
	float [] positionMatrix = new float[16];
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
	public Component (float x, float y, float z, float width, float height, boolean events, int modelID, int textureID, String type){
		this.x = x;
		this.y = y;
		this.z = z;
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
	
	public void update (long ns){
		
	}
	
	public void setMouseEventSize (float x, float y, float width, float height){
		eventx = x;
		eventy = y;
		eventwidth = width;
		eventheight = height;
	}
	
}
