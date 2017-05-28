
public class Model {
	
	private int VAO;
	private int VBO;
	private int IBO;
	private int indexLength;
	
	private int drawType;
	
	public Model (int VAO, int VBO, int IBO, int indexLength, int drawType){
		this.VAO = VAO;
		this.VBO = VBO;
		this.IBO = IBO;
		this.indexLength = indexLength;
		this.drawType = drawType;
	}
	
	public int getVAO (){
		return VAO;
	}
	
	public int getVBO (){
		return VBO;
	}
	
	public int getIBO (){
		return IBO;
	}
	
	public int getIndexLength (){
		return indexLength;
	}
	
	public int getDrawType (){
		return drawType;
	}
}
