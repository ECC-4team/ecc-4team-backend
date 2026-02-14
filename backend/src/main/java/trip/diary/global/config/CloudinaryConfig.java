package trip.diary.global.config;


import com.cloudinary.Cloudinary;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(CloudinaryProperties.class)
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary(CloudinaryProperties props) {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", props.getCloudName());
        config.put("api_key", props.getApiKey());
        config.put("api_secret", props.getApiSecret());
        return new Cloudinary(config);
    }
}