package services;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import exceptions.RequestException;
import org.bson.Document;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import utils.DatabaseUtils;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Singleton
public class SerializationService {
    @Inject
    HttpExecutionContext ec;

    @Inject
    ObjectMapper mapper;

    public <T> CompletableFuture<JsonNode> toJsonNode(T result) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return Json.toJson(result);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, "parsing_exception"));
            }
        }, ec.current());
    }

    public CompletableFuture<Document> parseBody(Http.Request request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JsonNode json = request.body().asJson();
                if (!json.isObject()) {
                    throw new RequestException(Http.Status.BAD_REQUEST, "invalid_parameters");
                }
                return DatabaseUtils.toDocument((ObjectNode) json);
            } catch (RequestException ex) {
                ex.printStackTrace();
                throw new CompletionException(ex);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, "parsing_exception"));
            }
        }, ec.current());
    }

    public <T> CompletableFuture<T> parseBodyOfType(Http.Request request, Class<T> valueType) {
        return CompletableFuture.supplyAsync(() -> this.syncParseBodyOfType(request, valueType), ec.current());
    }

    public <T> T syncParseBodyOfType (Http.Request request, Class<T> valueType) {
        try {
            Optional<T> body = request.body().parseJson(valueType);
            if (!body.isPresent()) {
                throw new RequestException(Http.Status.BAD_REQUEST, "parsing_exception");
            }
            return body.get();
        } catch (RequestException ex) {
            ex.printStackTrace();
            throw new CompletionException(ex);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "service_unavailable"));
        }
    }

    public CompletableFuture<List<Document>> parseListBody(Http.Request request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JsonNode json = request.body().asJson();
                if (!json.isArray()) {
                    throw new RequestException(Http.Status.BAD_REQUEST, "invalid_parameters");
                }
                return DatabaseUtils.toListDocument((ArrayNode) json);
            } catch (RequestException ex) {
                ex.printStackTrace();
                throw new CompletionException(ex);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, "parsing_exception"));
            }
        }, ec.current());
    }

    public <T> CompletableFuture<List<T>> parseFileOfType(Http.Request request, String key, Class<T> valueType) {
        return CompletableFuture.supplyAsync(() -> {
            Http.MultipartFormData<File> data = request.body().asMultipartFormData();
            if (data.getFiles().size() == 0) {
                throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, "invalid_parameters"));
            }
            File file = data.getFile(key).getRef(); // Agon used getFile();
            try {
                JsonNode content = this.fileToObjectNode(file);
                return DatabaseUtils.parseJsonListOfType(content, valueType);
            } catch (JsonProcessingException e) {
                throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, "parsing_exception"));
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "service_unavailable"));
            }
        }, ec.current());
    }


    public JsonNode fileToObjectNode (File which) throws IOException {
        JsonParser parser = mapper.getFactory().createParser(which);
        JsonNode config = mapper.readTree(parser);
        parser.close();
        return config;
    }

    public <T> CompletableFuture<List<T>> parseListBodyOfType (Http.Request request, Class<T> type) {
        return CompletableFuture.supplyAsync(() -> this.syncParseListBodyOfType(request, type), ec.current());
    }

    public <T> List<T> syncParseListBodyOfType (Http.Request request, Class<T> type) {
        JsonNode json = request.body().asJson();
        return DatabaseUtils.parseJsonListOfType(json, type);
    }

}

