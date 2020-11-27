package jwt;

import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.Data;

import java.util.Date;

@Data
public class JwtModel{
    private String header;
    private String payload;
    private String userId;
    private Date expiresAt;

    public JwtModel(DecodedJWT decodedJWT) {
        this.header = decodedJWT.getHeader();
        this.payload = decodedJWT.getPayload();
        this.expiresAt = decodedJWT.getExpiresAt();
        this.userId = decodedJWT.getClaim("user_id").asString();
    }


}
