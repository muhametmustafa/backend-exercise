package models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

import javax.validation.constraints.NotEmpty;

@EqualsAndHashCode(callSuper = true)
@Data
@BsonDiscriminator(key = "type", value = "IMAGE")
public class Image extends Content {
    @NotEmpty
    private String url;

    @Override
    public Type getType() {
        return Type.IMAGE;
    }
}
