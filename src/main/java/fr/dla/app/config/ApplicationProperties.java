package fr.dla.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to Dlapp.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 * See {@link io.github.jhipster.config.JHipsterProperties} for a good example.
 */
@ConfigurationProperties(prefix = "application")
public class ApplicationProperties {

    private GoogleMapsApi googleMapsApi;

    public static class GoogleMapsApi {
        private String url;

        private String key;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }

    public GoogleMapsApi getGoogleMapsApi() {
        return googleMapsApi;
    }

    public void setGoogleMapsApi(GoogleMapsApi googleMapsApi) {
        this.googleMapsApi = googleMapsApi;
    }
}
