package models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@BsonDiscriminator(key = "type", value = "TEXT")
public class Text extends Content {

    @NotEmpty
    private String text;

    @Override
    public Type getType() {
        return Type.TEXT;
    }

    public Text(String dashboardId, String text, List<String> readACL, List<String> writeACL) {
        this.text = text;
        this.dashboardId = dashboardId;
        this.readACL = readACL;
        this.writeACL = writeACL;
    }
}
