package exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString
public @Data
class RequestException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    // either String or List<String>
    private Object description;
    private int statusCode;

    public RequestException (int statusCode, Object message) {
        super(message.toString());
        this.description = message;
        this.statusCode = statusCode;
    }

    @Override
    public String getMessage() {
        return description.toString();
    }

    public int getStatusCode() {
        return statusCode;
    }
}