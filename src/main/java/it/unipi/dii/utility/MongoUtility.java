package it.unipi.dii.utility;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.mongodb.MongoWriteException;
import com.mongodb.client.*;
import com.mongodb.client.result.InsertManyResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static it.unipi.dii.utility.JsonToDocument.convertJsonToDocument;

public class MongoUtility {

    /**
     * Create a collection in the given database.
     *
     * @param mongoDB        MongoDB instances where create the collection.
     * @param collectionName The name of the collection to create.
     */
    public static void createCollection(MongoDatabase mongoDB, String collectionName) {
        try {
            mongoDB.createCollection(collectionName);
        } catch (Exception e) {
            System.out.println("The collection you are trying to create already exists.");
        }
    }

    /**
     * Delete a collection in the given database.
     *
     * @param mongoDB        MongoDB instances where create the collection.
     * @param collectionName The name of the collection to delete.
     */
    public static void dropCollection(MongoDatabase mongoDB, String collectionName) {
        mongoDB.getCollection(collectionName).drop();
    }

    /**
     * Deactivate the command line notifications generated by the mongoDB driver.
     */
    public static void deactivateMongoDBNotifications() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("org.mongodb.driver");
        rootLogger.setLevel(Level.OFF);
    }

    /**
     * Insert a list of documents in the database.
     *
     * @param dstCollection Collection where insert the new documents.
     * @param documents     Documents to insert.
     */
    public static boolean insertDocuments(MongoCollection<Document> dstCollection, List<Document> documents) {
        if (documents.size() == 1) {
            try {
                dstCollection.insertOne(documents.get(0));
            } catch (Exception e) {
                System.out.println("Error in the insertion of the document");
                return false;
            }
        } else if (documents.size() > 1) {
            try {
                InsertManyResult insertResults = dstCollection.insertMany(documents);
                List<ObjectId> insertedIds = new ArrayList<>();
                insertResults.getInsertedIds().values()
                        .forEach(doc -> insertedIds.add(doc.asObjectId().getValue()));
            } catch (Exception e) {
                System.out.println("Error in the insertion of the documents");
                return false;
            }

        }
        return true;
    }

    /**
     * Delete all the documents in the Database that match the match filters.
     *
     * @param dstCollection Collection where delete the documents.
     * @param matchFilter   Filter for select the documents to delete.
     */
    public static void deleteDocuments(MongoCollection<Document> dstCollection, Bson matchFilter) {
        dstCollection.deleteMany(matchFilter);
    }

    /**
     * @param mongoDB       The mongoDB instance.
     * @param dstCollection The target collection.
     * @param fieldName     The target field.
     * @param order         The order of the index:
     *                      <ul>
     *                        <li> 1 : Ascending order.</li>
     *                        <li> -1 :  Descending order.</li>
     *                      </ul>
     */
    public static void createIndex(MongoDatabase mongoDB, String dstCollection, String fieldName, int order) {
        if (order == 1 || order == -1) {
            MongoCollection<Document> collection = mongoDB.getCollection(dstCollection);
            Document index = new Document(fieldName, order);
            collection.createIndex(index);
        }
    }

    /**
     * @param mongoDB       The mongoDB instance.
     * @param dstCollection The target collection.
     * @param fields        The list of the target field.
     * @param order         The order of the index:
     *                      <ul>
     *                        <li> 1 : Ascending order.</li>
     *                        <li> -1 : Descending order.</li>
     *                      </ul>
     */
    public static void createCompoundIndex(MongoDatabase mongoDB, String dstCollection, List<String> fields, List<Integer> order) {
        MongoCollection<Document> collection = mongoDB.getCollection(dstCollection);
        Document index = new Document();
        for (int i = 0; i < fields.size(); i++) {
            index.append(fields.get(i), order.get(i));
        }

        collection.createIndex(index);
    }
    /**
     * @param mongoDB       The mongoDB instance.
     * @param dstCollection The target collection.
     * @param fields        The list of the target field.
     * @param order         The order of the index:
     *                      <ul>
     *                        <li> 1 : Ascending order.</li>
     *                        <li> -1 : Descending order.</li>
     *                      </ul>
     */
    public static void createCompoundIndex(MongoDatabase mongoDB, String dstCollection, String[] fields, Integer[] order) {
        MongoCollection<Document> collection = mongoDB.getCollection(dstCollection);
        Document index = new Document();
        for (int i = 0; i < fields.length; i++) {
            index.append(fields[i], order[i]);
        }

        collection.createIndex(index);
    }
}
