package models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@BsonDiscriminator(key = "type", value = "LINE")
public class Line extends Content {
    private List<LineData> data;

    @Override
    public Type getType() {
        return Type.LINE;
    }

    public @Data static class LineData {
        @NotEmpty
        private String category;

        @NotEmpty
        private int value;
    }
}
