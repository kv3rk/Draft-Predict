package lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.GatherInfo.ProSceneGatherInfo.Service;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import jakarta.annotation.PreDestroy;
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
import java.util.List;

@Service
@Slf4j
public class CsvFileParser {

    private final GoogleDriveFileReader googleDriveFileReader;
    private Path cachedCSVFilePath;

    public CsvFileParser(GoogleDriveFileReader googleDriveFileReader) {
        this.googleDriveFileReader = googleDriveFileReader;
    }

    public List<OracleElexir> readCSVFile() throws IOException {
        cachedCSVFilePath = googleDriveFileReader.downloadProSceneFile();
        System.out.println("Downloaded to: " + cachedCSVFilePath);

        try (InputStreamReader reader = new InputStreamReader(
                new FileInputStream(cachedCSVFilePath.toFile()), StandardCharsets.UTF_8)) {

            CsvToBean<OracleElexir> csvToBean = new CsvToBeanBuilder<OracleElexir>(reader)
                    .withType(OracleElexir.class)
                    .withSkipLines(108721)
                    .withThrowExceptions(false)
                    .build();

            List<OracleElexir> beans = csvToBean.parse()
                    .stream()
                    .filter(e->e.getDate().equals(LocalDate.now().minusDays(1)))
                    .toList();


            System.out.println("Successfully parsed: " + beans.size());
            csvToBean.getCapturedExceptions().forEach(ex ->
                    System.err.println("Parse error line " + ex.getLineNumber() + ": " + ex.getMessage())
            );

//            beans.forEach(bean -> {
//                StringBuilder stringBuilder = new StringBuilder();
//
//                stringBuilder.append(bean.getGameId()).append(" | ")
//                        .append(bean.getGameId()).append(" | ")
//                        .append(bean.getLeague()).append(" | ")
//                        .append(bean.getDate()).append(" | ")
//                        .append(bean.getPatch()).append(" | ")
//                        .append(bean.getSide()).append(" | ")
//                        .append(bean.getPosition()).append(" | ")
//                        .append(bean.getPlayerName()).append(" | ")
//                        .append(bean.getTeamName()).append(" | ")
//                        .append(bean.getFirstPick()).append(" | ")
//                        .append(bean.getChampion()).append(" | ")
//                        .append(bean.getBan1()).append(" | ")
//                        .append(bean.getBan2()).append(" | ")
//                        .append(bean.getBan3()).append(" | ")
//                        .append(bean.getBan4()).append(" | ")
//                        .append(bean.getBan5()).append(" | ")
//                        .append(bean.getPick1()).append(" | ")
//                        .append(bean.getPick2()).append(" | ")
//                        .append(bean.getPick3()).append(" | ")
//                        .append(bean.getPick4()).append(" | ")
//                        .append(bean.getPick5()).append(" | ")
//                        .append(bean.getResult()).append(" | ");
//
//                System.out.println(stringBuilder);
//            });

            int lastParsedLine = 108721 + beans.size();
            System.out.println("Last parsed line: " + lastParsedLine);

            return beans;
        }
    }

    @PreDestroy
    private void removeCSVFile() throws IOException {
        if (cachedCSVFilePath != null && Files.exists(cachedCSVFilePath)) {
            Files.delete(cachedCSVFilePath);
            log.info("CSV file removed: {}", cachedCSVFilePath);
        }
    }
}