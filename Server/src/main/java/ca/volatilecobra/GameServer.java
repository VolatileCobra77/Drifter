package ca.volatilecobra;

import ca.volatilecobra.terrain.config.StoragePaths;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.InputListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.event.InputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.network.Network;
import com.jme3.network.Server;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.util.BufferUtils;
import com.jme3.util.SkyFactory;
import com.simsilica.lemur.GuiGlobals;
import org.joml.sampling.BestCandidateSampling;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.FileReader;
import java.util.ArrayList;

public class GameServer extends SimpleApplication implements ActionListener {
    public static boolean SAVE_CHUNKS = true;
    private Node peopleNode;
    private Geometry waterGeom;
    private Material waterMaterial;
    private static String configFilePath;
    private static Config configFile = new Config();
    private static final Gson gson = new Gson();
    private static String ip;
    private static int port =-1;
    private GameWebSocketServer server;
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    private boolean mouseWheelUp = false, mouseWheelDown = false;
    private static boolean graphical = false;
    public static void main(String[] args) {
        for (String arg : args){
            if (arg.equalsIgnoreCase("-gui") || arg.equalsIgnoreCase("--enable-graphical-env")){
                graphical = true;
            };
            if (arg.equalsIgnoreCase("-h") || arg.equalsIgnoreCase("--help")){
                System.out.println("""
                        Server Help:
                             -h or --help: Displays this Help message
                             -gui or --enable-graphical-env: Runs the server with a graphical env, enabling a flycam
                             -c or --config: sets the config file for the server
                             -ip or --address: sets the IP address of the server, overwrites the ip specified by the config file
                             -p or --port: sets the port of the server, overwrites the port specified by the config file
                        \s""");
                System.exit(0);
            }
            if (arg.equalsIgnoreCase("-c")||arg.equalsIgnoreCase("--config")){
                configFilePath = getNextElement(args, arg);
                try{
                    assert configFilePath != null;
                    Gson prettyPrintGson = new GsonBuilder().setPrettyPrinting().create();
                    configFile = gson.fromJson((JsonObject) new JsonParser().parseReader(new FileReader(configFilePath)), Config.class);
                    System.out.println(GREEN + "SERVER: INFO: Config file loaded:" + RESET);
                    System.out.println(prettyPrintGson.toJson(configFile));
                }catch(Exception e){
                    e.printStackTrace();
                    System.exit(1);
                }
            }
            if (arg.equalsIgnoreCase("-ip")||arg.equalsIgnoreCase("--ip")){
                ip = getNextElement(args, arg);
                System.out.println(GREEN + "SERVER: INFO: IP overwrite recieved, using IP: "+ip + RESET);
            }
            if (arg.equalsIgnoreCase("-p")||arg.equalsIgnoreCase("--port")){
                port = Integer.parseInt(getNextElement(args, arg));
                System.out.println(GREEN + "SERVER: INFO: Port overwrite recieved, using Port: " + port + RESET);
            }
        }
        if (ip == null){
            ip = configFile.getServer().getAddress().getIp();
            System.out.println(GREEN + "SERVER: INFO: No IP overwrite recieved, using config file IP: " + ip + RESET);
        }
        if (port == -1){
            port = configFile.getServer().getAddress().getPort();
            System.out.println(GREEN + "SERVER: INFO:No Port overwrite recieved, using config file Port: " + port + RESET);
        }
        Application app = new GameServer();
        AppSettings settings = new AppSettings(true);

        if (graphical){
            settings.setResolution(1280, 720);
            settings.setFrameRate(60);
            settings.setVSync(false);
            settings.setTitle("Drifter Server - GRAPHICAL ENVIRONMENT");
            System.out.println(GREEN + "SERVER: INFO: Running in a graphical environment" + RESET);
        }else {
            settings.setRenderer(null);
            settings.setTitle("Drifter Server");
            System.out.println(GREEN + "SERVER: INFO: Running in a terminal environment" + RESET);
        }
        app.setPauseOnLostFocus(false);
        app.setSettings(settings);
        app.start();

    }
    private void setUpInteractions(){
        inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_EXIT);
        inputManager.addMapping("excapePressed", new KeyTrigger(KeyInput.KEY_ESCAPE));
        inputManager.addListener(this,"excapePressed");
    }

    public void kill(int code){
        server.getOfflinePlayerChecker().stop();
        this.stop();
        System.err.println(RED + "SERVER: INFO: Exiting with code " + code + RESET);
        System.exit(code);
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf){
        switch (name){
            case "excapePressed" -> kill(0);
        }


    }

    @Override
    public void simpleInitApp() {
        peopleNode = new Node();
        GuiGlobals.initialize(this);
        server = new GameWebSocketServer(ip,port,configFile,this,this);
        server.start();
        System.out.println(GREEN + "SERVER: INFO: Server started" + RESET);
        if(graphical){
            System.out.println(GREEN + "SERVER: INFO: Waiting for world" + RESET);
            try {
                while (server.getWorld() == null) {
                    Thread.sleep(100);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println(RED + "SERVER: ERROR: Thread interrupted" + RESET);
                System.exit(1);
            }
            System.out.println(GREEN + "SERVER: INFO: World Acquired" + RESET);
            Node worldNode = server.getWorld().getWorldNode();
            worldNode.setLocalTranslation(new Vector3f(0,-50,0));
            rootNode.attachChild(worldNode);

            AmbientLight ambientLight = new AmbientLight();
            ambientLight.setColor(ColorRGBA.White);
            rootNode.addLight(ambientLight);

            DirectionalLight sun = new DirectionalLight();
            sun.setColor(ColorRGBA.White);
            sun.setDirection(new Vector3f(-1, -1, -1));
            rootNode.addLight(sun);


            setUpWater();
            setUpSkybox();

            flyCam.setMoveSpeed(40);

            setUpInteractions();
            rootNode.attachChild(peopleNode);
        }
    }


    private Geometry createPlane(Vector2f size, float height) {
        int width = (int) size.x;
        int depth = (int) size.y;

        Mesh mesh = new Mesh();

        Vector3f[] vertices = new Vector3f[width * depth];
        int[] indices = new int[(width - 1) * (depth - 1) * 6];

        // Create vertices
        int i = 0;
        for (int z = 0; z < depth; z++) {
            for (int x = 0; x < width; x++) {
                vertices[i] = new Vector3f(x, height, z);
                i++;
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

        Geometry geom = new Geometry("DeformedPlane", mesh);
        return geom;
    }

    public void setUpWater(){

        waterGeom = createPlane(new Vector2f(1000,1000), 0);
        waterGeom.setLocalScale(new Vector3f(5,waterGeom.getLocalScale().y,5));
        //waterGeom.rotate(-(float)Math.PI/2f,0,0);
        waterGeom.setLocalTranslation(-500,0,-500);
        waterMaterial = new Material(assetManager, "MatDefs/ShaderTesting.j3md");
        waterMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
        waterGeom.setQueueBucket(RenderQueue.Bucket.Transparent);

        waterMaterial.setFloat("noiseScalar", 500);
        waterMaterial.setFloat("matTime", 0);
        waterMaterial.setVector3("lightDirection", new Vector3f(0.0f, -1.0f, 0.0f));
        waterMaterial.setFloat("sineNum", configFile.getWater().getSineAmmount());

        waterMaterial.setColor("Color", ColorRGBA.Blue);
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

    private void setUpSkybox() {

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




    @Override
    public void simpleUpdate(float tpf){
        if(graphical){
            inputManager.setCursorVisible(false);
            for (Player player : server.getPlayers()){
                peopleNode.detachAllChildren();
                Geometry sphere = new Geometry(player.name, new Sphere(66, 66, 1f));
                sphere.setLocalTranslation(player.position);
                Material sphereMat = new Material(assetManager,"Common/MatDefs/Misc/Unshaded.j3md");
                sphereMat.setColor("Color", ColorRGBA.White);
                sphere.setMaterial(sphereMat);
                peopleNode.attachChild(sphere);
            }
        }
    }


    public static String getNextElement(String[] array, String criteria) {
        for (int i = 0; i < array.length - 1; i++) {
            if (array[i].equals(criteria)) {
                return array[i + 1];
            }
        }
        // If no element matches the criteria or the matching element is the last one
        return null;
    }
}