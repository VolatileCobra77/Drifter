package ca.volatilecobra;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.*;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.water.*;

/**
 * This is the Main Class of your Game. It should boot up your game and do initial initialisation
 * Move your Logic into AppStates or Controls or other java classes
 */
public class Drifter extends SimpleApplication implements ActionListener {

    public Spatial raft;
    public Spatial sphere;
    public CharacterControl player;
    public RigidBodyControl sphereControl;
    private boolean sphereMass = false;
    private String keyPressed = "";
    private boolean forward = false, backward = false, left = false, right = false, up = false, down = false, underwater = false, lastTickUnderwater = underwater;

    final private Vector3f walkDir = new Vector3f();
    final private Vector3f camDir = new Vector3f();
    final private Vector3f camLeft = new Vector3f();
    
    final private float playerMoveMult = 0.2f;

    public static void main(String[] args) {
        Drifter app = new Drifter();
        app.setShowSettings(false); //Settings dialog not supported on mac
        app.start();
    }

    private void setUpKeys(){
        inputManager.addMapping("forward", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("backward", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("up", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addMapping("down", new KeyTrigger(KeyInput.KEY_C));
        inputManager.addMapping("down", new KeyTrigger(KeyInput.KEY_LSHIFT));
        inputManager.addListener(this, "forward");
        inputManager.addListener(this, "backward");
        inputManager.addListener(this, "left");
        inputManager.addListener(this, "right");
        inputManager.addListener(this, "up");
        inputManager.addListener(this, "down");
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
        }

    }

    @Override
    public void simpleInitApp() {
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
        bulletAppState.getPhysicsSpace().add(raftControl);
        bulletAppState.getPhysicsSpace().add(sphereControl);
        bulletAppState.getPhysicsSpace().add(player);
    }

    @Override
    public void simpleUpdate(float tpf) {
        //this method will be called every game tick and can be used to make updates
        if (player.getPhysicsLocation().y <=0){
            underwater = true;
        }else{
            underwater = false;
        }
        if (lastTickUnderwater != underwater){
            player.jump();
        }
        processPlayerInput();
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
        cam.setLocation(player.getPhysicsLocation());
    }
    @Override
    public void simpleRender(RenderManager rm) {
        //add render code here (if any)
    }
}
