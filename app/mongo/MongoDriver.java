package mongo;

import akka.Done;
import akka.actor.CoordinatedShutdown;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.typesafe.config.Config;
import models.*;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.ClassModel;
import org.bson.codecs.pojo.PojoCodecProvider;
import play.Logger;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.bson.codecs.pojo.Conventions.ANNOTATION_CONVENTION;

public abstract class MongoDriver implements IMongoDB {
    protected final Config config;
    protected MongoClient client;
    private MongoDatabase database;

    protected MongoDriver(CoordinatedShutdown coordinatedShutdown, Config config) {
        this.config = config;

        coordinatedShutdown.addTask(CoordinatedShutdown.PhaseServiceStop(), "shutting-down-mongo-connections", () -> {
            Logger.of(this.getClass()).debug("Shutting down mongo connections!");
            close();
            return CompletableFuture.completedFuture(Done.done());
        });
    }

    /**
     * Get a mongo database connection if not already available
     * @return
     */
    public synchronized MongoDatabase getMongoDatabase() {
        if (database == null) {
            database = this.connect();
        }


        ClassModel<Content> contentTypeClassModel = ClassModel.builder(Content.class).enableDiscriminator(true).build();
        ClassModel<Email> emailClassModel = ClassModel.builder(Email.class).enableDiscriminator(true).build();
        ClassModel<Image> imageClassModel = ClassModel.builder(Image.class).enableDiscriminator(true).build();
        ClassModel<Text> textClassModel = ClassModel.builder(Text.class).enableDiscriminator(true).build();
        ClassModel<Line> lineClassModel = ClassModel.builder(Line.class).enableDiscriminator(true).build();
        ClassModel<Line.LineData> lineDataClassModel = ClassModel.builder(Line.LineData.class).enableDiscriminator(true).build();


        CodecProvider pojoCodecProvider =
                PojoCodecProvider.builder()
                        .conventions(Collections.singletonList(ANNOTATION_CONVENTION))
                        .register("app/models")
                        .register(contentTypeClassModel, emailClassModel, imageClassModel, textClassModel, lineClassModel, lineDataClassModel)
                        .automatic(true)
                        .build();

        final CodecRegistry customEnumCodecs = CodecRegistries.fromCodecs();
        CodecRegistry pojoCodecRegistry = CodecRegistries
                .fromRegistries(
                        MongoClientSettings.getDefaultCodecRegistry(),
                        customEnumCodecs,
                        CodecRegistries.fromProviders(pojoCodecProvider)
                );

        return database.withCodecRegistry(pojoCodecRegistry);
    }

    protected abstract MongoDatabase connect();

    protected abstract void disconnect();

    public MongoClient getMongoClient() {
        return client;
    }

    /**
     * Shut down database connections when the app stops
     */
    private void close() {
        if (database != null) {
            database = null;
        }
        disconnect();
    }
}
