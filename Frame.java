import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.jogamp.opengl.GL4ES3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;

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
	int sx, sy, swidth, sheight, cwidth, cheight;
	
    private int screenIdx = 0;
    public GLWindow glWindow;
    public FPSAnimator animator;
	
    private int textureIDs [];
	private int textureShaderID;
	
	public final static int DYNTEXTURESIZE = 1024;
	
	private int dyntextureID;
	private int [] dynfboIDs = new int [6];
	
	public final static int SBwithH = 0;
	public final static int HBwithS = 1;
	public final static int SHwithB = 2;
	public final static int HueoffIDHB = 3;
	public final static int HueoffIDSH = 4;
	public final static int offSetsIDSB = 5;
	public final static int offSetsIDHB = 6;
	public final static int offSetsIDSH = 7;
	
	private int [] uniformIDs = new int [8];

    public Frame () {
    	Display display = NewtFactory.createDisplay(null);
		Screen screen = NewtFactory.createScreen(display, screenIdx);
		GLProfile glProfile = GLProfile.get(GLProfile.GL4ES3);
		GLCapabilities glCapabilities = new GLCapabilities(glProfile);
		glWindow = GLWindow.create(screen, glCapabilities);

		glWindow.setSize(dim.width, dim.height);
		glWindow.setPosition(0, 0);
		glWindow.setUndecorated(true);
		glWindow.setAlwaysOnTop(false);
		glWindow.setFullscreen(false);
		glWindow.setPointerVisible(true);
		glWindow.confinePointer(true);
		glWindow.setVisible(true);
		
		glWindow.addGLEventListener(this);

		animator = new FPSAnimator(glWindow, 120);
		animator.start();		
    }
    
    private float [] textureRenderSquare = new float [] {
    	(float) -1, (float) -1,
    	(float) +1, (float) -1,
    	(float) -1, (float) +1,
    	(float) +1, (float) +1,
    	(float) -1, (float) +1,
    	(float) +1, (float) -1};

    private float[][] vertexData = new float[][] {    		
		{(float) -1, (float) -1, (float) +1, (float) 0, (float) 0,
		(float) +1, (float) -1, (float) +1, (float) 1, (float) 0,
		(float) -1, (float) +1, (float) +1, (float) 0, (float) 1,
		(float) +1, (float) +1, (float) +1, (float) 1, (float) 1,
		(float) -1, (float) +1, (float) +1, (float) 0, (float) 1,
		(float) +1, (float) -1, (float) +1, (float) 1, (float) 0},
		
		{(float) -1, (float) +1, (float) -1, (float) 0, (float) 0,
		(float) +1, (float) +1, (float) -1, (float) 1, (float) 0,
		(float) -1, (float) +1, (float) +1, (float) 0, (float) 1,
		(float) +1, (float) +1, (float) +1, (float) 1, (float) 1,
		(float) -1, (float) +1, (float) +1, (float) 0, (float) 1,
		(float) +1, (float) +1, (float) -1, (float) 1, (float) 0},
		
		{(float) +1, (float) -1, (float) -1, (float) 0, (float) 0,
		(float) +1, (float) -1, (float) +1, (float) 1, (float) 0,
		(float) +1, (float) +1, (float) -1, (float) 0, (float) 1,
		(float) +1, (float) +1, (float) +1, (float) 1, (float) 1,
		(float) +1, (float) +1, (float) -1, (float) 0, (float) 1,
		(float) +1, (float) -1, (float) +1, (float) 1, (float) 0},
    };
    
	private short[] indexDataTextureRender = new short[textureRenderSquare.length/2];
	private int [] VAOTR = new int [1];
	private int [] VBOTR = new int [1];
	private int [] IBOTR = new int [1];
	Model square;
	
    private short[][] indexData = new short[vertexData.length][];
    private int[][] objects = new int[3][vertexData.length];
    
    {
    	for (int i=0;i<indexDataTextureRender.length;i++){
    		indexDataTextureRender[i] = (short) i;
    	}
		for (int i = 0; i < vertexData.length; i++) {
			indexData[i] = new short [vertexData[i].length/5];
			for (int j = 0; j < indexData[i].length; j++) {
				indexData[i][j] = (short) j;
			}
		}
	}
    
    private int modelMatrixID, projMatrixID, rotationUVID, offSetsID, layerID;
    private int programScreen, programTextureSBWH, programTextureHBWS, programTextureSHWB;
    private final String SHADERS_ROOT = "/shaders";
    
    private float modelAlignmentX = -1;
	private float modelAlignmentY = -1;
    
	Camara cam = new Camara (-3,-3,0,Math.PI/10, Math.PI/10,0);
	private float[] finalS = new float[16];
	private float[] finalM = new float[16];
    private float[] modelToClip = new float[16];
    private float[] projMatrix = new float[16];
    private float[] Ftranslations = new float[16];

    long nanoNow, nanoBefore, nanoTime;
    
    Component [] coms;
    Component_Piece [] comPs;
    
    @Override
	public void init(GLAutoDrawable drawable) {
		System.out.println("init");

		GL4ES3 gl4es3 = drawable.getGL().getGL4();

		initVbo(gl4es3);

		initIbo(gl4es3);	

		initVao(gl4es3);

		for (int i=0;i<objects[0].length;i++){
			ModelManager.addModel(new Model (objects[VAO][i], objects[VBO][i], objects[IBO][i], indexData[i].length), modelAlignmentX-vertexData[i][0], modelAlignmentY-vertexData[i][1]);
		}
		
		square = new Model (VAOTR[0], VBOTR[0], IBOTR[0], indexDataTextureRender.length);
		
		initTextures(gl4es3, "images");
		
		int [] temp = dynamicTextureInit(gl4es3);
		
		dyntextureID = temp[0];
		for (int i=0;i<temp.length-1;i++){
			dynfboIDs[i] = temp[i+1];
		}
				
		initProgram(gl4es3);
		
		addListeners();
		
		initComponents();
		
		gl4es3.glEnable(GL4ES3.GL_DEPTH_TEST);
		gl4es3.glEnable(GL4ES3.GL_BLEND);
		gl4es3.glBlendFunc(GL4ES3.GL_SRC_ALPHA,GL4ES3.GL_ONE_MINUS_SRC_ALPHA);
		
		nanoBefore = System.nanoTime();
	}

	private void initVbo(GL4ES3 gl4es3) {
		gl4es3.glGenBuffers(1, VBOTR, 0);
		gl4es3.glBindBuffer(GL4ES3.GL_ARRAY_BUFFER, VBOTR[0]);
		int sizeTR = textureRenderSquare.length  * GLBuffers.SIZEOF_FLOAT;
		FloatBuffer vertexBufferTR = GLBuffers.newDirectFloatBuffer(textureRenderSquare);
		gl4es3.glBufferData(GL4ES3.GL_ARRAY_BUFFER, sizeTR, vertexBufferTR, GL4ES3.GL_STATIC_DRAW);
		
		gl4es3.glGenBuffers(vertexData.length, objects[VBO], 0);
		{
			for (int i = 0; i < vertexData.length; i++) {
				gl4es3.glBindBuffer(GL4ES3.GL_ARRAY_BUFFER, objects[VBO][i]);
				int size = vertexData[i].length * GLBuffers.SIZEOF_FLOAT;
				FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertexData[i]);
				gl4es3.glBufferData(GL4ES3.GL_ARRAY_BUFFER, size, vertexBuffer, GL4ES3.GL_STATIC_DRAW);
			}
		}
		gl4es3.glBindBuffer(GL4ES3.GL_ARRAY_BUFFER,0);

		checkError(gl4es3, "initVbo");
	}

	private void initIbo(GL4ES3 gl4es3) {
		gl4es3.glGenBuffers(1, IBOTR, 0);
		gl4es3.glBindBuffer(GL4ES3.GL_ELEMENT_ARRAY_BUFFER, IBOTR[0]);
		int sizeTR = indexDataTextureRender.length * GLBuffers.SIZEOF_SHORT;
		ShortBuffer indexBufferTR = GLBuffers.newDirectShortBuffer(indexDataTextureRender);
		gl4es3.glBufferData(GL4ES3.GL_ELEMENT_ARRAY_BUFFER, sizeTR, indexBufferTR, GL4ES3.GL_STATIC_DRAW);
		
		gl4es3.glGenBuffers(indexData.length, objects[IBO], 0);
		{
			for (int i = 0; i < indexData.length; i++) {
				gl4es3.glBindBuffer(GL4ES3.GL_ELEMENT_ARRAY_BUFFER, objects[IBO][i]);
				int size = indexData[i].length * GLBuffers.SIZEOF_SHORT;
				ShortBuffer indexBuffer = GLBuffers.newDirectShortBuffer(indexData[i]);
				gl4es3.glBufferData(GL4ES3.GL_ELEMENT_ARRAY_BUFFER, size, indexBuffer, GL4ES3.GL_STATIC_DRAW);
			}
		}
		gl4es3.glBindBuffer(GL4ES3.GL_ELEMENT_ARRAY_BUFFER, 0);

		checkError(gl4es3, "initIbo");
	}

	private void initVao(GL4ES3 gl4es3) {
		
		gl4es3.glGenVertexArrays(1, VAOTR, 0);
		gl4es3.glBindVertexArray(VAOTR[0]);
		{
			gl4es3.glBindBuffer(GL4ES3.GL_ELEMENT_ARRAY_BUFFER, IBOTR[0]);
			{
				gl4es3.glBindBuffer(GL4ES3.GL_ARRAY_BUFFER, VBOTR[0]);
				{
					int stride = (2) * GLBuffers.SIZEOF_FLOAT;
					gl4es3.glEnableVertexAttribArray(POSITION);
					gl4es3.glVertexAttribPointer(POSITION, 2, GL4ES3.GL_FLOAT, false, stride, 0 * GLBuffers.SIZEOF_FLOAT);
				}
				gl4es3.glBindBuffer(GL4ES3.GL_ARRAY_BUFFER, 0);
			}
		}
		
		gl4es3.glGenVertexArrays(vertexData.length, objects[VAO], 0);
		for (int i = 0; i < vertexData.length; i++) {
			gl4es3.glBindVertexArray(objects[VAO][i]);
			{
				gl4es3.glBindBuffer(GL4ES3.GL_ELEMENT_ARRAY_BUFFER, objects[IBO][i]);
				{
					gl4es3.glBindBuffer(GL4ES3.GL_ARRAY_BUFFER, objects[VBO][i]);
					{
						int stride = (3 + 2) * GLBuffers.SIZEOF_FLOAT;
						gl4es3.glEnableVertexAttribArray(POSITION);
						gl4es3.glVertexAttribPointer(POSITION, 3, GL4ES3.GL_FLOAT, false, stride, 0 * GLBuffers.SIZEOF_FLOAT);
						gl4es3.glEnableVertexAttribArray(COLOR);
						gl4es3.glVertexAttribPointer(COLOR, 2, GL4ES3.GL_FLOAT, false, stride, 3 * GLBuffers.SIZEOF_FLOAT);
					}
					gl4es3.glBindBuffer(GL4ES3.GL_ARRAY_BUFFER, 0);
				}
			}
		}
		gl4es3.glBindVertexArray(0);

		checkError(gl4es3, "initVao");
	}

    private void initProgram(GL4ES3 gl4es3) {
    	ShaderCode vertShader = ShaderCode.create(gl4es3, GL_VERTEX_SHADER, 1, this.getClass(),
                SHADERS_ROOT, (new String[]{"vs_screen"}), "", null, true);
        ShaderCode fragShader = ShaderCode.create(gl4es3, GL_FRAGMENT_SHADER, 1, this.getClass(),
                SHADERS_ROOT, (new String[]{"fs_screen"}), "", null, true);
        ShaderCode vertShaderTexture = ShaderCode.create(gl4es3, GL_VERTEX_SHADER, 1, this.getClass(),
        		SHADERS_ROOT, (new String[]{"vs_textures"}), "", null, true);
        ShaderCode fragShaderSBWH1 = ShaderCode.create(gl4es3, GL_FRAGMENT_SHADER, 1, this.getClass(),
        		SHADERS_ROOT, (new String[]{"fs_texture_SBWH1"}), "", null, true);
        ShaderCode fragShaderHBWS1 = ShaderCode.create(gl4es3, GL_FRAGMENT_SHADER, 1, this.getClass(),
        		SHADERS_ROOT, (new String[]{"fs_texture_HBWS1"}), "", null, true);
        ShaderCode fragShaderSHWB1 = ShaderCode.create(gl4es3, GL_FRAGMENT_SHADER, 1, this.getClass(),
        		SHADERS_ROOT, (new String[]{"fs_texture_SHWB1"}), "", null, true);

        ShaderProgram shaderProgram = new ShaderProgram();
        shaderProgram.add(vertShader);
        shaderProgram.add(fragShader);
        
        ShaderProgram ShaderprogramTextureSBWH = new ShaderProgram();
        ShaderprogramTextureSBWH.add(vertShaderTexture);
        ShaderprogramTextureSBWH.add(fragShaderSBWH1);
        
        ShaderProgram shaderProgramTextureHBWS = new ShaderProgram();
        shaderProgramTextureHBWS.add(vertShaderTexture);
        shaderProgramTextureHBWS.add(fragShaderHBWS1);
        
        ShaderProgram shaderProgramTextureSHWB = new ShaderProgram();
        shaderProgramTextureSHWB.add(vertShaderTexture);
        shaderProgramTextureSHWB.add(fragShaderSHWB1);

        shaderProgram.init(gl4es3);
        ShaderprogramTextureSBWH.init(gl4es3);
        shaderProgramTextureHBWS.init(gl4es3);
        shaderProgramTextureSHWB.init(gl4es3);

        programScreen = shaderProgram.program();
        programTextureSBWH = ShaderprogramTextureSBWH.program();
        programTextureHBWS = shaderProgramTextureHBWS.program();
        programTextureSHWB = shaderProgramTextureSHWB.program();
        
        gl4es3.glBindAttribLocation(programTextureSBWH, 0, "positionV");   
        gl4es3.glBindAttribLocation(programTextureHBWS, 0, "positionV");   
        gl4es3.glBindAttribLocation(programTextureSHWB, 0, "positionV");
        gl4es3.glBindAttribLocation(programScreen, 0, "position");
        gl4es3.glBindAttribLocation(programScreen, 3, "vertexUV");
        
        shaderProgram.link(gl4es3, System.out);
        ShaderprogramTextureSBWH.link(gl4es3, System.out);
        shaderProgramTextureHBWS.link(gl4es3, System.out);
        shaderProgramTextureSHWB.link(gl4es3, System.out);
        
        uniformIDs[SBwithH] = gl4es3.glGetUniformLocation(programTextureSBWH, "hueRGB");
        uniformIDs[HBwithS] = gl4es3.glGetUniformLocation(programTextureHBWS, "sat");
        uniformIDs[SHwithB] = gl4es3.glGetUniformLocation(programTextureSHWB, "bri");
        uniformIDs[HueoffIDHB] = gl4es3.glGetUniformLocation(programTextureHBWS, "hueOffset");
        uniformIDs[HueoffIDSH] = gl4es3.glGetUniformLocation(programTextureSHWB, "hueOffset");
        uniformIDs[offSetsIDSB] = gl4es3.glGetUniformLocation(programTextureSBWH, "offSets");
        uniformIDs[offSetsIDHB] = gl4es3.glGetUniformLocation(programTextureHBWS, "offSets");
        uniformIDs[offSetsIDSH] = gl4es3.glGetUniformLocation(programTextureSHWB, "offSets");
        
        projMatrixID = gl4es3.glGetUniformLocation(programScreen, "projMatrix");
        modelMatrixID = gl4es3.glGetUniformLocation(programScreen, "modelMatrix");
        rotationUVID = gl4es3.glGetUniformLocation(programScreen, "rotationUV");
        offSetsID = gl4es3.glGetUniformLocation(programScreen, "offSets");
        layerID = gl4es3.glGetUniformLocation(programScreen, "layer");
        textureShaderID = gl4es3.glGetUniformLocation(programScreen, "textures");

        checkError(gl4es3, "initProgram");
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
		System.out.println("dispose");

		GL4ES3 gl4es3 = drawable.getGL().getGL4();
		
		gl4es3.glDeleteProgram(programTextureSBWH);
		gl4es3.glDeleteProgram(programTextureHBWS);
		gl4es3.glDeleteProgram(programScreen);
		gl4es3.glDeleteVertexArrays(vertexData.length, objects[0], 0);

		gl4es3.glDeleteBuffers(vertexData.length, objects[1], 0);

		gl4es3.glDeleteBuffers(vertexData.length, objects[2], 0);
		
		if (textureIDs.length>0){
			gl4es3.glDeleteTextures(textureIDs.length, textureIDs, 0);
		}
		gl4es3.glDeleteFramebuffers(dynfboIDs.length, dynfboIDs, 0);
		gl4es3.glDeleteTextures(1, new int [] {dyntextureID}, 0);
		
		System.exit(0);
	}
 
	double angleSpeed = 1d/10000000d;
	double color = 0;
	int fps = 0;
	long second = 0;

	public void display(GLAutoDrawable drawable) {
		nanoNow = System.nanoTime();
		nanoTime = (nanoNow-nanoBefore);
		fps++;
		second = second+nanoTime;
		if (second>=1000000000){
			System.out.println(fps);
			fps = 0;
			second=0;
		}
		cam.update(nanoTime);
		for (int i=0;i<coms.length;i++){
			coms[i].update(nanoTime);
		}
		color = color + (nanoTime)*angleSpeed;
		nanoBefore = nanoNow;
	    
		GL4ES3 gl4es3 = drawable.getGL().getGL4();
		gl4es3.glClearColor(0f, 0f, 0f, 1f);
		gl4es3.glClearDepthf(1f);
		gl4es3.glClear(GL4ES3.GL_COLOR_BUFFER_BIT | GL4ES3.GL_DEPTH_BUFFER_BIT);
		
		gl4es3.glViewport(0, 0, DYNTEXTURESIZE, DYNTEXTURESIZE);
		gl4es3.glUseProgram(programTextureSBWH);
		{

			gl4es3.glBindFramebuffer(GL4ES3.GL_FRAMEBUFFER, dynfboIDs[0]);
			gl4es3.glBindVertexArray(square.getVAO());
			{
				gl4es3.glUniform4fv(uniformIDs[offSetsIDSB], 1, new float [] {0,1,0,1}, 0);
				gl4es3.glUniform4fv(uniformIDs[SBwithH], 1, Colors.hueToRGB((float) (color/60d),1), 0);
				gl4es3.glDrawElements(GL4ES3.GL_TRIANGLES, square.getIndexLength(), GL4ES3.GL_UNSIGNED_SHORT, 0);
			}
			checkError(gl4es3, "display");
			
			gl4es3.glBindFramebuffer(GL4ES3.GL_FRAMEBUFFER, dynfboIDs[1]);
			
			gl4es3.glBindVertexArray(square.getVAO());
			{
				gl4es3.glUniform4fv(uniformIDs[offSetsIDSB], 1, new float [] {0,1,0,1}, 0);
				gl4es3.glUniform4fv(uniformIDs[SBwithH], 1, Colors.hueToRGB((float) (color/60d),1), 0);
				gl4es3.glDrawElements(GL4ES3.GL_TRIANGLES, square.getIndexLength(), GL4ES3.GL_UNSIGNED_SHORT, 0);
			}

		}
		gl4es3.glUseProgram(programTextureHBWS);
		{
			gl4es3.glBindFramebuffer(GL4ES3.GL_FRAMEBUFFER, dynfboIDs[2]);
			gl4es3.glBindVertexArray(square.getVAO());
			{
				gl4es3.glUniform4fv(uniformIDs[offSetsIDHB], 1, new float [] {0,1,0,1}, 0);
				gl4es3.glUniform1f(uniformIDs[HBwithS], 1);
				gl4es3.glUniform1f(uniformIDs[HueoffIDHB], (float) (color/60d));
				gl4es3.glDrawElements(GL4ES3.GL_TRIANGLES, square.getIndexLength(), GL4ES3.GL_UNSIGNED_SHORT, 0);
			}
			
			gl4es3.glBindFramebuffer(GL4ES3.GL_FRAMEBUFFER, dynfboIDs[3]);
			gl4es3.glBindVertexArray(square.getVAO());
			{
				gl4es3.glUniform4fv(uniformIDs[offSetsIDHB], 1, new float [] {0,1,0,1}, 0);
				gl4es3.glUniform1f(uniformIDs[HBwithS], 0);
				gl4es3.glUniform1f(uniformIDs[HueoffIDHB], (float) (color/60d));
				gl4es3.glDrawElements(GL4ES3.GL_TRIANGLES, square.getIndexLength(), GL4ES3.GL_UNSIGNED_SHORT, 0);
			}

		}
		gl4es3.glUseProgram(programTextureSHWB);
		{
			gl4es3.glBindFramebuffer(GL4ES3.GL_FRAMEBUFFER, dynfboIDs[4]);
			gl4es3.glBindVertexArray(square.getVAO());
			{
				gl4es3.glUniform4fv(uniformIDs[offSetsIDSH], 1, new float [] {0,1,0,1}, 0);
				gl4es3.glUniform1f(uniformIDs[SHwithB], 1);
				gl4es3.glUniform1f(uniformIDs[HueoffIDSH], (float) (color/60d));
				gl4es3.glDrawElements(GL4ES3.GL_TRIANGLES, square.getIndexLength(), GL4ES3.GL_UNSIGNED_SHORT, 0);
			}
			
			gl4es3.glBindFramebuffer(GL4ES3.GL_FRAMEBUFFER, dynfboIDs[5]);
			gl4es3.glBindVertexArray(square.getVAO());
			{
				gl4es3.glUniform4fv(uniformIDs[offSetsIDSH], 1, new float [] {0,1,0,1}, 0);
				gl4es3.glUniform1f(uniformIDs[SHwithB], 0);
				gl4es3.glUniform1f(uniformIDs[HueoffIDSH], (float) (color/60d));
				gl4es3.glDrawElements(GL4ES3.GL_TRIANGLES, square.getIndexLength(), GL4ES3.GL_UNSIGNED_SHORT, 0);
			}
			
		}
		gl4es3.glBindFramebuffer(GL4ES3.GL_FRAMEBUFFER, 0);
		 
		gl4es3.glUseProgram(programScreen);
		gl4es3.glViewport(sx, sy, swidth, sheight);
		
		{
			for (int i = 0; i < coms.length; i++) {
				if (coms[i].draw) {
					gl4es3.glBindVertexArray(coms[i].model.getVAO());
					{
						gl4es3.glBindTexture(GL4ES3.GL_TEXTURE_2D_ARRAY, coms[i].textureID);
						gl4es3.glUniform1i(textureShaderID, 0);

						finalS = FloatUtil.multMatrix(coms[i].finalRotation, coms[i].scaleMatrix, Ftranslations);
						Ftranslations = FloatUtil.multMatrix(coms[i].positionMatrix, finalS, Ftranslations);
						finalM = FloatUtil.multMatrix(cam.finalRotation, cam.translation, finalM);
						modelToClip = FloatUtil.multMatrix(finalM, Ftranslations, modelToClip);
						gl4es3.glUniformMatrix4fv(modelMatrixID, 1, false, modelToClip, 0);
						gl4es3.glUniformMatrix4fv(projMatrixID, 1, false, projMatrix, 0);
						gl4es3.glUniformMatrix2fv(rotationUVID, 1, false, coms[i].texUV, 0);
						gl4es3.glUniform4fv(offSetsID, 1, coms[i].texUVP, 0);
						gl4es3.glUniform1i(layerID, coms[i].layer);

						gl4es3.glDrawElements(GL4ES3.GL_TRIANGLES, coms[i].model.getIndexLength(), GL4ES3.GL_UNSIGNED_SHORT, 0);
					}
				}
			}
			gl4es3.glBindVertexArray(0);
		}
		gl4es3.glUseProgram(0);
		checkError(gl4es3, "display");
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
        GL4ES3 gl4es3 = drawable.getGL().getGL4();
        this.sx = x;
        this.sy = y;
        this.swidth = width;
        this.sheight = height;
        this.cwidth = width/2;
        this.cheight = height/2;
        projMatrix = BuildPerspProjMat(50, (double)width/(double)height, 0.1d, 400d);
        gl4es3.glViewport(x, y, width, height);
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

    private void initTextures(GL4ES3 gl4es3, String folderName) {
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
		textureIDs = new int[imageData.length];
		if (imageData.length > 0) {
			gl4es3.glGenTextures(imageData.length, textureIDs, 0);
			ModelManager.setImageIDs(textureIDs);
			for (int i = 0; i < imageData.length; i++) {
				addTexture(imageData[i], textureIDs[i], dims[i * 2 + 1], dims[i * 2], gl4es3);
			}
			gl4es3.glBindTexture(GL4ES3.GL_TEXTURE_2D, 0);
			ModelManager.checkError();
			ModelManager.orderTextureIDs();
		}
	}
	
	public void addTexture (byte [] data, int id, int width, int height, GL4ES3 gl4es3){
		gl4es3.glBindTexture(GL4ES3.GL_TEXTURE_2D, id);
		gl4es3.glTexParameterf(GL.GL_TEXTURE_2D, GL4ES3.GL_TEXTURE_MIN_FILTER, GL4ES3.GL_NEAREST);
		gl4es3.glTexParameterf(GL.GL_TEXTURE_2D, GL4ES3.GL_TEXTURE_MAG_FILTER, GL4ES3.GL_NEAREST);
		gl4es3.glTexParameterf(GL.GL_TEXTURE_2D, GL4ES3.GL_TEXTURE_WRAP_S, GL4ES3.GL_REPEAT);
		gl4es3.glTexParameterf(GL.GL_TEXTURE_2D, GL4ES3.GL_TEXTURE_WRAP_T, GL4ES3.GL_REPEAT);
		ByteBuffer buffer = GLBuffers.newDirectByteBuffer(data);
		gl4es3.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, width, height, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, buffer);
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
    
	public int [] dynamicTextureInit (GL4ES3 gl4es3){
		int [] IDs = new int [7];
		gl4es3.glGenTextures(1, IDs, 0);
		gl4es3.glBindTexture(GL4ES3.GL_TEXTURE_2D_ARRAY, IDs[0]);
		gl4es3.glTexParameterf(GL4ES3.GL_TEXTURE_2D_ARRAY, GL4ES3.GL_TEXTURE_MIN_FILTER, GL4ES3.GL_NEAREST);
		gl4es3.glTexParameterf(GL4ES3.GL_TEXTURE_2D_ARRAY, GL4ES3.GL_TEXTURE_MAG_FILTER, GL4ES3.GL_NEAREST);
		gl4es3.glTexImage3D(GL4ES3.GL_TEXTURE_2D_ARRAY, 0, GL4ES3.GL_RGBA, DYNTEXTURESIZE, DYNTEXTURESIZE, 6, 0, GL4ES3.GL_RGBA, GL4ES3.GL_UNSIGNED_BYTE, null);

		gl4es3.glGenFramebuffers(6, IDs, 1);
		for (int i=0;i<6;i++){			
			gl4es3.glBindFramebuffer(GL4ES3.GL_FRAMEBUFFER, IDs[i+1]);
			gl4es3.glFramebufferTextureLayer(GL4ES3.GL_FRAMEBUFFER, GL4ES3.GL_COLOR_ATTACHMENT0, IDs[0], 0, i);
		}
		gl4es3.glBindFramebuffer(GL4ES3.GL_FRAMEBUFFER, 0);
		gl4es3.glBindTexture(GL4ES3.GL_TEXTURE_2D_ARRAY, 0);
		
		if (gl4es3.glCheckFramebufferStatus(GL4ES3.GL_FRAMEBUFFER) != GL4ES3.GL_FRAMEBUFFER_COMPLETE) {
			System.out.println("FBO ERROR");
		}
		
		return IDs; 
	}
	
	public void addListeners (){
		glWindow.addKeyListener(this);
		glWindow.addMouseListener(this);
	}
	
	public void initComponents(){
		coms = new Component [24];
		comPs = new Component_Piece [24];
		coms[0] = Component_Piece.get_Component_Piece_Z(0,0,2f,0,0,0,0.5f,0.5f,1,true,ModelManager.ONEXONEF, dyntextureID,0,"Test", new float[] {1,0,0,1});	
		coms[1] = Component_Piece.get_Component_Piece_Z(0.5f,0,2f,0,0,Math.PI/4,0.5f,0.5f,1,true,ModelManager.ONEXONEF, dyntextureID,0,"Test", new float[] {0,1,-1,0});	
		coms[2] = Component_Piece.get_Component_Piece_Z(0.5f,0.5f,2f,0,0,Math.PI/2,0.5f,0.5f,1,true,ModelManager.ONEXONEF, dyntextureID,0,"Test", new float[] {-1,0,0,-1});	
		coms[3] = Component_Piece.get_Component_Piece_Z(0,0.5f,2f,0,0,3*Math.PI/4,0.5f,0.5f,1,true,ModelManager.ONEXONEF, dyntextureID,0,"Test", new float[] {0,-1,1,0});

		coms[4] = Component_Piece.get_Component_Piece_Z(0,0,3f,0,0,0,0.5f,0.5f,1,true,ModelManager.ONEXONEF, dyntextureID,1,"Test", new float[] {1,0,0,1});	
		coms[5] = Component_Piece.get_Component_Piece_Z(0.5f,0,3f,0,0,Math.PI/4,0.5f,0.5f,1,true,ModelManager.ONEXONEF, dyntextureID,1,"Test", new float[] {0,1,-1,0});	
		coms[6] = Component_Piece.get_Component_Piece_Z(0.5f,0.5f,3f,0,0,Math.PI/2,0.5f,0.5f,1,true,ModelManager.ONEXONEF, dyntextureID,1,"Test", new float[] {-1,0,0,-1});	
		coms[7] = Component_Piece.get_Component_Piece_Z(0,0.5f,3f,0,0,3*Math.PI/4,0.5f,0.5f,1,true,ModelManager.ONEXONEF, dyntextureID,1,"Test", new float[] {0,-1,1,0});
		
		coms[8] = Component_Piece.get_Component_Piece_Y(0,0.25f,2.25f,0,0,0,0.5f,1,0.5f,true,ModelManager.ONEXONET, dyntextureID,4,"Test", new float[] {1,0,0,1});	
		coms[9] = Component_Piece.get_Component_Piece_Y(0.5f,0.25f,2.25f,0,Math.PI/4,0,0.5f,1,0.5f,true,ModelManager.ONEXONET, dyntextureID,4,"Test", new float[] {0,1,-1,0});	
		coms[10] = Component_Piece.get_Component_Piece_Y(0.5f,0.25f,1.75f,0,Math.PI/2,0,0.5f,1,0.5f,true,ModelManager.ONEXONET, dyntextureID,4,"Test", new float[] {-1,0,0,-1});	
		coms[11] = Component_Piece.get_Component_Piece_Y(0,0.25f,1.75f,0,3*Math.PI/4,0,0.5f,1,0.5f,true,ModelManager.ONEXONET, dyntextureID,4,"Test", new float[] {0,-1,1,0});
		
		coms[12] = Component_Piece.get_Component_Piece_Y(0,-.75f,2.25f,0,0,0,0.5f,1,0.5f,true,ModelManager.ONEXONET, dyntextureID,5,"Test", new float[] {1,0,0,1});	
		coms[13] = Component_Piece.get_Component_Piece_Y(0.5f,-.75f,2.25f,0,Math.PI/4,0,0.5f,1,0.5f,true,ModelManager.ONEXONET, dyntextureID,5,"Test", new float[] {0,1,-1,0});	
		coms[14] = Component_Piece.get_Component_Piece_Y(0.5f,-.75f,1.75f,0,Math.PI/2,0,0.5f,1,0.5f,true,ModelManager.ONEXONET, dyntextureID,5,"Test", new float[] {-1,0,0,-1});	
		coms[15] = Component_Piece.get_Component_Piece_Y(0,-.75f,1.75f,0,3*Math.PI/4,0,0.5f,1,0.5f,true,ModelManager.ONEXONET, dyntextureID,5,"Test", new float[] {0,-1,1,0});
		
		coms[16] = Component_Piece.get_Component_Piece_X(.25f,0.0f,2.25f,0,0,0,1,0.5f,0.5f,true,ModelManager.ONEXONER, dyntextureID,2,"Test", new float[] {1,0,0,1});	
		coms[17] = Component_Piece.get_Component_Piece_X(.25f,0.5f,2.25f,Math.PI/4,0,0,1,0.5f,0.5f,true,ModelManager.ONEXONER, dyntextureID,2,"Test", new float[] {0,-1,1,0});	
		coms[18] = Component_Piece.get_Component_Piece_X(.25f,0.5f,1.75f,Math.PI/2,0,0,1,0.5f,0.5f,true,ModelManager.ONEXONER, dyntextureID,2,"Test", new float[] {-1,0,0,-1});	
		coms[19] = Component_Piece.get_Component_Piece_X(.25f,0.0f,1.75f,3*Math.PI/4,0,0,1,0.5f,0.5f,true,ModelManager.ONEXONER, dyntextureID,2,"Test", new float[] {0,1,-1,0});
		
		coms[20] = Component_Piece.get_Component_Piece_X(-.75f,0.0f,2.25f,0,0,0,1,0.5f,0.5f,true,ModelManager.ONEXONER, dyntextureID,3,"Test", new float[] {1,0,0,1});	
		coms[21] = Component_Piece.get_Component_Piece_X(-.75f,0.5f,2.25f,Math.PI/4,0,0,1,0.5f,0.5f,true,ModelManager.ONEXONER, dyntextureID,3,"Test", new float[] {0,-1,1,0});	
		coms[22] = Component_Piece.get_Component_Piece_X(-.75f,0.5f,1.75f,Math.PI/2,0,0,1,0.5f,0.5f,true,ModelManager.ONEXONER, dyntextureID,3,"Test", new float[] {-1,0,0,-1});	
		coms[23] = Component_Piece.get_Component_Piece_X(-.75f,0.0f,1.75f,3*Math.PI/4,0,0,1,0.5f,0.5f,true,ModelManager.ONEXONER, dyntextureID,3,"Test", new float[] {0,1,-1,0});
		
		comPs[0] = (Component_Piece) coms[0];
		comPs[1] = (Component_Piece) coms[1];
		comPs[2] = (Component_Piece) coms[2];
		comPs[3] = (Component_Piece) coms[3];
		comPs[4] = (Component_Piece) coms[4];
		comPs[5] = (Component_Piece) coms[5];
		comPs[6] = (Component_Piece) coms[6];
		comPs[7] = (Component_Piece) coms[7];
		comPs[8] = (Component_Piece) coms[8];
		comPs[9] = (Component_Piece) coms[9];
		comPs[10] = (Component_Piece) coms[10];
		comPs[11] = (Component_Piece) coms[11];
		comPs[12] = (Component_Piece) coms[12];
		comPs[13] = (Component_Piece) coms[13];
		comPs[14] = (Component_Piece) coms[14];
		comPs[15] = (Component_Piece) coms[15];
		comPs[16] = (Component_Piece) coms[16];
		comPs[17] = (Component_Piece) coms[17];
		comPs[18] = (Component_Piece) coms[18];
		comPs[19] = (Component_Piece) coms[19];
		comPs[20] = (Component_Piece) coms[20];
		comPs[21] = (Component_Piece) coms[21];
		comPs[22] = (Component_Piece) coms[22];
		comPs[23] = (Component_Piece) coms[23];
	}
	
	
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_BACK_QUOTE){
			animator.stop();
			glWindow.destroy();
		} else if (e.getKeyCode() == KeyEvent.VK_A){
			cam.keyState(0, true);
		} else if (e.getKeyCode() == KeyEvent.VK_D){
			cam.keyState(1, true);
		} else if (e.getKeyCode() == KeyEvent.VK_S){
			cam.keyState(3, true);
		} else if (e.getKeyCode() == KeyEvent.VK_W){
			cam.keyState(2, true);
		} else if (e.getKeyCode() == KeyEvent.VK_1){
			cam.rotateX(Math.PI/12);
		} else if (e.getKeyCode() == KeyEvent.VK_2){
			cam.rotateX(-Math.PI/12);
		} else if (e.getKeyCode() == KeyEvent.VK_3){
			cam.rotateY(+Math.PI/12);
		} else if (e.getKeyCode() == KeyEvent.VK_4){
			cam.rotateY(-Math.PI/12);
		} else if (e.getKeyCode() == KeyEvent.VK_5){
			cam.rotateZ(Math.PI/12);
		} else if (e.getKeyCode() == KeyEvent.VK_6){
			cam.rotateZ(-Math.PI/12);
		} else if (e.getKeyCode() == KeyEvent.VK_UP){
			angleSpeed = angleSpeed + 1d/50000000d;
		} else if (e.getKeyCode() == KeyEvent.VK_DOWN){
			angleSpeed = angleSpeed - 1d/50000000d;
		} else if (e.getKeyCode() == KeyEvent.VK_G){
			if (comPs[0].change){				
				for (int i=0;i<comPs.length;i++){
					comPs[i].change = false;
				}
			} else {
				for (int i=0;i<comPs.length;i++){
					comPs[i].change = true;
				}				
			}
		}
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		if (e.isAutoRepeat() == false) {
			if (e.getKeyCode() == KeyEvent.VK_A) {
				cam.keyState(0, false);
			} else if (e.getKeyCode() == KeyEvent.VK_D) {
				cam.keyState(1, false);
			} else if (e.getKeyCode() == KeyEvent.VK_S) {
				cam.keyState(3, false);
			} else if (e.getKeyCode() == KeyEvent.VK_W) {
				cam.keyState(2, false);
			}
		}
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

	double mouseX, mouseY;
	
	@Override
	public void mouseMoved(MouseEvent arg0) {
		mouseX = ((double) (arg0.getX())/(double) (swidth))*2 - 1f;
		mouseY = ((double) (arg0.getY())/(double) (sheight))*2 - 1f;
		cam.mouseMoved(mouseX, mouseY);
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		
	}

	@Override
	public void mouseWheelMoved(MouseEvent arg0) {
		
	}
	
}
