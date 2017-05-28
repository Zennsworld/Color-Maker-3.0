import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;

import com.jogamp.nativewindow.util.Dimension;
import com.jogamp.newt.Display;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Screen;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_INVALID_ENUM;
import static com.jogamp.opengl.GL.GL_INVALID_FRAMEBUFFER_OPERATION;
import static com.jogamp.opengl.GL.GL_INVALID_OPERATION;
import static com.jogamp.opengl.GL.GL_INVALID_VALUE;
import static com.jogamp.opengl.GL.GL_NO_ERROR;
import static com.jogamp.opengl.GL.GL_OUT_OF_MEMORY;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import com.jogamp.opengl.math.FloatUtil;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Frame implements GLEventListener, KeyListener, MouseListener {

	public final static int VAO = 0;
	public final static int VBO = 1;
	public final static int IBO = 2;
	
	public final static int POSITION = 0;
	public final static int LIGHT = 2;
	public final static int COLOR = 3;
	
	java.awt.Dimension dim = Toolkit.getDefaultToolkit().getScreenSize(); 
	int sx, sy, swidth, sheight;
	
    private int screenIdx = 0;
    public GLWindow glWindow;
    public FPSAnimator animator;
	
    private int textureIDs [];
	private int textureShaderID;
	
	public final static int DYNTEXTURESIZE = 1024;
	public final static float COLORUPDATEAMOUNT = 256f/(float)DYNTEXTURESIZE;
	
	private int dyntextureIDs[] = new int [6];
	private int dynfboIDs[] = new int [6];
	
	public final static int SBwithH1 = 0;
	public final static int SBwithH2 = 1;
	public final static int HBwithS1 = 2;
	public final static int HBwithS2 = 3;
	public final static int SHwithB1 = 4;
	public final static int SHwithB2 = 5;
	
	private int [] uniformIDs = new int [6];
	
	private int hue1ID;

    public Frame () {
    	Display display = NewtFactory.createDisplay(null);
		Screen screen = NewtFactory.createScreen(display, screenIdx);
		GLProfile glProfile = GLProfile.get(GLProfile.GL4);
		GLCapabilities glCapabilities = new GLCapabilities(glProfile);
		glWindow = GLWindow.create(screen, glCapabilities);

		glWindow.setSize(dim.width, dim.height);
		glWindow.setPosition(0, 0);
		glWindow.setUndecorated(true);
		glWindow.setAlwaysOnTop(true);
		glWindow.setFullscreen(false);
		glWindow.setPointerVisible(true);
		glWindow.confinePointer(false);
		glWindow.setVisible(true);
		
		glWindow.addGLEventListener(this);

		animator = new FPSAnimator(glWindow, 120);
		animator.start();   
    }

    private float[][] vertexData = new float[][] {
    	{(float) -1, (float) -1, (float) +1, (float) 1, (float) 1,
    	(float) +1, (float) -1, (float) +1, (float) 0, (float) 1,
    	(float) -1, (float) +1, (float) +1, (float) 1, (float) 0, 
    	(float) +1, (float) +1, (float) +1, (float) 0, (float) 0,
    	(float) -1, (float) +1, (float) +1, (float) 1, (float) 0,
    	(float) +1, (float) -1, (float) +1, (float) 0, (float) 1},
    	
		{(float) -1, (float) -1, (float) +1, (float) 1, (float) 1,
		(float) +1, (float) -1, (float) +1, (float) 0, (float) 1,
		(float) -1, (float) +1, (float) +1, (float) 1, (float) 0, 
		(float) +1, (float) +1, (float) +1, (float) 0, (float) 0,
		(float) -1, (float) +1, (float) +1, (float) 1, (float) 0,
		(float) +1, (float) -1, (float) +1, (float) 0, (float) 1,
		
		(float) -1, (float) +1, (float) -1, (float) 1, (float) 1,
		(float) +1, (float) +1, (float) -1, (float) 0, (float) 1,
		(float) -1, (float) +1, (float) +1, (float) 1, (float) 0, 
		(float) +1, (float) +1, (float) +1, (float) 0, (float) 0,
		(float) -1, (float) +1, (float) +1, (float) 1, (float) 0,
		(float) +1, (float) +1, (float) -1, (float) 0, (float) 1,
		
		(float) -1, (float) -1, (float) -1, (float) 1, (float) 1,
		(float) +1, (float) -1, (float) -1, (float) 0, (float) 1,
		(float) -1, (float) -1, (float) +1, (float) 1, (float) 0, 
		(float) +1, (float) -1, (float) +1, (float) 0, (float) 0,
		(float) -1, (float) -1, (float) +1, (float) 1, (float) 0,
		(float) +1, (float) -1, (float) -1, (float) 0, (float) 1,
		
		(float) +1, (float) -1, (float) -1, (float) 1, (float) 1,
		(float) +1, (float) -1, (float) +1, (float) 0, (float) 1,
		(float) +1, (float) +1, (float) -1, (float) 1, (float) 0, 
		(float) +1, (float) +1, (float) +1, (float) 0, (float) 0,
		(float) +1, (float) +1, (float) -1, (float) 1, (float) 0,
		(float) +1, (float) -1, (float) +1, (float) 0, (float) 1,
		
		(float) -1, (float) -1, (float) -1, (float) 1, (float) 1,
		(float) -1, (float) -1, (float) +1, (float) 0, (float) 1,
		(float) -1, (float) +1, (float) -1, (float) 1, (float) 0, 
		(float) -1, (float) +1, (float) +1, (float) 0, (float) 0,
		(float) -1, (float) +1, (float) -1, (float) 1, (float) 0,
		(float) -1, (float) -1, (float) +1, (float) 0, (float) 1,
		
		(float) -1, (float) -1, (float) -1, (float) 1, (float) 1,
		(float) +1, (float) -1, (float) -1, (float) 0, (float) 1,
		(float) -1, (float) +1, (float) -1, (float) 1, (float) 0, 
		(float) +1, (float) +1, (float) -1, (float) 0, (float) 0,
		(float) -1, (float) +1, (float) -1, (float) 1, (float) 0,
		(float) +1, (float) -1, (float) -1, (float) 0, (float) 1},
		};
		
	private int [] drawTypes = {GL4.GL_TRIANGLES, GL4.GL_TRIANGLES};
    
    private short[][] indexData = new short[vertexData.length][];
    private int[][] objects = new int[3][vertexData.length];
    
    {
		for (int i = 0; i < vertexData.length; i++) {
			indexData[i] = new short [vertexData[i].length/5];
			for (int j = 0; j < indexData[i].length; j++) {
				indexData[i][j] = (short) j;
			}
		}
	}
    
    private int modelMatrixID, projMatrixID;
    private int programScreen, programTexture;
    private final String SHADERS_ROOT = "/shaders";
    
    private float modelAlignmentX = -1;
	private float modelAlignmentY = -1;
    
    private float[] identity = {1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1};
    //private float[] rotationX = {1,0,0,0,0,(float) Math.cos(theataX),(float) Math.sin(theataX),0,0,(float) -Math.sin(theataX),(float) Math.cos(theataX),0,0,0,0,1};
    //private float[] rotationY = {(float) Math.cos(theataY),0,(float) -Math.sin(theataY),0,0,1,0,0,(float)Math.sin(theataY),0,(float) Math.cos(theataY),0,0,0,0,1};
    private float[] rotationX = identity;
    private float[] rotationY = identity;
   // private float[] rotationZ = identity;
    private float[] translation = { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };
    private float[] finalM = FloatUtil.multMatrix(rotationX,rotationY);
 //   private float[] finalM = translation;
    private float[] modelToClip = new float[16];
    private float[] projMatrix = new float[16];
    private float[] Ftranslations = new float[16];

    long nanoNow, nanoBefore;
    
    Component [] coms = new Component [2];
    
    @Override
	public void init(GLAutoDrawable drawable) {
		System.out.println("init");

		GL4 gl4 = drawable.getGL().getGL4();

		initVbo(gl4);

		initIbo(gl4);	

		initVao(gl4);

		for (int i=0;i<objects[0].length;i++){
			ModelManager.addModel(new Model (objects[VAO][i], objects[VBO][i], objects[IBO][i], indexData[i].length, drawTypes[i]), modelAlignmentX-vertexData[i][0], modelAlignmentY-vertexData[i][1]);
		}
		
		initTextures(gl4, "images");
		
		int [] temp = dynamicTextureInit(gl4);
		
		for (int i=0;i<temp.length/2;i++){
			dyntextureIDs[i] = temp[i*2];
			dynfboIDs[i] = temp[i*2+1];
		}
		
		square = ModelManager.getModel(ModelManager.ONEXONESQUARE);
				
		initProgram(gl4);
		
		addListeners();
		
		initComponents();
		
		gl4.glEnable(GL4.GL_DEPTH_TEST);
		gl4.glEnable(GL4.GL_BLEND);
		gl4.glBlendFunc(GL4.GL_SRC_ALPHA,GL4.GL_ONE_MINUS_SRC_ALPHA);
		
		nanoBefore = System.nanoTime();
	}

	private void initVbo(GL4 gl4) {
		gl4.glGenBuffers(vertexData.length, objects[VBO], 0);
		{
			for (int i = 0; i < vertexData.length; i++) {
				gl4.glBindBuffer(GL4.GL_ARRAY_BUFFER, objects[VBO][i]);
				int size = vertexData[i].length * GLBuffers.SIZEOF_FLOAT;
				FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData[i]);
				gl4.glBufferData(GL4.GL_ARRAY_BUFFER, size, vertexBuffer, GL4.GL_STATIC_DRAW);
			}
		}
		gl4.glBindBuffer(GL4.GL_ARRAY_BUFFER,0);

		checkError(gl4, "initVbo");
	}

	private void initIbo(GL4 gl4) {

		gl4.glGenBuffers(indexData.length, objects[IBO], 0);
		{
			for (int i = 0; i < indexData.length; i++) {
				gl4.glBindBuffer(GL4.GL_ELEMENT_ARRAY_BUFFER, objects[IBO][i]);
				ShortBuffer indexBuffer = GLBuffers.newDirectShortBuffer(indexData[i]);
				int size = indexData[i].length * GLBuffers.SIZEOF_SHORT;
				gl4.glBufferData(GL4.GL_ELEMENT_ARRAY_BUFFER, size, indexBuffer, GL4.GL_STATIC_DRAW);
			}
		}
		gl4.glBindBuffer(GL4.GL_ELEMENT_ARRAY_BUFFER, 0);

		checkError(gl4, "initIbo");
	}

	private void initVao(GL4 gl4) {
		gl4.glGenVertexArrays(vertexData.length, objects[VAO], 0);
		for (int i = 0; i < vertexData.length; i++) {
			gl4.glBindVertexArray(objects[VAO][i]);
			{
				gl4.glBindBuffer(GL4.GL_ELEMENT_ARRAY_BUFFER, objects[IBO][i]);
				{
					gl4.glBindBuffer(GL4.GL_ARRAY_BUFFER, objects[VBO][i]);
					{
						int stride = (3 + 2) * GLBuffers.SIZEOF_FLOAT;
						gl4.glEnableVertexAttribArray(POSITION);
						gl4.glVertexAttribPointer(POSITION, 3, GL4.GL_FLOAT, false, stride, 0 * GLBuffers.SIZEOF_FLOAT);
						gl4.glEnableVertexAttribArray(COLOR);
						gl4.glVertexAttribPointer(COLOR, 2, GL4.GL_FLOAT, false, stride, 3 * GLBuffers.SIZEOF_FLOAT);
					}
					gl4.glBindBuffer(GL4.GL_ARRAY_BUFFER, 0);
				}
			}
		}
		gl4.glBindVertexArray(0);

		checkError(gl4, "initVao");
	}

    private void initProgram(GL4 gl4) {
    	ShaderCode vertShader = ShaderCode.create(gl4, GL_VERTEX_SHADER, 1, this.getClass(),
                SHADERS_ROOT, (new String[]{"vs_screen"}), "", null, true);
        ShaderCode fragShader = ShaderCode.create(gl4, GL_FRAGMENT_SHADER, 1, this.getClass(),
                SHADERS_ROOT, (new String[]{"fs_screen"}), "", null, true);
        ShaderCode vertShaderTexture = ShaderCode.create(gl4, GL_VERTEX_SHADER, 1, this.getClass(),
        		SHADERS_ROOT, (new String[]{"vs_textures"}), "", null, true);
        ShaderCode fragShaderSBWH1 = ShaderCode.create(gl4, GL_FRAGMENT_SHADER, 1, this.getClass(),
        		SHADERS_ROOT, (new String[]{"fs_texture_SBWH1"}), "", null, true);

        ShaderProgram shaderProgram = new ShaderProgram();
        shaderProgram.add(vertShader);
        shaderProgram.add(fragShader);
        ShaderProgram shaderProgramTexture = new ShaderProgram();
        shaderProgramTexture.add(vertShaderTexture);
        shaderProgramTexture.add(fragShaderSBWH1);

        shaderProgram.init(gl4);
        shaderProgramTexture.init(gl4);

        programScreen = shaderProgram.program();
        programTexture = shaderProgramTexture.program();
        
        gl4.glBindAttribLocation(programScreen, Semantic.Attr.POSITION, "positionV");
        gl4.glBindFragDataLocation(programScreen, Semantic.Frag.COLOR, "positionF");
        gl4.glBindFragDataLocation(programScreen, Semantic.Frag.COLOR, "outputColorF");

        gl4.glBindAttribLocation(programScreen, Semantic.Attr.POSITION, "position");
        gl4.glBindAttribLocation(programScreen, Semantic.Attr.COLOR, "vertexUV");
        gl4.glBindFragDataLocation(programScreen, Semantic.Frag.COLOR, "outputColor");
        gl4.glBindFragDataLocation(programScreen, Semantic.Frag.COLOR, "fragmentUV");
        
        shaderProgram.link(gl4, System.out);
        shaderProgramTexture.link(gl4, System.out);
        
        hue1ID = gl4.glGetUniformLocation(programTexture, "hue1");
        
        uniformIDs[SBwithH1] = hue1ID;
        
        projMatrixID = gl4.glGetUniformLocation(programScreen, "projMatrix");
        modelMatrixID = gl4.glGetUniformLocation(programScreen, "modelMatrix");
        textureShaderID = gl4.glGetUniformLocation(programScreen, "textures");

        checkError(gl4, "initProgram");
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
		System.out.println("dispose");

		GL4 gl4 = drawable.getGL().getGL4();
		
		gl4.glDeleteProgram(programTexture);
		gl4.glDeleteProgram(programScreen);
		gl4.glDeleteVertexArrays(vertexData.length, objects[0], 0);

		gl4.glDeleteBuffers(vertexData.length, objects[1], 0);

		gl4.glDeleteBuffers(vertexData.length, objects[2], 0);
		
		gl4.glDeleteTextures(textureIDs.length, textureIDs, 0);
		
		System.exit(0);
	}
 
	double angleSpeed = 360d/100000d;
	double currentns = 0;
	
	Model square;

	public void display(GLAutoDrawable drawable) {
		
		nanoNow = System.nanoTime();
		currentns = currentns + (nanoNow-nanoBefore)*angleSpeed/10000000d;
		nanoBefore = nanoNow;
		
		float [] rotationXT = {1,0,0,0,0,(float) Math.cos(currentns),(float) Math.sin(currentns),0,0,(float) -Math.sin(currentns),(float) Math.cos(currentns),0,0,0,0,1};
	    float [] rotationYT = {(float) Math.cos(currentns),0,(float) -Math.sin(currentns),0,0,1,0,0,(float)Math.sin(currentns),0,(float) Math.cos(currentns),0,0,0,0,1};
	    
		GL4 gl4 = drawable.getGL().getGL4();
		gl4.glClearColor(0f, 0f, 0f, 1f);
		gl4.glClearDepthf(1f);
		gl4.glClear(GL4.GL_COLOR_BUFFER_BIT | GL4.GL_DEPTH_BUFFER_BIT);
		
		gl4.glUseProgram(programTexture);

		gl4.glBindFramebuffer(GL4.GL_FRAMEBUFFER, dynfboIDs[0]);
		gl4.glViewport(0,0,1024,1024);
		
		gl4.glBindVertexArray(square.getVAO());
		{
			gl4.glUniform1f(hue1ID, 0);
			gl4.glDrawElements(GL4.GL_TRIANGLES, square.getIndexLength(), GL4.GL_UNSIGNED_SHORT, 0);
		}
		
		gl4.glBindFramebuffer(GL4.GL_FRAMEBUFFER, 0);
		 
		gl4.glUseProgram(programScreen);
		gl4.glViewport(sx, sy, swidth, sheight);
		
		{
			for (int i = 0; i < coms.length; i++) {
				gl4.glBindVertexArray(coms[i].model.getVAO());
				{
					gl4.glBindTexture(GL.GL_TEXTURE_2D,coms[i].textureID);
					gl4.glUniform1i(textureShaderID, 0);
					
					finalM = FloatUtil.multMatrix(rotationXT,rotationYT,finalM);
					Ftranslations = FloatUtil.multMatrix(coms[i].positionMatrix, translation, Ftranslations);
					modelToClip = FloatUtil.multMatrix(Ftranslations, finalM, modelToClip);
					gl4.glUniformMatrix4fv(modelMatrixID, 1, false, modelToClip, 0);
					gl4.glUniformMatrix4fv(projMatrixID, 1, false, projMatrix, 0);

					gl4.glDrawElements(coms[i].model.getDrawType(), coms[i].model.getIndexLength(), GL4.GL_UNSIGNED_SHORT, 0);
				}
			}
			gl4.glBindVertexArray(0);
		}
		gl4.glUseProgram(0);
		checkError(gl4, "display");
	}

	protected boolean checkError(GL gl, String title) {

        int error = gl.glGetError();
        if (error != GL_NO_ERROR) {
            String errorString;
            switch (error) {
                case GL_INVALID_ENUM:
                    errorString = "GL_INVALID_ENUM";
                    break;
                case GL_INVALID_VALUE:
                    errorString = "GL_INVALID_VALUE";
                    break;
                case GL_INVALID_OPERATION:
                    errorString = "GL_INVALID_OPERATION";
                    break;
                case GL_INVALID_FRAMEBUFFER_OPERATION:
                    errorString = "GL_INVALID_FRAMEBUFFER_OPERATION";
                    break;
                case GL_OUT_OF_MEMORY:
                    errorString = "GL_OUT_OF_MEMORY";
                    break;
                default:
                    errorString = "UNKNOWN";
                    break;
            }
            System.out.println("OpenGL Error(" + errorString + "): " + title);
            throw new Error();
        }
        return error == GL_NO_ERROR;
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        System.out.println("reshape");
        GL4 gl4 = drawable.getGL().getGL4();
        this.sx = x;
        this.sy = y;
        this.swidth = width;
        this.sheight = height;
        projMatrix = BuildPerspProjMat(50, (double)width/(double)height, 0.1d, 400d);
        gl4.glViewport(x, y, width, height);
    }

    public float[] BuildPerspProjMat(double fov, double aspect, double znear, double zfar) {
		double ymax = znear * (Math.tan(fov * Math.PI / 360));
		double ymin = -ymax;
		double xmax = ymax * aspect;
		double xmin = ymin * aspect;

		double width = xmax - xmin;
		double height = ymax - ymin;

		double depth = zfar - znear;
		double q = -(zfar + znear) / depth;
		double qn = -2 * (zfar * znear) / depth;

		double w = 2 * znear / width;
		double h = 2 * znear / height;
		
		float[] m = { (float) w, 0, 0, 0, 0, (float) h, 0, 0, 0, 0, (float) q, -1, 0, 0, (float) qn, 0 };
		return m;
	}

    private void initTextures(GL4 gl4, String folderName) {
		int [] dims = new int [0];
		byte [][] imageData = new byte [0][];
		try {
			File folder = new File("src\\" + folderName);
			File[] listOfFiles = folder.listFiles();
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					ModelManager.addImageName(listOfFiles[i].getName());
					BufferedImage image = ImageIO.read(Frame.class.getResourceAsStream(folderName + "\\" + listOfFiles[i].getName()));
					dims = addImageDim(dims, image);
					imageData = addImageData(imageData, image);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		textureIDs = new int [imageData.length];
		gl4.glGenTextures(imageData.length,textureIDs,0);
		ModelManager.setImageIDs(textureIDs);
		for (int i=0;i<imageData.length;i++){
			addTexture (imageData[i], textureIDs[i],dims[i*2+1], dims[i*2], gl4);
		}
		gl4.glBindTexture(GL4.GL_TEXTURE_2D, 0);
		ModelManager.checkError();
		ModelManager.orderTextureIDs();
	}
	
	public void addTexture (byte [] data, int id, int width, int height, GL4 gl4){
		gl4.glBindTexture(GL4.GL_TEXTURE_2D, id);
		gl4.glTexParameterf(GL.GL_TEXTURE_2D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_NEAREST);
		gl4.glTexParameterf(GL.GL_TEXTURE_2D, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_NEAREST);
		gl4.glTexParameterf(GL.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_S, GL4.GL_REPEAT);
		gl4.glTexParameterf(GL.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_T, GL4.GL_REPEAT);
		ByteBuffer buffer = GLBuffers.newDirectByteBuffer(data);
		gl4.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, width, height, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, buffer);
	}
	
	public int [] addImageDim (int [] dims, BufferedImage image){
		int [] copyD = new int [dims.length+2];
		for (int j = 0; j < dims.length/2; j++) {
			copyD[j*2] = dims[j*2];
			copyD[j*2+1] = dims[j*2+1];
		}
		copyD[dims.length] = image.getHeight();
		copyD[dims.length + 1] = image.getWidth();
		return copyD;
	}
	
	public byte [] addImageData (byte [] imageData, BufferedImage image){
		byte[] temp = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		int reverseAmount = temp.length/(image.getHeight()*4);
		int reverse = 0;
		imageData = new byte[temp.length];
		for (int j=0;j<temp.length/4;j++){
			int jr = j%reverseAmount;
			if (jr==0){
				reverse += reverseAmount*4;
			}
			imageData[reverse-jr*4-4] = temp[j*4+3];
			imageData[reverse-jr*4-3] = temp[j*4+2];
			imageData[reverse-jr*4-2] = temp[j*4+1];
			imageData[reverse-jr*4-1] = temp[j*4];
		}
		return imageData;
	}
	
	public byte [][] addImageData (byte [][] imageData, BufferedImage image){
		byte[][] copy = new byte[imageData.length + 1][];
		for (int j = 0; j < imageData.length; j++) {
			copy[j] = imageData[j];
		}
		byte[] temp = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		int reverseAmount = temp.length/(image.getHeight()*4);
		int reverse = 0;
		copy[imageData.length] = new byte[temp.length];
		for (int j=0;j<temp.length/4;j++){
			int jr = j%reverseAmount;
			if (jr==0){
				reverse += reverseAmount*4;
			}
			copy[imageData.length][reverse-jr*4-4] = temp[j*4+3];
			copy[imageData.length][reverse-jr*4-3] = temp[j*4+2];
			copy[imageData.length][reverse-jr*4-2] = temp[j*4+1];
			copy[imageData.length][reverse-jr*4-1] = temp[j*4];
		}
		return copy;
	}
    
	public int [] dynamicTextureInit (GL4 gl4){
		int [] IDs = new int [12];
		for (int i=0;i<6;i++){
			gl4.glGenTextures(1, IDs, i*2);
			gl4.glBindTexture(GL4.GL_TEXTURE_2D, IDs[i*2]);
			gl4.glTexParameterf(GL.GL_TEXTURE_2D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_NEAREST);
			gl4.glTexParameterf(GL.GL_TEXTURE_2D, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_NEAREST);
			gl4.glTexImage2D(GL4.GL_TEXTURE_2D, 0, GL4.GL_RGBA, DYNTEXTURESIZE, DYNTEXTURESIZE, 0, GL4.GL_RGBA, GL4.GL_UNSIGNED_BYTE, null);

			gl4.glGenFramebuffers(1, IDs, i*2+1);
			gl4.glBindFramebuffer(GL4.GL_FRAMEBUFFER, IDs[i*2+1]);
			gl4.glFramebufferTexture2D(GL4.GL_FRAMEBUFFER, GL4.GL_COLOR_ATTACHMENT0, GL4.GL_TEXTURE_2D, IDs[i*2], 0);
		}
		gl4.glBindFramebuffer(GL4.GL_FRAMEBUFFER, 0);
		gl4.glBindTexture(GL4.GL_TEXTURE_2D, 0);
		return IDs; 
	}
	
	public void addListeners (){
		glWindow.addKeyListener(this);
		glWindow.addMouseListener(this);
	}
	
	public void initComponents(){
		coms[0] = new Component(0,0,4f,1,1,true,ModelManager.ONEXONEXONECUBE, ModelManager.getID("Texture",true),"Test");
		coms[1] = new Component(4,4,4f,1,1,true,ModelManager.ONEXONEXONECUBE, dyntextureIDs[0],"Test");
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_BACK_QUOTE){
			animator.stop();
			glWindow.destroy();
		} else if (e.getKeyCode() == KeyEvent.VK_A){
			translation[12]+=2;
		} else if (e.getKeyCode() == KeyEvent.VK_D){
			translation[12]-=2;
		} else if (e.getKeyCode() == KeyEvent.VK_S){
			translation[13]+=2;
		} else if (e.getKeyCode() == KeyEvent.VK_W){
			translation[13]-=2;
		}
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		
	}

	@Override
	public void mouseWheelMoved(MouseEvent arg0) {
		translation[14]+=arg0.getRotation()[1];
	}
	
}
