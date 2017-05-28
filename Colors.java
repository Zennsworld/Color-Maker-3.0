
public class Colors {
	//0-6
	public static float [] hueToRGB (float hue, float alpha){
		float [] temp = new float [4];
		temp[0] = Math.max(Math.min(Math.abs(3-hue%6)-1,1),0);
		temp[1] = Math.max(Math.min(Math.abs(3-(hue+4)%6)-1,1),0);	
		temp[2] = Math.max(Math.min(Math.abs(3-(hue+2)%6)-1,1),0);
		temp[3] = alpha;
		return temp;
	}
}
