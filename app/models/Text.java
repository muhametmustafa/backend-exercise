package models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

import javax.validation.constraints.NotEmpty;

@EqualsAndHashCode(callSuper = true)
@Data
@BsonDiscriminator(key = "type", value = "TEXT")
public class Text extends Content {

    @NotEmpty
    private String text;

    @Override
    public Type getType() {
        return Type.TEXT;
    }
}
