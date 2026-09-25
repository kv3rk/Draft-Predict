package lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.GatherInfo.ProSceneGatherInfo.Service;

import lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.GatherInfo.ProSceneGatherInfo.DTO.GoogleDriveDTO.GoogleDriveFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CopyFilesToDrive {

    @Value("${api.google_drive.key}")
    private String key;

    @Value("${api.google_drive.destination_folder_id}")
    private String destinationFolderId;

    private final WebClient proSceneCopyFile;

    public CopyFilesToDrive(
            @Qualifier(value = "proSceneCopyFile") WebClient proSceneCopyFile
    ) {
        this.proSceneCopyFile = proSceneCopyFile;
    }

    public GoogleDriveFile copyFileToDrive(String googleDriveFileId) {
        try {
            ResponseEntity<GoogleDriveFile> response = proSceneCopyFile
                    .post()
                    .uri(uriBuilder -> uriBuilder
                            .pathSegment(googleDriveFileId, "copy")
                            .queryParam("key", key)
                            .build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("parents", List.of(destinationFolderId)))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp ->
                            resp.bodyToMono(String.class).flatMap(body -> {
                                log.error("Google error: {}", body);
                                return Mono.error(new RuntimeException(body));
                            }))
                    .toEntity(new ParameterizedTypeReference<GoogleDriveFile>() {
                    })
                    .block();

            if (response == null) {

                log.warn("Response equals null");

            }

            if (!response.getStatusCode().is2xxSuccessful()) {

                log.warn("Bad response type");
            }

            if (response.getBody() == null) {
                log.warn("Response is empty");
            }

            GoogleDriveFile googleDriveFile = response.getBody();

            log.info("File copied successfully: {}", googleDriveFileId);

            return googleDriveFile;

        } catch (Exception e) {
            log.error("Failed to copy file {}: {}", googleDriveFileId, e.getMessage());
            throw e;
        }
    }
}