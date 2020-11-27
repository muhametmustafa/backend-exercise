package mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

public interface IMongoDB {
    MongoDatabase getMongoDatabase();
    MongoClient getMongoClient();

}
