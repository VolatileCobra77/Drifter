package ca.volatilecobra.terrain.config;

/**
 * Provides configuration settings for the application.
 */
public class AppConfig {

    private VideoConfig videoConfig;

    public AppConfig() {
        this.videoConfig = new VideoConfig();
    }

    public VideoConfig getVideoConfig() { return this.videoConfig; }
    protected void setVideoConfig(VideoConfig videoConfig) { this.videoConfig = videoConfig; }

}
