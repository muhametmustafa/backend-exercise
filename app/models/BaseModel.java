package models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import mongo.serializers.ObjectIdDeSerializer;
import mongo.serializers.ObjectIdStringSerializer;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;

import java.io.Serializable;

@EqualsAndHashCode(of={"id"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public @Data
class BaseModel implements Cloneable, Serializable {
    @BsonId
    @JsonSerialize(using = ObjectIdStringSerializer.class)
    @JsonDeserialize(using = ObjectIdDeSerializer.class)
    public ObjectId id;

    @Setter(AccessLevel.NONE)
    @BsonIgnore
    @JsonIgnore
    protected Long createdAt;
    @JsonIgnore
    protected Long updatedAt;

    public void setId(ObjectId id) {
        if (id == null) {
            this.id = null;
            this.createdAt = null;
            return;
        }
        this.id = id;
        this.createdAt = id.getTimestamp() * 1000L;
    }

    @BsonIgnore
    @JsonIgnore
    public Long getLastUpdate() {
        if (updatedAt != null) {
            return updatedAt;
        }
        return createdAt;
    }

    @Override
    public BaseModel clone() throws CloneNotSupportedException {
        BaseModel clone = (BaseModel) super.clone();
        clone.setId(this.getId());
        clone.setUpdatedAt(this.getUpdatedAt());
        return clone;
    }
}

