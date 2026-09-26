package lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.GatherInfo.ProSceneGatherInfo.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;

@Service
@Slf4j
public class RemoveProMatchFiles {

    @Value("${api.google_drive.key}")
    private String key;

    private final WebClient proSceneDeleteFile;

    public RemoveProMatchFiles(
            @Qualifier(value = "proSceneGetAndDeleteFile") WebClient proSceneDeleteFile
    ) {
        this.proSceneDeleteFile = proSceneDeleteFile;
    }

    public void removeAllProSceneFiles(Map<String, Path> fileMetadata) throws IOException {

        String fileId = new LinkedHashSet<>(fileMetadata.keySet()).getFirst();
        Path filePath = new ArrayList<>(fileMetadata.values()).getFirst();

        removeLocalProSceneFile(filePath);
        removeGoogleDriveProSceneFile(fileId);

    }

    private void removeLocalProSceneFile(Path filePath) throws IOException {

        if (filePath != null) {
            Files.delete(filePath);
            boolean stillExists = Files.exists(filePath);
            if (!stillExists) {
                log.info("Local pro scene file removed successfully");
            } else {
                log.warn("Failed to remove local file: {}", filePath);
            }
        } else {
            log.warn("Reference to local file wasnt found and file hasnt been removed by method");
        }

    }

    private void removeGoogleDriveProSceneFile(String fileId) {

        proSceneDeleteFile
                .delete()
                .uri(uriBuilder -> uriBuilder
                        .pathSegment(fileId)
                        .queryParam("key", key)
                        .build())
                .retrieve()
                .toBodilessEntity()
                .block();

    }

}
