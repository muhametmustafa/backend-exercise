package jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import play.libs.Json;
import play.mvc.Http;

import java.util.Optional;

@Singleton
public class JwtValidator {
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";

    private final JWTVerifier verifier;

    @Inject
    public JwtValidator(Config config) {
        String secret = config.getString("play.http.secret.key");
        Algorithm algorithm = Algorithm.HMAC256(secret);
        verifier = JWT.require(algorithm)
                .build();
    }

    public ObjectNode verify(String token) {
        ObjectNode node = Json.newObject();
        try {
            DecodedJWT decodedJWT = verifier.verify(token);
            JwtModel jwtModel = new JwtModel(decodedJWT);
            node.put("userId", jwtModel.getUserId());
            return node;
        } catch (JWTVerificationException exception) {
            //invalid signature or claims
            node.put("error", "You are not authorized!");
            return node;
        }

    }

    public ObjectNode validateJwt (Http.Request request) {
        ObjectNode node = Json.newObject();
        Optional<String> authHeader = request.getHeaders().get(HEADER_AUTHORIZATION);
        if(!authHeader.filter(ah -> ah.contains(BEARER)).isPresent()){
            //wrong authorization pattern
            node.put("error", "Wrong authorization header pattern! Use Bearer + token.");
            return node;
        }
        String token = authHeader.map(ah -> ah.replace(BEARER, "")).orElse("");
        return verify(token);

    }

}
