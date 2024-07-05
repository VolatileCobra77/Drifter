package ca.volatilecobra;

import com.jme3.app.SimpleApplication;
import ca.volatilecobra.TerrainGenerator;
import ca.volatilecobra.CustomWaterGenerator;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.*;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Quad;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.ui.Picture;
import com.jme3.util.SkyFactory;
import com.jme3.water.*;
import com.jme3.scene.control.Control;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.water.*;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.style.BaseStyles;
import com.jme3.audio.*;


public class Drifter extends SimpleApplication implements ActionListener {


    //initalize private variables
    private float[] newData;
    private Spatial waterGeom;
    private Container mainMenu;
    private Container pauseMenu;
    private Container settingsMenu;
    private Picture background;
    private boolean statsOverlay = false, fpsOverlay = false;
    private CustomWaterGenerator customWaterGenerator;
    private float movement = 0f;
    private int tick;
    private boolean sphereMass = false;
    private String keyPressed = "";
    //breaks Java for some reason, looking into it -VolatileCobra77
//    private AudioNode underwater_scary = new AudioNode(assetManager, "Sounds/ambience/underwater_scary.wav", AudioData.DataType.Buffer);
//    private AudioNode above_water_scary = new AudioNode(assetManager, "Sounds/ambience/Above_water_scary.wav", AudioData.DataType.Buffer);
//    private AudioNode above_water_background_sound = new AudioNode(assetManager, "Sounds/background/above_water_background_sound.wav", AudioData.DataType.Buffer);
//    private AudioNode calm_underwater = new AudioNode(assetManager, "Sounds/background/calm_underwater.wav", AudioData.DataType.Buffer);
//    private AudioNode intense_underwater_1 = new AudioNode(assetManager, "Sounds/background/intense_underwater-1.wav", AudioData.DataType.Buffer);
//    private AudioNode intense_underwater_2 = new AudioNode(assetManager, "Sounds/background/intense_underwater-2.wav", AudioData.DataType.Buffer);
//    private AudioNode whale = new AudioNode(assetManager, "Sounds/creature_sounds/whales/whale.wav", AudioData.DataType.Buffer);
//    private AudioNode bigCreature = new AudioNode(assetManager, "Sounds/creature_sounds/misc/big_creature_coming_by.wav");

    private boolean playerHasControl = false, forward = false, backward = false, left = false, right = false, up = false, down = false, underwater = false, lastTickUnderwater = underwater;
    //initalize final variables
    final private Vector3f walkDir = new Vector3f();
    final private Vector3f camDir = new Vector3f();
    final private Vector3f camLeft = new Vector3f();
    final private float playerMoveMult = 0.2f;
    //initalize public variables
    public Spatial raft;
    public Spatial sphere;
    public CharacterControl player;
    public RigidBodyControl sphereControl;

    public static void main(String[] args) {
        Drifter app = new Drifter();
        app.setShowSettings(false); //Settings dialog not supported on mac
        app.start();
    }

    private void setUpKeys(){
        inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_EXIT);
        inputManager.addMapping("forward", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("backward", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("up", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("down", new KeyTrigger(KeyInput.KEY_C));
        inputManager.addMapping("down", new KeyTrigger(KeyInput.KEY_LSHIFT));
        inputManager.addMapping("pause", new KeyTrigger(KeyInput.KEY_ESCAPE));
        inputManager.addListener(this, "forward");
        inputManager.addListener(this, "backward");
        inputManager.addListener(this, "left");
        inputManager.addListener(this, "right");
        inputManager.addListener(this, "up");
        inputManager.addListener(this, "down");
        inputManager.addListener(this, "pause");
    }
    @Override
    public void onAction(String name, boolean isPressed, float tpf){
        switch(name){
            case "forward" -> forward = isPressed;
            case "backward" -> backward = isPressed;
            case "left" -> left = isPressed;
            case "right" -> right = isPressed;
            case "up" -> up = isPressed;
            case "down" -> down = isPressed;
            case "pause" -> pauseGame();
        }

    }
    private void pauseGame(){
        playerHasControl = false;
        mainMenu.removeFromParent();
        settingsMenu.removeFromParent();
        guiNode.attachChild(background);
        guiNode.attachChild(pauseMenu);
    }

    public void setUpGuis(){


        background = new Picture("Background");

        background.setHeight(settings.getHeight());
        background.setWidth(settings.getWidth());
        Material bgMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        bgMat.setColor("Color", ColorRGBA.Black);
        background.setMaterial(bgMat);
        mainMenu = new Container();
        mainMenu.setLocalTranslation((settings.getHeight()/2f)-mainMenu.getSize().x, (settings.getWidth()/2f)-mainMenu.getSize().y, 0);
        Label mainLabel = new Label("Main Menu");
        Button startButton = new Button("Start");
        startButton.addClickCommands(source -> {
            playerHasControl = true;
            mainMenu.removeFromParent();
            background.removeFromParent();
        });
        mainMenu.addChild(mainLabel);
        startButton.setLocalTranslation(mainMenu.getLocalTranslation().add(new Vector3f(0,-10,0)));
        mainMenu.addChild(startButton);

        guiNode.attachChild(mainMenu);

        settingsMenu = new Container();
        settingsMenu.setLocalTranslation(settings.getHeight()/2f, settings.getWidth()/2f, 0);
        Button enableDevOverlayButton = new Button("Toggle Dev overlay");
        enableDevOverlayButton.addClickCommands(source->{
            statsOverlay = !statsOverlay;


        });
        enableDevOverlayButton.setLocalTranslation(settingsMenu.getLocalTranslation().add(new Vector3f(0,-10f,0)));
        Button enableFpsOverlayButton = new Button("Toggle Fps overlay");
        enableFpsOverlayButton.addClickCommands(source->{
            fpsOverlay = !fpsOverlay;


        });
        enableFpsOverlayButton.setLocalTranslation(enableDevOverlayButton.getLocalTranslation().add(new Vector3f(0,-10f,0)));
        Button backToPauseFromSettings = new Button("< Back to Pause Menu");
        backToPauseFromSettings.addClickCommands(source->{
            settingsMenu.removeFromParent();
            guiNode.attachChild(pauseMenu);
        });
        settingsMenu.addChild(backToPauseFromSettings);
        settingsMenu.addChild(enableFpsOverlayButton);
        settingsMenu.addChild(enableDevOverlayButton);
        backToPauseFromSettings.setLocalTranslation(enableFpsOverlayButton.getLocalTranslation().add(new Vector3f(0,-10f,0)));
        pauseMenu = new Container();
        pauseMenu.setLocalTranslation(settings.getWidth()/2f, settings.getHeight()/2f, 0);
        Button resumeButton = new Button("Resume");
        resumeButton.addClickCommands(source -> {
            playerHasControl = true;
            pauseMenu.removeFromParent();
            background.removeFromParent();
        });
        Button settingsButton = new Button("Settings");
        settingsButton.addClickCommands(source -> {
            guiNode.attachChild(settingsMenu);
            pauseMenu.removeFromParent();
        });
        Button quitButton = new Button("Quit");
        quitButton.addClickCommands(source->{
            this.stop();
        });
        pauseMenu.addChild(resumeButton);
        pauseMenu.addChild(settingsButton);
        pauseMenu.addChild(quitButton);
        guiNode.attachChild(background);

    }

    public void setUpWater(){
        customWaterGenerator = new CustomWaterGenerator(new Vector2f(100,100), 0.05f, new Vector3f(1,0,1));
        float[] heightmap = customWaterGenerator.generate();
        Material waterMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        waterMat.setTexture("ColorMap", new Texture2D(customWaterGenerator.generateWaterTexture(heightmap)));
        waterGeom = customWaterGenerator.draw(assetManager, heightmap, waterMat);
        customWaterGenerator.setCenter((Geometry) waterGeom, player.getPhysicsLocation());
        rootNode.attachChild(waterGeom);
    }

    @Override
    public void simpleInitApp() {



        rootNode.attachChild(SkyFactory.createSky(assetManager,"Textures/rocks.png", SkyFactory.EnvMapType.SphereMap));

        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.5f, -0.5f, -0.5f));
        sun.setColor(ColorRGBA.White);

        rootNode.addLight(sun);
        viewPort.setBackgroundColor(ColorRGBA.White);

        GuiGlobals.initialize(this);
        setUpGuis();
        TerrainGenerator terrainGenerator = new TerrainGenerator(assetManager, cam);
        TerrainQuad terrain = terrainGenerator.getTerrain();
        terrain.scale(352, 25, 352);
        setUpKeys();
        BulletAppState bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        CapsuleCollisionShape playerControl = new CapsuleCollisionShape(1,2);
        player = new CharacterControl(playerControl, 0.5f);
        player.setGravity(30);
        player.setJumpSpeed(20);
        player.setFallSpeed(20);
        player.setPhysicsLocation(new Vector3f(0, 10, 0));
        Vector3f raftSize = new Vector3f(50f,0.2f,50f);
        Material brown = new Material(assetManager,"Common/MatDefs/Misc/Unshaded.j3md");
        brown.setColor("Color", ColorRGBA.Brown);
        Material sphereTex = new Material(assetManager,"Common/MatDefs/Misc/Unshaded.j3md");
        sphereTex.setTexture("ColorMap", assetManager.loadTexture("Textures/download.jpg"));
        BoxCollisionShape raftCollider = new BoxCollisionShape(raftSize);
        RigidBodyControl raftControl = new RigidBodyControl(raftCollider, 0f);
        raft = new Geometry("raft", new Box(raftSize.x,raftSize.y,raftSize.z));
        raft.setLocalTranslation(-1.5f, 0, -1.5f);
        raft.addControl(raftControl);
        sphere = new Geometry("Sphere", new Sphere(66, 66, 1f));
        SphereCollisionShape sphereCollider = new SphereCollisionShape(1f);
        sphereControl = new RigidBodyControl(sphereCollider);
        sphere.addControl(sphereControl);
        sphereControl.setPhysicsLocation(raftControl.getPhysicsLocation().add(new Vector3f(0f,10f,0f)));
        sphere.setMaterial(sphereTex);
        raft.setMaterial(brown);


        rootNode.attachChild(sphere);
        rootNode.attachChild(raft);
        terrain.setLocalTranslation(new Vector3f(0,-50,0));
        rootNode.attachChild(terrain);
        bulletAppState.getPhysicsSpace().add(raftControl);
        bulletAppState.getPhysicsSpace().add(sphereControl);
        bulletAppState.getPhysicsSpace().add(player);
        terrain.addControl(new RigidBodyControl(0));
        bulletAppState.getPhysicsSpace().add(terrain);
        setUpWater();
    }

    @Override
    public void simpleUpdate(float tpf) {
        movement += 0.01f;
//        waterGeom.removeFromParent();
//
//        newData = customWaterGenerator.regenerateMap(player.getPhysicsLocation(), new Vector2f(movement,movement));
//        Material tex = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
//        tex.setTexture("ColorMap", new Texture2D(customWaterGenerator.generateWaterTexture(assetManager,newData)));
//        waterGeom = customWaterGenerator.draw(assetManager, newData, tex);
//        customWaterGenerator.setCenter((Geometry)waterGeom, player.getPhysicsLocation());
//        rootNode.attachChild(waterGeom);
        tick++;
        if (tick >= 200){

            tick=0;
        }
        setDisplayFps(fpsOverlay);
        setDisplayStatView(statsOverlay);
        if (playerHasControl){
            inputManager.setCursorVisible(false);
            if (player.getPhysicsLocation().y <= 0) {
                underwater = true;
            } else {
                underwater = false;
            }
            if (lastTickUnderwater != underwater) {
                player.jump();
            }
            processPlayerInput();
        }else{inputManager.setCursorVisible(true);}

        cam.setLocation(player.getPhysicsLocation());
        lastTickUnderwater = underwater;
    }
    private void processPlayerInput(){
        if (underwater) {
            camDir.set(cam.getDirection().multLocal(playerMoveMult));
            player.setGravity(0);
            player.setFallSpeed(0);
        }else{
            camDir.set(new Vector3f(cam.getDirection().x, 0, cam.getDirection().z)).multLocal(playerMoveMult);
            player.setGravity(30);
            player.setFallSpeed(30);
        }
        camLeft.set(cam.getLeft()).multLocal(playerMoveMult);
        walkDir.set(0, 0, 0);
        if (left) {
            walkDir.addLocal(camLeft);
        }
        if (right) {
            walkDir.addLocal(camLeft.negate());
        }
        if (forward) {
            walkDir.addLocal(camDir);
        }
        if (backward) {
            walkDir.addLocal(camDir.negate());
        }
        if (up){
            if (underwater){
                walkDir.addLocal(new Vector3f(0f,playerMoveMult,0f));
            }else{
                player.jump();
                up = false;
            }
        }
        if (down){
            if (underwater){
                walkDir.addLocal(new Vector3f(0f,-playerMoveMult,0f));
            }
        }
        player.setWalkDirection(walkDir);

    }
    @Override
    public void simpleRender(RenderManager rm) {
        //add render code here (if any)
    }
}
