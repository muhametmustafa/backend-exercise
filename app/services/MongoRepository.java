package services;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import exceptions.RequestException;
import models.validators.HibernateValidator;
import mongo.IMongoDB;
import org.bson.types.ObjectId;
import play.mvc.Http;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class MongoRepository<T> implements BaseRepository<T>{

    @Inject
    IMongoDB mongoDB;

    private MongoCollection<T> getMongoCollection(String collectionName, Class<T> tClass) {
        return mongoDB.getMongoDatabase().getCollection(collectionName, tClass);
    }

    @Override
    public T findById(String collectionName, Class<T> classType, String id) {

        if(!ObjectId.isValid(id)) {
            throw new RequestException(Http.Status.BAD_REQUEST, "invalid hexadecimal representation of an ObjectId: [" + id + "]");
        }

        MongoCollection<T> mongoCollection = getMongoCollection(collectionName, classType);
        T result = mongoCollection
                .find()
                .filter(Filters.eq("_id", new ObjectId(id)))
                .first();

        if(result == null){
            throw new RequestException(Http.Status.NOT_FOUND, "Document with id: " + id + " not found!");
        }
        return result;
    }
    
    @Override
    public List<T> findAll(String collectionName, Class<T> classType) {
        MongoCollection<T> mongoCollection = getMongoCollection(collectionName, classType);
        return mongoCollection
                .find()
                .into(new ArrayList<>());
    }

    @Override
    public T deleteById(String collectionName, Class<T> classType, String id) {

        if(!ObjectId.isValid(id)) {
            throw new RequestException(Http.Status.BAD_REQUEST, "invalid hexadecimal representation of an ObjectId: [" + id + "]");
        }

        MongoCollection<T> mongoCollection = getMongoCollection(collectionName, classType);
        T result = mongoCollection
                .findOneAndDelete(Filters.eq("_id", new ObjectId(id)));

        if(result == null){
            throw new RequestException(Http.Status.NOT_FOUND, "Document with id: " + id + " not found!");
        }
        return result;
    }

    @Override
    public T save(String collectionName, Class<T> classType, T t) {

        String validObject = HibernateValidator.validate(t);
        if(!Strings.isNullOrEmpty(validObject)) {
            throw new RequestException(Http.Status.BAD_REQUEST, validObject);
        }

        MongoCollection<T> mongoCollection = getMongoCollection(collectionName, classType);
        InsertOneResult insertOneResult = mongoCollection.insertOne(t);
        if(!insertOneResult.wasAcknowledged() || insertOneResult.getInsertedId() == null) {
            throw new RequestException(Http.Status.BAD_REQUEST, "Document could not be inserted");
        }
        return mongoCollection.find(Filters.eq("_id", insertOneResult.getInsertedId().asObjectId())).first();
    }

    @Override
    public T update(String collectionName, Class<T> classType, String id, T t) {

        if(!ObjectId.isValid(id)) {
            throw new RequestException(Http.Status.BAD_REQUEST,
                    String.format("Invalid hexadecimal representation of an ObjectId: [%s]", id));
        }

        String validObject = HibernateValidator.validate(t);
        if(!Strings.isNullOrEmpty(validObject)) {
            throw new RequestException(Http.Status.BAD_REQUEST, validObject);
        }

        MongoCollection<T> mongoCollection = getMongoCollection(collectionName, classType);
        T replaced = mongoCollection
                .findOneAndReplace(Filters.eq("_id", new ObjectId(id)), t);

        if(replaced == null) {
            throw new RequestException(Http.Status.NOT_FOUND, "Document with id: " + id + " not found!");
        }
        return replaced;
    }

    public void dropCollection(String collectionName, Class<T> tClass) {
        MongoCollection<T> mongoCollection = getMongoCollection(collectionName, tClass);
        mongoCollection.drop();
    }
}
