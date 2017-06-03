
public class Component_Piece extends Component {

	private Component_Piece(float x, float y, float z, double xr, double yr, double zr, float width, float height, float depth, boolean events, int modelID, int textureID, int layer, String type, float[] texUV) {
		super(x, y, z, xr, yr, zr, width, height, depth, events, modelID, textureID, layer, type);
		this.texUV = texUV;
		float [] texUVP = {0,0.5f,0,0.5f};
		this.texUVP = texUVP;
	}
	
	public static Component_Piece get_Component_Piece_X (float x, float y, float z, double xr, double yr, double zr, float width, float height, float depth, boolean events, int modelID, int textureID, int layer, String type, float[] texUV){
		return new Component_Piece(x,y,z,xr,yr,zr,width,height,depth,events,modelID,textureID,layer,type,texUV){
			public void updateMatricies (double increment){
				float[] scaleMatrix = { width, 0, 0, 0, 0, (float) (height - increment), 0, 0, 0, 0, (float) (depth + increment), 0, 0, 0, 0, 1 };
				this.scaleMatrix = scaleMatrix;
				float[] positionMatrix = { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, x * 2, (float) (y * 2 + -increment * Math.sin(xr) + (-increment) * Math.cos(xr)), (float) (-z * 2 + (increment) * Math.cos(xr) + (increment) * -Math.sin(xr)), 1 };
				this.positionMatrix = positionMatrix;
				float [] texUVP = {0,(float) (depth + increment),0, (float) (height - increment)};
				this.texUVP = texUVP;
			}
		};
	}
	
	public static Component_Piece get_Component_Piece_Y (float x, float y, float z, double xr, double yr, double zr, float width, float height, float depth, boolean events, int modelID, int textureID, int layer, String type, float[] texUV){
		return new Component_Piece(x,y,z,xr,yr,zr,width,height,depth,events,modelID,textureID,layer,type,texUV){
			public void updateMatricies (double increment){
				float[] scaleMatrix = { (float) (width + increment), 0, 0, 0, 0, height, 0, 0, 0, 0, (float) (depth - increment), 0, 0, 0, 0, 1 };
				this.scaleMatrix = scaleMatrix;
				float[] positionMatrix = { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, (float) (x * 2 + (increment) * Math.cos(yr) + (-increment) * -Math.sin(yr)), y * 2 , (float) (-z * 2 + increment * Math.sin(yr) + (-increment) * Math.cos(yr)), 1 };
				this.positionMatrix = positionMatrix;
				float [] texUVP = {0,(float) (width + increment),0, (float) (depth - increment)};
				this.texUVP = texUVP;
			}
		};
	}
	
	public static Component_Piece get_Component_Piece_Z (float x, float y, float z, double xr, double yr, double zr, float width, float height, float depth, boolean events, int modelID, int textureID, int layer, String type, float[] texUV){
		return new Component_Piece(x,y,z,xr,yr,zr,width,height,depth,events,modelID,textureID,layer,type,texUV){
			public void updateMatricies (double increment){
				float[] scaleMatrix = { (float) (width + increment), 0, 0, 0, 0, (float) (height - increment), 0, 0, 0, 0, depth, 0, 0, 0, 0, 1 };
				this.scaleMatrix = scaleMatrix;
				float[] positionMatrix = { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, (float) (x * 2 + (increment) * Math.cos(zr) + (-increment) * -Math.sin(zr)), (float) (y * 2 + increment * Math.sin(zr) + (-increment) * Math.cos(zr)), -z * 2, 1 };
				this.positionMatrix = positionMatrix;
				float [] texUVP = {0,(float) (width + increment),0, (float) (height - increment)};
				this.texUVP = texUVP;
			}
		};
	}

	double speed = 1d / 1000d;
	double current = Math.PI / 2;
	double increment;
	boolean change = false;
	boolean out = true;
	double PI = Math.PI;

	public void update(long ns) {
		if (change) {
			draw = true;
			increment = (Math.cos(current)) / 2;
			updateMatricies(increment);
			double ms = ns / 1000000d;
			if (out) {
				current += speed * ms;
			} else {
				current -= speed * ms;
			}
			if (current >= PI && out) {
				updateMatricies(-0.5f);
				current = PI;
				change = false;
				out = false;
				draw = false;
			} else if (current <= PI/2 && out == false) {
				updateMatricies(0);
				current = PI / 2;
				change = false;
				out = true;
			}
		}
	}
	
	public void updateMatricies (double increment){
		
	}
	
}
