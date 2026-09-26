package lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.GatherInfo.ProSceneGatherInfo.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;

@Service
@Slf4j
public class RemoveProMatchFiles {

    public void removeAllProSceneFiles(Map<String, Path> fileMetadata) throws IOException {

        String fileId = new LinkedHashSet<>(fileMetadata.keySet()).getFirst();
        Path filePath = new ArrayList<>(fileMetadata.values()).getFirst();

        removeLocalProSceneFile(filePath);
        removeGoogleDriveProSceneFile(fileId);

    }

    private void removeLocalProSceneFile(Path filePath) throws IOException {

        if (filePath != null) {
            Files.delete(filePath);
            boolean removing = Files.exists(filePath);
            if (removing) {
                log.info("Local pro scene file removed");
            } else {
                log.warn("Local file didnt found and hasnt been removed by method");
            }
        } else {
            log.warn("Reference to local file wasnt found and file hasnt been removed by method");
        }

    }

    private void removeGoogleDriveProSceneFile(String fileId) {



    }

}
