package game;

import game.characters.*;
import game.camera.*;

import sage.app.BaseGame;
import sage.display.*;
import sage.camera.*;
import sage.input.*;
import sage.scene.*;
import sage.scene.shape.*;
import sage.scene.state.*;
import sage.terrain.*;
import sage.texture.*;
import sage.model.loader.OBJLoader;

import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;
import graphicslib3D.Matrix3D;

import java.awt.Color;

import net.java.games.input.Component.*;

public class TheGame extends BaseGame
{

	IDisplaySystem display;
	ICamera camera;
	Camera3Pcontroller camController;
	IInputManager im;
	String kbName;
	Group rootNode;
	SkyBox skybox;
	Human player1;

	Matrix3D rotation;
	Vector3D direction = new Vector3D(0,1,0);

	int endOfWorld = 20;

	private float HEIGHT = 0.0f;
	private float SPEED = 0.01f;
	private float GRAVITY = 0.5f;
	
	protected void initGame()
	{
		im = getInputManager();
		kbName = im.getKeyboardName();
		initDisplay();
		initSkyBox();
		initTerrain();
		//initTerrainHeightMap();
		initGameObjects();
		initMovementControls();
	}

	private void initDisplay()
	{
		display = getDisplaySystem();
		display.setTitle("The Game");
	}

	private void initSkyBox()
	{
		skybox = new SkyBox("SkyBox", 24.0f, 15.0f, 50.0f);
		Texture background = TextureManager.loadTexture2D("./textures/skybox/nightsky_ft.jpg");
		skybox.setTexture(SkyBox.Face.North, background);
		addGameWorldObject(skybox);
	}

	private void initTerrain()
	{
		for(int i=0; i<5; i++)
		{
			/*
			if(i == 2)
			{
				Rectangle hazard = createHazardPanel();
				Matrix3D hazardMat = hazard.getLocalTranslation();
				hazardMat.translate(endOfWorld,0,0);
				hazard.setLocalTranslation(hazardMat);
				addGameWorldObject(hazard);
				endOfWorld -= 10;
			}*/
			
			Rectangle ground = createGroundPanel();
			Matrix3D groundMat = ground.getLocalTranslation();
			groundMat.translate(endOfWorld-20,0,0);
			ground.setLocalTranslation(groundMat);
			addGameWorldObject(ground);
			endOfWorld -= 40;

			Rectangle hazard = createHazardPanel();
			Matrix3D hazardMat = hazard.getLocalTranslation();
			hazardMat.translate(endOfWorld,0,0);
			hazard.setLocalTranslation(hazardMat);
			addGameWorldObject(hazard);
			endOfWorld -= 5;
		}
		System.out.println("End of world = " + endOfWorld);
	}

	private void initTerrainHeightMap()
	{
		skybox = new SkyBox("SkyBox", 20.0f, 20.0f, 20.0f);
		Texture background = TextureManager.loadTexture2D("./textures/background.png");
		skybox.setTexture(SkyBox.Face.North, background);
		//rootNode.addChild(skybox);
		addGameWorldObject(skybox);

		TerrainBlock[] terrain = new TerrainBlock[3];
		ImageBasedHeightMap myHeightMap = new ImageBasedHeightMap("./textures/height.jpg");
		for(int i=0; i<3; i++)
		{
			terrain[i] = createTerBlock(myHeightMap, i+1);
		}

		// create texture state to color terrain 
		TextureState groundState;
		Texture groundTexture = TextureManager.loadTexture2D("./textures/sand.bmp");
		groundTexture.setApplyMode(Texture.ApplyMode.Replace);
		groundState = (TextureState) display.getRenderer().createRenderState(RenderState.RenderStateType.Texture); // TODO - condense this?
		groundState.setTexture(groundTexture, 0);
		groundState.setEnabled(true);

		// apply texture to the terrain
		for(int i=0; i<3; i++)
		{
			terrain[i].setRenderState(groundState);
			addGameWorldObject(terrain[i]);
		}
	}

	private void initGameObjects()
	{
		camera = display.getRenderer().getCamera();
		camera.setPerspectiveFrustum(45, 1, 0.01, 1000);
		camera.setLocation(new Point3D(0,0,0));

		// Players will be cylinders
		player1 = new Human();
		player1.setSlices(50);
		player1.rotate(90, new Vector3D(1,0,0));
		player1.scale(1,3,1);
		Matrix3D player1Mat = player1.getLocalTranslation();
		player1Mat.translate(0,2,0);
		player1.setLocalTranslation(player1Mat);
		addGameWorldObject(player1);

		// Create camera controller
		camController = new Camera3Pcontroller(camera, player1, im, kbName);

		// Ground will be a rectangle
		/*
		Ground ground = new Ground();
		Matrix3D groundMat = ground.getLocalTranslation();
		groundMat.translate(0,-10,-8);
		ground.setLocalTranslation(groundMat);
		ground.scale(100,10,1);
		addGameWorldObject(ground);
		*/

		// Gate center
		OBJLoader loader = new OBJLoader();
		TriMesh gateCenter = loader.loadModel("./models/gate_wood.obj");
		Texture woodTex = TextureManager.loadTexture2D("./textures/light_wood.png");
		TextureState woodTexState = (TextureState)display.getRenderer().createRenderState(RenderState.RenderStateType.Texture);
		woodTexState.setTexture(woodTex);
		woodTexState.setEnabled(true);
		gateCenter.setRenderState(woodTexState);
		gateCenter.rotate(90,new Vector3D(0,1,0));
		gateCenter.scale(1,.75f,1);
		gateCenter.translate(10,0,0);
		addGameWorldObject(gateCenter);
		
		
				TriMesh golem = loader.loadModel("./models/golem.obj");
		Texture golemTex = TextureManager.loadTexture2D("./textures/golem.png");
		TextureState golemTexState = (TextureState) display.getRenderer().createRenderState(RenderState.RenderStateType.Texture);
		golemTexState.setTexture(golemTex);
		golemTexState.setEnabled(true);
		golem.setRenderState(golemTexState);
		golem.rotate(75,new Vector3D(0,1,0));
		golem.scale(1,.75f,1);
		golem.translate(-10,2.5f,6);
		addGameWorldObject(golem);
		
		
		TriMesh man = loader.loadModel("./models/man.obj");
		Texture manTex = TextureManager.loadTexture2D("./textures/man.png");
		TextureState manTexState = (TextureState) display.getRenderer().createRenderState(RenderState.RenderStateType.Texture);
		manTexState.setTexture(manTex);
		manTexState.setEnabled(true);
		man.setRenderState(manTexState);
		man.rotate(0,new Vector3D(0,1,0));
		man.scale(4,4,4);
		man.translate(-2,6,6);
		addGameWorldObject(man);
		

		// Gate pillars
		TriMesh gateFrame = loader.loadModel("./models/gate_stone.obj");
		Texture stoneTex = TextureManager.loadTexture2D("./textures/stone.jpg");
		TextureState stoneTexState = (TextureState) display.getRenderer().createRenderState(RenderState.RenderStateType.Texture);
		stoneTexState.setTexture(stoneTex);
		stoneTexState.setEnabled(true);
		gateFrame.setRenderState(stoneTexState);
		gateFrame.rotate(90,new Vector3D(0,1,0));
		gateFrame.scale(1,.75f,1);
		gateFrame.translate(10,0,0);
		addGameWorldObject(gateFrame);

		// Background wall
		TriMesh wall = loader.loadModel("./models/wall_stone.obj");
		wall.setRenderState(stoneTexState);
		wall.rotate(90,new Vector3D(0,1,0));
		wall.translate(-5,0,40);
		addGameWorldObject(wall);


		Point3D origin = new Point3D(0,0,0);
		Point3D xEnd = new Point3D(100,0,0);
		Point3D yEnd = new Point3D(0,100,0);
		Point3D zEnd = new Point3D(0,0,100);

		Line xAxis = new Line(origin, xEnd, Color.red, 2);
		Line yAxis = new Line(origin, yEnd, Color.green, 2);
		Line zAxis = new Line(origin, zEnd, Color.blue, 2);

		addGameWorldObject(xAxis);
		addGameWorldObject(yAxis);
		addGameWorldObject(zAxis);
	}

	private void initMovementControls()
	{
		// Keyboard actions
		//MoveAction mvForward = new MoveAction(player1, "FORWARD");
		//MoveAction mvBackward = new MoveAction(player1, "BACKWARD");
		MoveAction mvLeft = new MoveAction(player1, "LEFT");
		MoveAction mvRight = new MoveAction(player1, "RIGHT");
		MoveAction jump = new MoveAction(player1, "JUMP");
		//MoveAction yawRight = new MoveAction(player1, "YAW_RIGHT");
		//MoveAction yawLeft = new MoveAction(player1, "YAW_LEFT");
		//MoveAction pitchUp = new MoveAction(player1, "PITCH_UP");
		//MoveAction pitchDown = new MoveAction(player1, "PITCH_DOWN");
		//MoveAction rollRight = new MoveAction(player1, "ROLL_RIGHT");
		//MoveAction rollLeft = new MoveAction(player1, "ROLL_LEFT");

		// Keyboard key bindings
		//im.associateAction(kbName, Identifier.Key.W, mvForward, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		//im.associateAction(kbName, Identifier.Key.S, mvBackward, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kbName, Identifier.Key.A, mvLeft, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kbName, Identifier.Key.D, mvRight, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kbName, Identifier.Key.LALT, jump, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		//im.associateAction(kbName, Identifier.Key.RIGHT, yawRight, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		//im.associateAction(kbName, Identifier.Key.LEFT, yawLeft, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		//im.associateAction(kbName, Identifier.Key.UP, pitchUp, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		//im.associateAction(kbName, Identifier.Key.DOWN, pitchDown, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		//im.associateAction(kbName, Identifier.Key.E, rollRight, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		//im.associateAction(kbName, Identifier.Key.Q, rollLeft, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		//im.associateAction(kbName, Identifier.Key.ESCAPE, quit, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

	}

	protected void update(float elapsedTimeMS)
	{
		Point3D camLoc = camera.getLocation();
		//System.out.println("camera.X=" + camLoc.getX() + " camera.Y=" + camLoc.getY() + " camera.Z=" + camLoc.getZ());1
		Matrix3D camTranslation = new Matrix3D();
		camTranslation.translate(camLoc.getX(), camLoc.getY(), camLoc.getZ());
		skybox.setLocalTranslation(camTranslation);

		player1.update(elapsedTimeMS);

		camController.update(elapsedTimeMS);
		super.update(elapsedTimeMS);
	}

	private Rectangle createGroundPanel()
	{
		Rectangle ground = new Rectangle();
		ground.scale(40,80,1);
		Texture groundTex = TextureManager.loadTexture2D("./textures/seamless_brick_dark.png");
		TextureState groundTexState = (TextureState) display.getRenderer().createRenderState(RenderState.RenderStateType.Texture);
		groundTexState.setTexture(groundTex);
		groundTexState.setEnabled(true);
		ground.setRenderState(groundTexState);
		ground.rotate(90,new Vector3D(1,0,0));

		return ground;
	}

	private Rectangle createHazardPanel()
	{
		Rectangle hazard = new Rectangle();
		hazard.scale(10,80,1);
		Texture hazardTex = TextureManager.loadTexture2D("./textures/lava.png");
		TextureState hazardTexState = (TextureState) display.getRenderer().createRenderState(RenderState.RenderStateType.Texture);
		hazardTexState.setTexture(hazardTex);
		hazardTexState.setEnabled(true);
		hazard.setRenderState(hazardTexState);
		hazard.rotate(90,new Vector3D(1,0,0));

		return hazard;
	}

	private TerrainBlock createTerBlock(AbstractHeightMap heightMap, int blockNum)
	{
		float heightScale = 0.005f;
		Vector3D terrainScale = new Vector3D(.1, heightScale, .1);

		// use the size of the height map as size of terrain
		int terrainSize = heightMap.getSize();

		//specify terrain origin so heightmap (0,0) is at world origin
		float cornerHeight = heightMap.getTrueHeightAtPoint(0,0) * heightScale;
		Point3D terrainOrigin = new Point3D((blockNum*(-80)), -cornerHeight, 0);

		// create terrain block using the height map
		String name = "Terrain:" + heightMap.getClass().getSimpleName();
		TerrainBlock tb = new TerrainBlock(name, terrainSize, terrainScale, heightMap.getHeightData(), terrainOrigin);
		return tb;
	}

	public Human getPlayer()
	{
		return player1;
	}
}