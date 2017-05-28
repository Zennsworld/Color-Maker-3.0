import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ModelManager {
	
	public final static int ONEXONE = 0;
	
	static Model [] models = new Model[0];
	static float [][] lineupoffsets = new float [0][2]; 
	
	static String [] imageNames = new String[0];
	private static int [] IDs = new int [0];
	
	public static void addImageName(String name){
		String [] copyS = new String[imageNames.length+1];
		for (int i=0;i<imageNames.length;i++){
			copyS[i] = imageNames[i];
		}
		copyS[imageNames.length] = name.split("\\.")[0];
		imageNames = copyS;
	}
	
	public static void addID(int id){
		int [] copy = new int[IDs.length+1];
		for (int i=0;i<IDs.length;i++){
			copy[i] = IDs[i];
		}
		copy[IDs.length] = id;
		IDs = copy;
	}
	
	public static void setImageIDs (int [] IDs){
		ModelManager.IDs = new int [IDs.length];
		for (int i=0;i<IDs.length;i++){
			ModelManager.IDs[i] = IDs[i];
		}
	}
	
	public static void checkError (){
		if (imageNames.length!=IDs.length){
			System.out.println("Image names length " + imageNames.length + " does not equal IDs length " + IDs.length);
		}
	}
	
	public static int getID (String name, boolean inOpenGL){
		for (int i=0;i<imageNames.length;i++){
			if (imageNames[i].equals(name)){
				if (inOpenGL){
					return IDs[i];
				} else {
					return i;					
				}
			}
		}
		System.out.println("No Texture ID exists for "+name);
		return 0;
	}
	
	public static int getID (int ID){
		return IDs[ID];
	}
	
	public static Model getModel (int ID){
		return models[ID];
	}
	
	public static void orderTextureIDs(){
		String[] nameOrders = new String[0];
		try {
			BufferedReader reader;
			reader = new BufferedReader(new InputStreamReader(ModelManager.class.getResourceAsStream("initiations\\TextureIDOrders")));
			String string;
			while ((string = reader.readLine()) != null) {
				if (string.contains("//")) {
				} else {
					String[] array = string.split(" ");
					String[] copy = new String[nameOrders.length + 1];
					for (int i = 0; i < nameOrders.length; i++) {
						copy[i] = nameOrders[i];
					}
					copy[nameOrders.length] = array[1];
					nameOrders = copy;
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} if (IDs.length < nameOrders.length){
			System.out.println("Lol wtf is there a duplicate texture ID name or something? (more names than IDs)");			
		}
		int[] tempIDs = new int[nameOrders.length];
		for (int i=0;i<nameOrders.length;i++){
			tempIDs[i] = getID(nameOrders[i], true);
		}
		int[] copyIDs = new int[IDs.length];
		String[] copyNames = new String[imageNames.length];
		for (int i=0;i<IDs.length;i++){
			copyIDs[i] = IDs[i];
			copyNames[i] = imageNames[i];
		}
		int smallest;
		if (IDs.length>tempIDs.length){
			smallest = tempIDs.length;
		} else {
			smallest = IDs.length;
		}
		for (int i=0;i<smallest;i++){
			IDs[i] = tempIDs[i];
			imageNames[i] = nameOrders[i];
		}
		int [] originalIDsUnused = new int [0];
		String [] originalNamesUnused = new String [0];
		for (int i=0;i<copyIDs.length;i++){
			boolean used = false;
			for (int j=0;j<tempIDs.length;j++){
				if (copyIDs[i]==tempIDs[j]){
					used = true;
					break;
				}
			}
			if (used==false){
				int [] copyOIU = new int [originalIDsUnused.length+1];
				String [] copyONU = new String [originalNamesUnused.length+1];
				for (int j=0;j<originalIDsUnused.length;j++){
					copyOIU[j] = originalIDsUnused[j];
					copyONU[j] = originalNamesUnused[j];
				}
				copyOIU [originalIDsUnused.length] = copyIDs[i];
				copyONU [originalNamesUnused.length] = copyNames[i];
				originalIDsUnused = copyOIU;
				originalNamesUnused = copyONU;
			}
		}
		for (int i=0;i<originalIDsUnused.length;i++){
			IDs[tempIDs.length+i] = originalIDsUnused[i];
			imageNames[tempIDs.length+i] = originalNamesUnused[i];
		}
		/*for (int i=0;i<imageNames.length;i++){
			System.out.println(IDs[i] + " " + imageNames[i]);
		}*/
	}
	
	public static void addModel (Model model, float offx, float offy){
		Model [] copy = new Model[models.length+1];
		float [][] copyOff = new float [lineupoffsets.length+1][2];
		for (int i=0;i<models.length;i++){
			copy[i] = models[i];
			copyOff[i][0] = lineupoffsets[i][0];
			copyOff[i][1] = lineupoffsets[i][1];
		}
		copyOff[lineupoffsets.length][0] = offx;
		copyOff[lineupoffsets.length][1] = offy;
		copy[models.length] = model;
		models = copy;
		lineupoffsets = copyOff;
	}
}
