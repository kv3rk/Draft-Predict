package lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.GatherInfo.ProSceneGatherInfo.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class GoogleDriveFileReader {

    @Value("${api.google_drive.key}")
    private String key;

    private final String downloadPath = "ProSceneFiles/Actual_season/proscene.csv";
    private String copiedFileId;

    private final GatherFileMetadata gatherFileMetadata;
    private final WebClient proSceneGetFile;

    public GoogleDriveFileReader(GatherFileMetadata gatherFileMetadata,
                                 @Qualifier(value = "proSceneGetAndDeleteFile") WebClient proSceneGetFile) {
        this.gatherFileMetadata = gatherFileMetadata;
        this.proSceneGetFile = proSceneGetFile;
    }

    public Path downloadProSceneFile() {
        String fileId = gatherFileMetadata.getActualFileMetadata();
        copiedFileId = fileId;
        Path destination = Paths.get(downloadPath);

        return downloadFileToDisk(fileId, destination)
                .doOnSuccess(path -> log.info("File downloaded to: {}", path))
                .doOnError(e -> log.error("Download failed: {}", e.getMessage()))
                .block();
    }

    private Flux<DataBuffer> readFileChunks(String fileId) {
        return proSceneGetFile
                .get()
                .uri(uriBuilder -> uriBuilder
                        .pathSegment(fileId)
                        .queryParam("alt", "media")
                        .queryParam("key", key)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class).flatMap(body -> {
                            log.error("Google Drive error: {}", body);
                            return Mono.error(new RuntimeException("Failed to read file: " + body));
                        }))
                .bodyToFlux(DataBuffer.class);
    }

    private Mono<Path> downloadFileToDisk(String fileId, Path destination) {

        try {
            Files.createDirectories(destination.getParent());
        } catch (IOException e) {
            return Mono.error(e);
        }

        AtomicLong position = new AtomicLong(0);

        return readFileChunks(fileId)
                .concatMap(chunk -> writeChunk(chunk, destination, position))
                .then(Mono.just(destination));
    }

    private Mono<Void> writeChunk(DataBuffer chunk, Path destination, AtomicLong position) {
        try {
            AsynchronousFileChannel channel = AsynchronousFileChannel.open(
                    destination,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE
            );

            long pos = position.getAndAdd(chunk.readableByteCount());

            return DataBufferUtils.write(Mono.just(chunk), channel, pos)
                    .doFinally(signalType -> {
                        DataBufferUtils.release(chunk);
                        try {
                            channel.close();
                        } catch (IOException e) {
                            log.warn("Failed to close channel", e);
                        }
                    }).then();

        } catch (IOException e) {
            DataBufferUtils.release(chunk);
            return Mono.error(e);
        }
    }

    public String getCopiedFileId() {
        return copiedFileId;
    }
}