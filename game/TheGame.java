package game;

import game.characters.*;
import game.camera.*;
import game.ai.*;
import game.projectiles.*;

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
import sage.model.loader.ogreXML.OgreXMLParser;

import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;
import graphicslib3D.Matrix3D;

import sage.audio.*;
import com.jogamp.openal.ALFactory;

import java.io.File;
import java.util.Iterator;
import java.util.Random;
import java.awt.Color;

import net.java.games.input.Component.*;

public class TheGame extends BaseGame
{

	IDisplaySystem display;
	ICamera camera;
	Camera3Pcontroller camController;
	IInputManager im;
	String kbName;
	Group rootNode, manModel, golemModel;
	SkyBox skybox;
	Avatar player1;
	Monster golem;

	GolemController golemController;
	ProjectileController projectileController;
	
	IAudioManager audioMgr;
	Sound testSound, testSound2;

	int endOfWorld = 20;

	int iteration;
	TriMesh gateCenter;
	Point3D gateCenterLoc;

	private float HEIGHT = 0.0f;
	private float SPEED = 0.01f;
	private float GRAVITY = 0.5f;
	private int NUM_ENEMIES = 5;

	private Group projectiles;
	
	protected void initGame()
	{
		im = getInputManager();
		kbName = im.getKeyboardName();
		initDisplay();
		initSkyBox();
		initTerrain();
		initGameObjects();
		initPlayers();
		initMovementControls();
		initAudio();
		initHUD();
	}

	private void initDisplay()
	{
		display = getDisplaySystem();
		display.setTitle("The Game");
	}
	
	private void initHUD()
	{
		
		// HUD stuff
		
		// Set life bars
		life1 = new HUDImage("./life.png");
		life1.setLocation(-0.9,0.8);
		life1.scale(0.1f,0.1f,0.1f);
		addGameWorldObject(life1);
		
		life2 = new HUDImage("./life.png");
		life2.setLocation(-0.8,0.8);
		life2.scale(0.1f,0.1f,0.1f);
		addGameWorldObject(life2);
		
		life3 = new HUDImage("./life.png");
		life3.setLocation(-0.7,0.8);
		life3.scale(0.1f,0.1f,0.1f);
		addGameWorldObject(life3);
		
		
		
		// Set 'HEALTH' Lable
		health.setLocation(0.03, 0.94);
		health.scale(1.5f,1.5f,1.5f);
		addGameWorldObject(health);
		
		// Set 'SCORE' Lable
		score.setLocation(0.9, 0.94);
		score.scale(1.5f,1.5f,1.5f);
		addGameWorldObject(score);
		
		
		
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

	private void initGameObjects()
	{
		golem = new Monster(new Point3D(-20,2,0), new Vector3D(1,0,0), 90);
		//Random rand = new Random();
		// Get a random spawn location
		//int spawn_loc = rand.nextInt(Math.abs(endOfWorld)) + 20;
		//System.out.println("Spawn location = " + -spawn_loc);
		applyTexture(golem, "golem.png");
		addGameWorldObject(golem);

		golemController = new GolemController(this, golem);

		// Spawn enemies
		/*
		golems = new Monster[NUM_ENEMIES];
		Random rand = new Random();
		for(int i=0; i<NUM_ENEMIES; i++)
		{
			int spawn_loc = rand.nextInt(Math.abs(endOfWorld)) + 20;	// Get a random spawn location
			golems[i] = new Monster(new Point3D(-spawn_loc,2,0), new Vector3D(1,0,0), 90);
			System.out.println("Spawn location = " + -spawn_loc);
			applyTexture(golems[i], "golem.png");
			addGameWorldObject(golems[i]);

			golemController = new GolemController(this, golems[i]);
		}
		*/


		// Add Group which we will use to store projectiles
		projectiles = new Group();
		addGameWorldObject(projectiles);
		projectileController = new ProjectileController();

		OBJLoader loader = new OBJLoader();
	/*
		Avatar man = new Human(); 
		applyTexture(man, "./textures/man.png");
		man.rotate(0,new Vector3D(0,1,0));
		man.scale(4,4,4);
		man.translate(-2,6,6);
		addGameWorldObject(man);
		
		Avatar golem = new Monster(); 
		applyTexture(golem, "./textures/golem.png");
		golem.rotate(75,new Vector3D(0,1,0));
		golem.scale(1,.75f,1);
		golem.translate(-10,2.5f,6);
		addGameWorldObject(golem);
	*/		

		// Gate center
		gateCenter = loader.loadModel("./models/gate_wood.obj");
		Texture woodTex = TextureManager.loadTexture2D("./textures/light_wood.png");
		TextureState woodTexState = (TextureState)display.getRenderer().createRenderState(RenderState.RenderStateType.Texture);
		woodTexState.setTexture(woodTex);
		woodTexState.setEnabled(true);
		gateCenter.setRenderState(woodTexState);
		gateCenter.rotate(90,new Vector3D(0,1,0));
		gateCenter.scale(1,.75f,1);
		gateCenter.translate(10,10,0);
		addGameWorldObject(gateCenter);
		gateCenterLoc = new Point3D(10,10,0);
		iteration = 0;


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

	private void initPlayers()
	{
		OBJLoader loader = new OBJLoader();

		player1 = new Human(new Point3D(0,2,0), new Vector3D(0,1,0), 90);
		applyTexture(player1, "./textures/man.png");
		//player1 = new Human();
		
		//player1.rotate(90, new Vector3D(0,1,0));
		player1.scale(2,2,2);
		//Matrix3D player1Mat = player1.getLocalTranslation();
		//player1Mat.translate(0,2,0);
		//player1.setLocalTranslation(player1Mat);
		addGameWorldObject(player1);

/*
		golemModel = getMonsterAvatar();
		golemModel.translate(0,4,0);

		Iterator<SceneNode> itr = golemModel.getChildren();
		while(itr.hasNext())
		{
			Model3DTriMesh mesh = ((Model3DTriMesh)itr.next());
			mesh.startAnimation("Walk");
		}
*/
/*
		Group model = ((Human)player1).getModel();

		Iterator<SceneNode> itr = model.getChildren();
		while (itr.hasNext())
		{ 
			Model3DTriMesh mesh = ((Model3DTriMesh)itr.next());
			//mesh.startAnimation("Legs_walk");
		}
*/
		//player1.startAnimation("Legs_walk");
		//player1.startAnimation("Arms_walk");


		// Create camera controller
		camera = display.getRenderer().getCamera();
		camera.setPerspectiveFrustum(45, 1, 0.01, 1000);
		camera.setLocation(new Point3D(0,0,0));

		camController = new Camera3Pcontroller(camera, player1, im, kbName);
	}

	private void initMovementControls()
	{
		// Keyboard actions
		MoveAction mvLeft = new MoveAction(player1, "LEFT");
		MoveAction mvRight = new MoveAction(player1, "RIGHT");
		MoveAction jump = new MoveAction(player1, "JUMP");
		AttackAction fire = new AttackAction((Human)player1, this);

		// Keyboard key bindings
		im.associateAction(kbName, Identifier.Key.A, mvLeft, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kbName, Identifier.Key.D, mvRight, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kbName, Identifier.Key.SPACE, fire, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateAction(kbName, Identifier.Key.LALT, jump, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
	}

	protected void update(float elapsedTimeMS)
	{
		Point3D camLoc = camera.getLocation();
		Matrix3D camTranslation = new Matrix3D();
		camTranslation.translate(camLoc.getX(), camLoc.getY(), camLoc.getZ());
		skybox.setLocalTranslation(camTranslation);
		camController.update(elapsedTimeMS);

		golemController.update(elapsedTimeMS);

		golem.updateAnimation(elapsedTimeMS);
		golem.update(elapsedTimeMS);
		testSound.setLocation(new Point3D(golem.getWorldTranslation().getCol(3)));
		
		setEarParameters();
		
		checkHitDetection();
		
		redrawHealth();

		// Close the gate over a period of about 5 seconds
		if(gateCenterLoc.getY() > 0)
		{
			Vector3D loc = new Vector3D(gateCenterLoc);
			Vector3D dir = new Vector3D(0,1,0);
			dir.scale(-0.0067);
			loc = loc.add(dir);
			gateCenterLoc = new Point3D(loc);
			Matrix3D mat = new Matrix3D();
			mat.translate(gateCenterLoc.getX(), gateCenterLoc.getY(), gateCenterLoc.getZ());
			gateCenter.setLocalTranslation(mat);
		}


/*
		for(int i=0; i<NUM_ENEMIES; i++)
		{
			golems[i].updateAnimation(elapsedTimeMS);
			golems[i].update(elapsedTimeMS);
		}
*/

/*
		Iterator<SceneNode> itr = golemModel.getChildren();
		while(itr.hasNext())
		{
			Model3DTriMesh submesh = (Model3DTriMesh) itr.next();
			submesh.updateAnimation(elapsedTimeMS);
		}

		golemModel.updateGeometricState(elapsedTimeMS, true);
*/

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

	private void applyTexture(Avatar c, String file)
	{
		Texture texture = TextureManager.loadTexture2D(file);
		TextureState textureState = (TextureState) display.getRenderer().createRenderState(RenderState.RenderStateType.Texture);
		textureState.setTexture(texture);
		textureState.setEnabled(true);
		c.setRenderState(textureState);
		c.updateRenderStates();
	}

	private Group getPlayerAvatar()
	{
		Group model = null;
		OgreXMLParser loader = new OgreXMLParser();

		try
		{
			String slash = File.separator;
			model = loader.loadModel("models" + slash + "Cube.001.mesh.xml",
			"models" + slash + "man_skin.material",
			"models" + slash + "Cube.001.skeleton.xml");
			model.updateGeometricState(0, true);
		}
		catch (Exception e)
		{ 
			e.printStackTrace();
			System.exit(1);
		}
		
		return model;
	}

	private Group getMonsterAvatar()
	{
		Group model = null;
		OgreXMLParser loader = new OgreXMLParser();
		loader.setVerbose(true);

		try
		{
			String slash = File.separator;
			model = loader.loadModel("models" + slash + "golem.mesh.xml",
			"materials" + slash + "golem_mat.material",
			"models" + slash + "golem.skeleton.xml");
			model.updateGeometricState(0, true);
		}
		catch (Exception e)
		{ 
			e.printStackTrace();
			System.exit(1);
		}
		
		return model;
	}

	public boolean checkNearbyMonsters(Monster golem)
	{
		if(Math.abs(player1.getLocation().getX() - golem.getLocation().getX()) <= 10)
			return true;
		else
			return false;
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
	
	public void initAudio(){
		
		AudioResource resource1, resource2;
		
		audioMgr = AudioManagerFactory.createAudioManager("sage.audio.joal.JOALAudioManager");
		
		if(!audioMgr.initialize())
		{ System.out.println("Audio Manager failed to initialize!");
			return;
		}
		
		resource1 = audioMgr.createAudioResource("roar.wav", AudioResourceType.AUDIO_SAMPLE);
		resource2 = audioMgr.createAudioResource("order.wav", AudioResourceType.AUDIO_SAMPLE);
		
		testSound =  new Sound(resource1, SoundType.SOUND_EFFECT, 3, true);
		testSound.initialize(audioMgr);
		testSound.setMaxDistance(50.0f);
		testSound.setMinDistance(3.0f);
		testSound.setRollOff(5.0f);
		testSound.setLocation(new Point3D(golem.getWorldTranslation().getCol(3)));
		
		testSound2 =  new Sound(resource2, SoundType.SOUND_MUSIC, 1, true);
		testSound2.initialize(audioMgr);
		testSound2.setMaxDistance(50.0f);
		testSound2.setMinDistance(3.0f);
		testSound2.setRollOff(5.0f);
		
		
		setEarParameters();
		
		
		testSound.play();
		testSound2.play();
		
	}
	
	public void setEarParameters(){
		
		Matrix3D avDir = (Matrix3D) (player1.getWorldRotation().clone());
		//float camAz = camController.getAzimuth();
		avDir.rotateY(180.0f);
		Vector3D camDir = new Vector3D(0,0,1);
		camDir = camDir.mult(avDir);
		
		audioMgr.getEar().setLocation(camera.getLocation());
		audioMgr.getEar().setOrientation(camDir, new Vector3D(0,1,0));
		
	}
	

	public Avatar getPlayer()
	{
		return player1;
	}

	public void createProjectile(Projectile p)
	{
		projectiles.addChild(p);
		p.addController(projectileController);
		projectileController.addControlledNode(p);
	}
	
	public void redrawHealth()
	{
		int h = ((Human)player1).getHealth();
		
		if (h==3)
		{
			life1.setLocation(-0.9,0.8);
			life2.setLocation(-0.8,0.8);
			life3.setLocation(-0.7,0.8);
		}else if (h==2){
			life1.setLocation(-0.9,0.8);
			life2.setLocation(-0.8,0.8);
			life3.setLocation(-0.7,1.5);
		}else if (h==1){
			life1.setLocation(-0.9,0.8);
			life2.setLocation(-0.8,1.5);
			life3.setLocation(-0.7,1.5);
		}else{
			life1.setLocation(-0.9,1.5);
			life2.setLocation(-0.8,1.5);
			life3.setLocation(-0.7,1.5);
		}
		
	}

	public void checkHitDetection()
	{
		/*ArrayList<SceneNode> deleteList = new ArrayList<SceneNode>();
		Iterator<SceneNode> itr = projectiles.getChildren();

		while(itr.hasNext())
		{
			SceneNode p = itr.next();
			if(p instanceof Projectile)
			{
				p.updateWorldBound();

			}
		}*/
		
		Iterator<SceneNode>	itemList = projectiles.getChildren();
		Iterator<SceneNode>	monList = monsters.getChildren();
		
		while(itemList.hasNext())
		{
			SceneNode item = itemList.next();
			if(item instanceof Projectile)
			{
			  Point3D p1Point = new Point3D(golem.getWorldTranslation().getCol(3));
			  //System.out.println(p1Point.getX() + " " + p1Point.getY() + " " + p1Point.getZ());
			  
			  item.updateWorldBound();
			  
			  // Check to see if projectile collided with a golem
			  if(item.getWorldBound().intersects(golem.getWorldBound()))
			  {
				  System.out.println("hit!");
				  itemList.remove();
				  
				  ((Monster)golem).setHealth(((Monster)golem).getHealth()-1);
				  System.out.println("hit him!");
				  
			  }
			}
		}// end of while 
		
		player1.updateWorldBound();
		golem.updateWorldBound();
		if(player1.getWorldBound().intersects(golem.getWorldBound()))
		{
			System.out.println("hit you!");
			((Human)player1).setHealth(((Human)player1).getHealth()-1);
		}
		
	}
	
}