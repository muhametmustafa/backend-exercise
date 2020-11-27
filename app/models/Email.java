package models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

import javax.validation.constraints.NotEmpty;

@EqualsAndHashCode(callSuper = true)
@Data
@BsonDiscriminator(key = "type", value = "EMAIL")
public class Email extends Content {
    @NotEmpty
    private String text;

    @NotEmpty
    private String subject;

    @javax.validation.constraints.Email
    @NotEmpty
    private String email;

    @Override
    public Type getType() {
        return Type.EMAIL;
    }

}
