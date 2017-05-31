
public class Model {
	
	private int VAO;
	private int VBO;
	private int IBO;
	private int indexLength;
	
	public Model (int VAO, int VBO, int IBO, int indexLength){
		this.VAO = VAO;
		this.VBO = VBO;
		this.IBO = IBO;
		this.indexLength = indexLength;
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
}
