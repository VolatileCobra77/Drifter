package ca.volatilecobra;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
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
import com.jme3.scene.*;
import com.jme3.scene.control.Control;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.texture.Texture;
import com.jme3.ui.Picture;
import com.jme3.util.BufferUtils;
import com.jme3.util.SkyFactory;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.simsilica.lemur.*;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class Drifter extends SimpleApplication implements ActionListener {


    //initalize private variables
    private String name;
    private Vector3f lastUpdateLocation;
    private int id;
    private List<player> players = new ArrayList<player>();
    private List<Geometry> playerGeometrys  = new ArrayList<Geometry>();
    private List<player> lastUpdatePlayers = new ArrayList<player>();
    private List<Geometry> lastUpdatePlayerGeometrys = new ArrayList<Geometry>();
    private Gson gson = new Gson();
    private WebSocketClient client;
    private BulletAppState bulletAppState;
    private float[] newData;
    private ArrayList<bouyantObject> bouyantObjects;
    private Spatial waterGeom;
    private Container mainMenu;
    private Container pauseMenu;
    private Container settingsMenu;
    private Container connectionDialog;
    private Picture background;
    private boolean statsOverlay = false, fpsOverlay = false;
    private CustomWaterGenerator customWaterGenerator;
    private float movement = 0f;
    private int tick;
    private boolean sphereMass = false;
    private String keyPressed = "";
    private List<Vector3f> GlobalHeightMap;
    public Material waterMaterial;
    //breaks Java for some reason, looking into it -VolatileCobra77
//    private AudioNode underwater_scary = new AudioNode(assetManager, "Sounds/ambience/underwater_scary.wav", AudioData.DataType.Buffer);
//    private AudioNode above_water_scary = new AudioNode(assetManager, "Sounds/ambience/Above_water_scary.wav", AudioData.DataType.Buffer);
//    private AudioNode above_water_background_sound = new AudioNode(assetManager, "Sounds/background/above_water_background_sound.wav", AudioData.DataType.Buffer);
//    private AudioNode calm_underwater = new AudioNode(assetManager, "Sounds/background/calm_underwater.wav", AudioData.DataType.Buffer);
//    private AudioNode intense_underwater_1 = new AudioNode(assetManager, "Sounds/background/intense_underwater-1.wav", AudioData.DataType.Buffer);
//    private AudioNode intense_underwater_2 = new AudioNode(assetManager, "Sounds/background/intense_underwater-2.wav", AudioData.DataType.Buffer);
//    private AudioNode whale = new AudioNode(assetManager, "Sounds/creature_sounds/whales/whale.wav", AudioData.DataType.Buffer);
//    private AudioNode bigCreature = new AudioNode(assetManager, "Sounds/creature_sounds/misc/big_creature_coming_by.wav");

    private boolean connected = false, setUp = false, playing = false, playerHasControl = false, forward = false, backward = false, left = false, right = false, up = false, down = false, underwater = false, lastTickUnderwater = underwater;
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

    public void AddBouyancy(RigidBodyControl Object, Vector3f Amplitude){

        bouyantObjects.add(new bouyantObject(Object, Amplitude));
    }

    public void CheckBouyancy(){
        for (bouyantObject Object: bouyantObjects){
            if (Object.CheckFloating()){
                Object.physicsControl.applyForce(Object.amplitude, new Vector3f(0,0,0));
            }
        }
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
        Container errorMenu = new Container();
        connectionDialog = new Container();
        Label errorLabel = new Label("There Was an Error connecting to your session, ensure you have the right IP and port!");
        TextField ipField = new TextField("Ip");
        TextField portField = new TextField("Port");
        TextField nameField = new TextField("name");
        Button connect = new Button("Connect");
        errorMenu.addChild(errorLabel);

        connect.addClickCommands(source ->{
            name = nameField.getText();
            if (initalizeOnline(ipField.getText(), portField.getText())){
                System.out.printf("Successfully Initalized a Websocket Connection to %s:%s \n", ipField.getText(), portField.getText());

                mainMenu.removeFromParent();
                background.removeFromParent();
                playing = true;
            }else{
                guiNode.attachChild(errorMenu);
                mainMenu.removeFromParent();
            }

        });
        ipField.setLocalTranslation(0,0,0);
        portField.setLocalTranslation(0,0,0);
        connect.setLocalTranslation(0,0,0);
        connectionDialog.setLocalTranslation(0,0,0);

        connectionDialog.addChild(ipField);
        connectionDialog.addChild(portField);
        connectionDialog.addChild(connect);
        mainMenu = new Container();
        mainMenu.setLocalTranslation((settings.getHeight()/2f)-mainMenu.getSize().x, (settings.getWidth()/2f)-mainMenu.getSize().y, 0);
        Label mainLabel = new Label("Main Menu");
        Button startButton = new Button("Start offline");
        startButton.addClickCommands(source -> {
            initalizeOffline();
            playing = true;
            playerHasControl = true;
            mainMenu.removeFromParent();
            background.removeFromParent();
        });
        Button onlineStartButton = new Button("Start online");
        onlineStartButton.addClickCommands(source ->{
            guiNode.attachChild(connectionDialog);
            mainMenu.removeFromParent();

        });
        mainMenu.addChild(mainLabel);
        startButton.setLocalTranslation(mainMenu.getLocalTranslation().add(new Vector3f(0,-10,0)));
        mainMenu.addChild(startButton);
        onlineStartButton.setLocalTranslation(startButton.getLocalTranslation().add(new Vector3f(0,-10,0)));
//        mainMenu.addChild(onlineStartButton);\
        nameField.setLocalTranslation(startButton.getLocalTranslation().add(new Vector3f(0,-10,0)));
        mainMenu.addChild(nameField);
        ipField.setLocalTranslation(nameField.getLocalTranslation().add(new Vector3f(0,-10,0)));
        mainMenu.addChild(ipField);
        portField.setLocalTranslation(ipField.getLocalTranslation().add(new Vector3f(0,-10,0)));
        mainMenu.addChild(portField);
        connect.setLocalTranslation(portField.getLocalTranslation().add(new Vector3f(0,-10,0)));
        mainMenu.addChild(connect);
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
            client.close();
            System.exit(0);
        });
        pauseMenu.addChild(resumeButton);
        pauseMenu.addChild(settingsButton);
        pauseMenu.addChild(quitButton);
        guiNode.attachChild(background);

    }

    private Mesh createDeformedPlane(Vector3f size) {
        int width = (int) size.x;
        int depth = (int) size.z;
        int height = (int) size.y;

        Mesh mesh = new Mesh();

        Vector3f[] vertices = new Vector3f[width * depth];
        int[] indices = new int[(width - 1) * (depth - 1) * 6];

        // Create vertices
        int i = 0;
        for (int z = 0; z < depth; z++) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                vertices[i] = new Vector3f(x, y, z);
                i++;
                }
            }
        }

        // Create indices
        int index = 0;
        for (int z = 0; z < depth - 1; z++) {
            for (int x = 0; x < width - 1; x++) {
                int topLeft = (z * width) + x;
                int topRight = topLeft + 1;
                int bottomLeft = ((z + 1) * width) + x;
                int bottomRight = bottomLeft + 1;

                // First triangle
                indices[index++] = topLeft;
                indices[index++] = bottomLeft;
                indices[index++] = topRight;

                // Second triangle
                indices[index++] = topRight;
                indices[index++] = bottomLeft;
                indices[index++] = bottomRight;
            }
        }

        mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
        mesh.setBuffer(VertexBuffer.Type.Index, 3, BufferUtils.createIntBuffer(indices));
        mesh.updateBound();

        return mesh;
    }
    public void processServerMessage(String message){
        JsonObject messageJson = JsonParser.parseString(message).getAsJsonObject();
        //System.out.println("Received message: " + message);
        // Process messages from the server
        if (message.startsWith("{connection:")){
            client.send("{terrainRequest:true}");
            client.send(String.format("{player:%s, position: %s, inventory: %s}",name, gson.toJson(player.getPhysicsLocation()), gson.toJson(new ArrayList<List<Integer>>())));
        }
        if (message.startsWith("{heightMap:")) {
            String jsonHeightMap = message.substring("{heightMap:".length(), message.length() - 1);
            Type listType = new TypeToken<List<Vector3f>>() {}.getType();
            List<Vector3f> heightMap = gson.fromJson(jsonHeightMap, listType);
            // Handle the received height map
            System.out.println("Received height map:");

            GlobalHeightMap = heightMap;
            connected = true;
        }
        if (message.startsWith("{players:")){
            JsonArray playersJson = messageJson.get("players").getAsJsonArray();
            Type listType = new TypeToken<List<player>>() {}.getType();
            players = gson.fromJson(playersJson, listType);
            for (player user : (List<player>)gson.fromJson(playersJson, listType)){
                System.out.printf("Player %s at position %s\n", user.name, user.position);
            }
        }
        if (message.startsWith("{id:")){
            id = messageJson.get("id").getAsInt();
            System.out.println("Received id: " + id);
        }
    }

    public void setUpOnlineStuff(List<Spatial> objects, List<Spatial> ores, List<Vector3f> heightmap, List<player> players, List<PhysicsObject> PhysicsObjects){

        setUpSkybox();
        TerrainGenerator terrainGenerator = null;
        try {
             terrainGenerator = new TerrainGenerator(heightmap, assetManager, cam);
        } catch (Error e) {
            client.close();
            e.printStackTrace();
            System.exit(1);
        }
        TerrainQuad terrain = terrainGenerator.getTerrain();
        terrain.scale(352, 25, 352);

        stateManager.attach(bulletAppState);
        CapsuleCollisionShape playerControl = new CapsuleCollisionShape(1,2);
        player = new CharacterControl(playerControl, 0.5f);
        player.setGravity(30);
        player.setJumpSpeed(20);
        player.setFallSpeed(20);
        player.setPhysicsLocation(new Vector3f(0, 10, 0));
        Vector3f raftSize = new Vector3f(5f,0.2f,5f);
        Material brown = new Material(assetManager,"Common/MatDefs/Misc/Unshaded.j3md");
        brown.setColor("Color", ColorRGBA.Brown);
        BoxCollisionShape raftCollider = new BoxCollisionShape(raftSize);
        RigidBodyControl raftControl = new RigidBodyControl(raftCollider, 0f);
        raft = new Geometry("raft", new Box(raftSize.x,raftSize.y,raftSize.z));
        raft.setLocalTranslation(-1.5f, 0, -1.5f);
        raft.addControl(raftControl);
        //sphere = new Geometry("Sphere", new Sphere(66, 66, 1f));
        SphereCollisionShape sphereCollider = new SphereCollisionShape(1f);
        //sphereControl.setPhysicsLocation(raftControl.getPhysicsLocation().add(new Vector3f(0f,10f,0f)));
        raft.setMaterial(brown);

        for (PhysicsObject object:PhysicsObjects){
            Material objMat = new Material(assetManager,"Common/MatDefs/Misc/Unshaded.j3md");
            objMat.setTexture("ColorMap", assetManager.loadTexture("Textures/download.jpg"));
            object.setUp(objMat);
            rootNode.attachChild(object.spatial);
        }


        //rootNode.attachChild(sphere);
        rootNode.attachChild(raft);
        terrain.setLocalTranslation(new Vector3f(0,-50,0));
        rootNode.attachChild(terrain);
        bulletAppState.getPhysicsSpace().add(raftControl);
        //bulletAppState.getPhysicsSpace().add(sphereControl);
        //AddBouyancy(sphereControl, new Vector3f(0,20,0));
        bulletAppState.getPhysicsSpace().add(player);
        terrain.addControl(new RigidBodyControl(0));
        bulletAppState.getPhysicsSpace().add(terrain);
        setUpWater();
    }

    public boolean initalizeOnline(String ip, String port){
        try{
            client = new WebSocketClient(new URI("ws://" + ip + ":" + port)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    System.out.println("Connected to server");
                }

                @Override
                public void onMessage(String message) {
                    processServerMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    System.out.println("Disconnected from server");
                }

                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                }
            };
            client.connect();
            return client.isOpen();
        } catch (URISyntaxException e){
            e.printStackTrace();
            return false;
        }

    }
    public void initalizeOffline(){

        setUpSkybox();


        TerrainGenerator terrainGenerator = new TerrainGenerator(assetManager, cam);
        TerrainQuad terrain = terrainGenerator.getTerrain();
        terrain.scale(352, 25, 352);

        stateManager.attach(bulletAppState);
        CapsuleCollisionShape playerControl = new CapsuleCollisionShape(1,2);
        player = new CharacterControl(playerControl, 0.5f);
        player.setGravity(30);
        player.setJumpSpeed(20);
        player.setFallSpeed(20);
        player.setPhysicsLocation(new Vector3f(0, 10, 0));
        Vector3f raftSize = new Vector3f(5f,0.2f,5f);
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
        AddBouyancy(sphereControl, new Vector3f(0,20,0));
        bulletAppState.getPhysicsSpace().add(player);
        terrain.addControl(new RigidBodyControl(0));
        bulletAppState.getPhysicsSpace().add(terrain);
        setUpWater();
    }

    private void setUpSkybox() {
        bouyantObjects = new ArrayList<bouyantObject>();

        Texture west = assetManager.loadTexture("Textures/Skybox/left.png");
        Texture east = assetManager.loadTexture("Textures/Skybox/right.png");
        Texture north = assetManager.loadTexture("Textures/Skybox/front.png");
        Texture south = assetManager.loadTexture("Textures/Skybox/back.png");
        Texture up = assetManager.loadTexture("Textures/Skybox/top.png");
        Texture down = assetManager.loadTexture("Textures/Skybox/bottom.png");


        rootNode.attachChild(SkyFactory.createSky(assetManager,west,east,north,south,up,down));

        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.5f, -0.5f, -0.5f));
        sun.setColor(ColorRGBA.White);

        rootNode.addLight(sun);
        viewPort.setBackgroundColor(ColorRGBA.White);
    }

    public void setUpWater(){
        Mesh oceanMesh = new Box(1000, 0.002f, 1000);
        waterGeom = new Geometry("Ocean", oceanMesh);
        //waterGeom.rotate(-(float)Math.PI/2f,0,0);
        waterGeom.setLocalTranslation(-500,0,-500);
        waterMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        waterMaterial.setTexture("ColorMap",  assetManager.loadTexture("Textures/WaterTransparent.png"));
//        waterMaterial = new Material(assetManager, "MatDefs/water.j3md");
//        waterMaterial.setVector3("iResolution", new Vector3f(settings.getWidth(),settings.getHeight(),0));
//        waterMaterial.setFloat("iTime", 10);
//        waterMaterial.setFloat("iTimeDelta", 0.01f);
//        waterMaterial.setFloat("iFrameRate", 60);
//        waterMaterial.setInt("iFrame", 1);
//        waterMaterial.setVector4("iMouse", new Vector4f(0,0,0,0));


        waterGeom.setMaterial(waterMaterial);
        rootNode.attachChild(waterGeom);
    }

    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
        GuiGlobals.initialize(this);
        setUpGuis();
        setUpKeys();
    }

    private void syncWithServer(){
        client.send(String.format("{player:%s, position:%s, inventory:%s, id:%s}", name ,gson.toJson(player.getPhysicsLocation()), gson.toJson(new ArrayList<List<Integer>>()), id));
        client.send("getPlayers");
    }


    @Override
    public void simpleUpdate(float tpf) {
        if (connected && !setUp){
            setUp = true;
            setUpOnlineStuff(new ArrayList<>(),new ArrayList<>(),GlobalHeightMap,new ArrayList<>(), new ArrayList<>());
            playing = true;
            playerHasControl = true;
            background.removeFromParent();
        }

        if (playing){

            movement++;
            CheckBouyancy();
            for (Geometry geo:playerGeometrys){
                geo.removeFromParent();
            }
            for (player user :players){
                Geometry playerGeo = new Geometry(user.name, new Sphere(30,30,1));
                Material playerMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                playerMat.setColor("Color", ColorRGBA.Blue);
                playerGeo.setMaterial(playerMat);
                playerGeo.setLocalTranslation(user.position);
                playerGeo.setName(user.name);
                rootNode.attachChild(playerGeo);
                playerGeometrys.add(playerGeo);
            }
            lastUpdatePlayerGeometrys = playerGeometrys;
            lastUpdatePlayers = players;

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
            if (tick >= 200) {

                tick = 0;
            }
            setDisplayFps(fpsOverlay);
            setDisplayStatView(statsOverlay);
            if (playerHasControl) {
                if (movement >=30 && connected){
                    syncWithServer();

                    movement = 0;
                }
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
            } else {
                inputManager.setCursorVisible(true);
            }

            cam.setLocation(player.getPhysicsLocation());
            lastTickUnderwater = underwater;
        }
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
class bouyantObject{
    public RigidBodyControl physicsControl;
    public Vector3f amplitude;
    public boolean shouldFloat;

    public bouyantObject(RigidBodyControl physicsControl, Vector3f amplitude){
        this.physicsControl = physicsControl;
        this.amplitude = amplitude;
    }

    public boolean CheckFloating(){

        shouldFloat = physicsControl.getPhysicsLocation().y <= 0;
        return shouldFloat;

    }
}
class player{
    public String name;
    public Vector3f position;
    public int id;
    public player(String name, Vector3f position, int id){
        this.name = name;
        this.position = position;
        this.id = id;
    }
}
class PhysicsObject{
    public Spatial spatial;
    public CollisionShape collider;
    public RigidBodyControl physicsControl;
    public int id;
    public PhysicsObject(Geometry geometry, RigidBodyControl physicsControl, CollisionShape collider, int id){
        this.spatial = geometry;
        this.physicsControl = physicsControl;
        this.collider = collider;
        this.id = id;
    }
    public PhysicsObject(Geometry geometry, CollisionShape collider, int id){
        this.spatial = geometry;
        this.physicsControl = new RigidBodyControl(collider);
        this.collider = collider;
        this.id = id;
    }
    public void setUp(Material material){
        spatial.setMaterial(material);
        physicsControl.setCollisionShape(collider);
        spatial.addControl(physicsControl);
    }
}