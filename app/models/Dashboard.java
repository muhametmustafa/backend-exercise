package models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.bson.codecs.pojo.annotations.BsonIgnore;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Data
public class Dashboard extends BaseModel implements Accessible{
    @NotEmpty
    private String name;

    @NotEmpty
    private String description;

    private String parentId;

    private Long timestamp;


    private List<String> readACL = new ArrayList<>();
    private List<String> writeACL = new ArrayList<>();

    @BsonIgnore
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Dashboard> children = new ArrayList<>();

    public Dashboard(String name, String description, List<String> readACL, List<String> writeACL) {
        this.name = name;
        this.description = description;
        this.readACL = readACL;
        this.writeACL = writeACL;
    }
}
