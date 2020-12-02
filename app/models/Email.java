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

    public Email(String email, String subject, String text, List<String> readACL, List<String> writeACL) {
        this.email = email;
        this.subject = subject;
        this.text = text;
        this.readACL = readACL;
        this.writeACL = writeACL;
    }

}
