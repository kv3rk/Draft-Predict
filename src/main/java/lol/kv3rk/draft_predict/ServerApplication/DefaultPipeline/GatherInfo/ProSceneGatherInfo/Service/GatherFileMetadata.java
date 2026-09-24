package lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.GatherInfo.ProSceneGatherInfo.Service;

import lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.GatherInfo.ProSceneGatherInfo.DTO.GoogleDriveFile;
import lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.GatherInfo.ProSceneGatherInfo.DTO.GoogleDriveFileList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;

@Service
@Slf4j
public class GatherFileMetadata {

    @Value("${api.google_drive.key}")
    private String key;
    private final WebClient proSceneFilesMetadata;
    private final CopyFilesToDrive copyFilesToDrive;


    public GatherFileMetadata(
            @Qualifier(value = "proSceneFilesMetadata") WebClient proSceneFilesMetadata,
            CopyFilesToDrive copyFilesToDrive
    ) {
        this.proSceneFilesMetadata = proSceneFilesMetadata;
        this.copyFilesToDrive = copyFilesToDrive;
    }


    public String getActualFileMetadata() {

        GoogleDriveFileList fileList = formatResponse();

        GoogleDriveFile actualProSceneFile = fileList.files().getFirst();

        GoogleDriveFile copiedFile = copyFilesToDrive.copyFileToDrive(actualProSceneFile.id());

        log.info("Gathered copied actual pro scene stats file {}: {}", copiedFile.name(), copiedFile.id());

        return copiedFile.id();
    }


    private GoogleDriveFileList formatResponse() {

        ResponseEntity<GoogleDriveFileList> response = proSceneFilesMetadata
                .get()
                .uri((UriBuilder) -> {
                    ;
                    return URI.create(UriBuilder
                            .queryParam("includeItemsFromAllDrives", true)
                            .queryParam("supportsAllDrives", true)
                            .queryParam("key", key)
                            .queryParam("q", "'1gLSw0RLjBbtaNy0dgnGQDAZOHIgCe-HH' in parents and trashed = false")
                            .toUriString());
                })
                .retrieve()
                .toEntity(new ParameterizedTypeReference<GoogleDriveFileList>() {
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

        GoogleDriveFileList fileList = response.getBody();

        log.info("Got pro scene files list {}", fileList.files());

        return fileList;

    }
}
