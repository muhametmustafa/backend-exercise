package models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.bson.codecs.pojo.annotations.BsonIgnore;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
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

}
