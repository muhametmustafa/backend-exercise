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
@BsonDiscriminator(key = "type", value = "IMAGE")
public class Image extends Content {
    @NotEmpty
    private String url;

    @Override
    public Type getType() {
        return Type.IMAGE;
    }

    public Image(String dashboardId, String url, List<String> readACL, List<String> writeACL) {
        this.url = url;
        this.dashboardId = dashboardId;
        this.readACL = readACL;
        this.writeACL = writeACL;
    }
}
