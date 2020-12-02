package models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @NotEmpty
    private String id;

    @NotEmpty
    private String name; // can be used enum instead

}
