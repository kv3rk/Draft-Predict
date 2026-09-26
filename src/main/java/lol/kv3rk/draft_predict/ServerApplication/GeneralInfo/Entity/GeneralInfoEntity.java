package lol.kv3rk.draft_predict.ServerApplication.GeneralInfo.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Data
@NoArgsConstructor
@Table(name = "general_info")
@Entity
public class GeneralInfoEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "parsed_lines")
    private int parsedLines;

}
