package models;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.codecs.pojo.annotations.BsonIgnore;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, property="type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Email.class, name = "EMAIL"),
        @JsonSubTypes.Type(value = Image.class, name = "IMAGE"),
        @JsonSubTypes.Type(value = Text.class, name = "TEXT"),
        @JsonSubTypes.Type(value = Line.class, name = "LINE")
})
@BsonDiscriminator(key = "type", value = "NONE")
public class Content extends BaseModel implements Accessible{
    @BsonIgnore
    Type type = Type.NONE;

    @NotEmpty
    String dashboardId;

    List<String> readACL = new ArrayList<>();
    List<String> writeACL = new ArrayList<>();

    enum Type {
        EMAIL, TEXT, IMAGE, LINE, NONE
    }
}

