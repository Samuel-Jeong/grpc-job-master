package com.telcoware.job_master_app_demo.config;

import com.telcoware.job_master_app_demo.data.dto.job.CronJobInfoDto;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * packageName    : com.telcoware.job_master_app_demo.config
 * fileName       : CronJobConfig
 * author         : samuel
 * date           : 25. 10. 28.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 25. 10. 28.        samuel       최초 생성
 */
@Slf4j
@Getter
@Configuration
public class CronJobConfig {

    @Value("${spring.profiles.active}")
    private String profile;

    private Map<String, LinkedHashMap<String, String>> cronJobMap;

    @PostConstruct
    public void loadYaml() {
        String targetYmlName = "cronjob-" + profile + ".yml";
        try (InputStream input = new ClassPathResource(targetYmlName).getInputStream()) {
            Yaml yaml = new Yaml();
            cronJobMap = yaml.load(input);
            log.info("->SVC::Loaded cronjob map: {}", cronJobMap);
        } catch (Exception e) {
            throw new RuntimeException("->SVC::Failed to load targetYmlName", e);
        }
    }

    /**
     * 특정 작업 이름으로 조회
     */
    public LinkedHashMap<String, String> getCronJobInfoByKey(String key) {
        return cronJobMap.get(key);
    }

    public List<Map.Entry<String, LinkedHashMap<String, String>>> getAllCronJobs() {
        return new ArrayList<>(cronJobMap.entrySet());
    }

}
