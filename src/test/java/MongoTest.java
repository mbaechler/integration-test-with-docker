import java.net.UnknownHostException;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;


public class MongoTest {

    public @Rule DockerRule dockerRule = new DockerRule("dockerfile/mongodb:latest");
    
    @Test
    public void test() throws NumberFormatException, UnknownHostException {
        MongoClient mongoClient = new MongoClient("localhost", Integer.valueOf(dockerRule.getPortBindings().get("27017/tcp")));
        DB db = mongoClient.getDB("mongotest");
        DBCollection collection = db.getCollection("myCollection");
        collection.insert(new BasicDBObject("name", "docker"));
        Assertions.assertThat(mongoClient.getDatabaseNames()).contains("mongotest", "admin");
    }
    
}
