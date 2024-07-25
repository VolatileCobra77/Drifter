package ca.volatilecobra.terrain;

import ca.volatilecobra.terrain.config.AnistropicFilteringAssetListener;
import ca.volatilecobra.terrain.config.StoragePaths;
import ca.volatilecobra.terrain.core.ApplicationContext;
import ca.volatilecobra.terrain.gui.TerrainEditorGui;
import ca.volatilecobra.terrain.gui.debug.DebugHudState;
import ca.volatilecobra.terrain.input.GuiAlternatorListener;
import ca.volatilecobra.terrain.input.movement.SimpleCameraMovement;
import ca.volatilecobra.terrain.interaction.SimpleInteractionState;
import ca.volatilecobra.terrain.world.AnimaliaWorld;
import ca.volatilecobra.terrain.world.World;
import ca.volatilecobra.terrain.world.WorldType;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.system.AppSettings;
import com.jme3.water.WaterFilter;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.event.MouseAppState;
import com.simsilica.lemur.style.BaseStyles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main extends SimpleApplication {

    public static boolean SAVE_CHUNKS = false;

    private static Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String... args) {


        Main main = new Main();

        AppSettings appSettings = new AppSettings(true);
        appSettings.setTitle("Modifiable IsoSurface - jMonkeyEngine");
        appSettings.setResolution(1280, 720);
        // appSettings.setResolution(1680, 1050);
        // appSettings.setFullscreen(true);
        // appSettings.setUseJoysticks(true);
        //appSettings.setVSync(true);



        main.setSettings(appSettings);
        // main.setPauseOnLostFocus(false);
        main.setShowSettings(true);
        main.setDisplayStatView(true);
        main.setDisplayFps(true);
        main.start();

    }

    private Main() {
        //super(new AppState[0]);
        super(new StatsAppState());

        StoragePaths.create();
    }

    private void initLemur() {
        // initialize lemur
        GuiGlobals.initialize(this);

        // only add the gui viewport for collision data to save on calculations.
        stateManager.getState(MouseAppState.class).setIncludeDefaultCollisionRoots(false);
        stateManager.getState(MouseAppState.class).addCollisionRoot(guiViewPort);
    }

    private World earth;

    @Override
    public void simpleInitApp() {

        viewPort.setBackgroundColor(new ColorRGBA(0.5f, 0.6f, 0.7f, 1.0f));

        inputManager.setCursorVisible(false);
        // flyCam.setDragToRotate(true);
        // flyCam.setMoveSpeed(100);

        initLemur();

        // the context for application-specific data
        ApplicationContext appContext = new ApplicationContext(this);

        // light for now...
        AmbientLight ambientLight = new AmbientLight(ColorRGBA.White.mult(0.5f));
        rootNode.addLight(ambientLight);

        DirectionalLight sun = new DirectionalLight(new Vector3f(-1, -1, -1).normalizeLocal(), ColorRGBA.White);
        rootNode.addLight(sun);

        // Anistropic Filtering
        AnistropicFilteringAssetListener anistropicFilteringAssetListener = new AnistropicFilteringAssetListener(appContext.getAppConfig().getVideoConfig().getAnistropicFilteringLevel());
        assetManager.addAssetEventListener(anistropicFilteringAssetListener);

        earth = new AnimaliaWorld(appContext, WorldType.EARTH, 312312, "My World");
        rootNode.attachChild(earth.getWorldNode());

        // PostProcessingState postProcessingState = new PostProcessingState(appContext.getAppConfig(), sun);
        // stateManager.attach(postProcessingState);

        // debug
        DebugHudState debugHudState = new DebugHudState(earth);
        stateManager.attach(debugHudState);

        // simple camera movement
        SimpleCameraMovement cameraMovement = new SimpleCameraMovement(appContext, earth);
        stateManager.attach(cameraMovement);

        // for tabbing in and out of the game / GUI
        GuiAlternatorListener guiAlternatorListener = new GuiAlternatorListener(appContext);
        guiAlternatorListener.addMappings();

        SimpleInteractionState interactionState = new SimpleInteractionState(appContext, earth);
        stateManager.attach(interactionState);

        TerrainEditorGui terrainEditorGui = new TerrainEditorGui(earth, interactionState);
        stateManager.attach(terrainEditorGui);


        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        DirectionalLightShadowFilter shadowFilter = new DirectionalLightShadowFilter(assetManager, 4096, 2);
        shadowFilter.setLight(sun);
        shadowFilter.setShadowIntensity(0.4f);
        shadowFilter.setShadowZExtend(256);
        fpp.addFilter(shadowFilter);

        WaterFilter waterFilter = new WaterFilter();
        waterFilter.setWaterHeight(10f);
        fpp.addFilter(waterFilter);

        //viewPort.addProcessor(fpp);
    }


}
