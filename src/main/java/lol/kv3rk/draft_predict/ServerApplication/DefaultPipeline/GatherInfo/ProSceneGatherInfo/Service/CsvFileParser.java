package lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.GatherInfo.ProSceneGatherInfo.Service;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lol.kv3rk.draft_predict.ServerApplication.GeneralInfo.Repository.GeneralInfoRepository;
import lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.GatherInfo.ProSceneGatherInfo.DTO.OracleElexirDTO.OracleElexir;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CsvFileParser {

    private final GoogleDriveFileReader googleDriveFileReader;
    private final GeneralInfoRepository generalInfoRepository;
    private Path cachedCSVFilePath;

    public CsvFileParser(GoogleDriveFileReader googleDriveFileReader,
                         GeneralInfoRepository generalInfoRepository) {
        this.googleDriveFileReader = googleDriveFileReader;
        this.generalInfoRepository = generalInfoRepository;
    }

    public List<OracleElexir> readCSVFile() throws IOException {

        int skipLines = getParsedLinesFromDb();
        log.info("Skipping lines: {}", skipLines);

        cachedCSVFilePath = googleDriveFileReader.downloadProSceneFile();
        log.info("Downloaded to: {}", cachedCSVFilePath);

        try (InputStreamReader reader = new InputStreamReader(
                new FileInputStream(cachedCSVFilePath.toFile()), StandardCharsets.UTF_8)) {

            CsvToBean<OracleElexir> csvToBean = new CsvToBeanBuilder<OracleElexir>(reader)
                    .withType(OracleElexir.class)
                    .withSkipLines(skipLines)
                    .withThrowExceptions(false)
                    .build();

            List<OracleElexir> beans = csvToBean.parse()
                    .stream()
                    .filter(e -> e.getDate().equals(LocalDate.now().minusDays(1)))
                    .toList();

            log.info("Successfully parsed: {}", beans.size());
            csvToBean.getCapturedExceptions().forEach(ex ->
                    log.warn("Parse error line {}: {}", ex.getLineNumber(), ex.getMessage())
            );

            int lastParsedLine = skipLines + beans.size();
            log.info("Last parsed line: {}", lastParsedLine);

            updateParsedLinesInDb(beans.size());

            return beans;
        }
    }

    private int getParsedLinesFromDb() {
        return generalInfoRepository.getParsedLines();
    }

    private void updateParsedLinesInDb(int addedLines) {
        generalInfoRepository.updateParsedLines(addedLines);
        log.info("Updated parsed_lines by: {}", addedLines);
    }

    public Map<String, Path> getProSceneFileMetadata() throws IOException {
        if (cachedCSVFilePath != null && Files.exists(cachedCSVFilePath)) {
            return new HashMap<>(Map.of(googleDriveFileReader.getCopiedFileId(), cachedCSVFilePath));
        }
        return null;
    }
}