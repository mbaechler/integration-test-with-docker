import static com.jayway.restassured.RestAssured.given;

import java.net.URL;
import java.net.UnknownHostException;

import org.junit.Rule;
import org.junit.Test;

import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import com.jayway.restassured.RestAssured;


public class ElasticSearchTest {

    public @Rule DockerRule dockerRule = new DockerRule("dockerfile/elasticsearch");
    
    @Test
    public void test() throws NumberFormatException, UnknownHostException {
        RestAssured.port = Integer.valueOf(dockerRule.getPortBindings().get("9200/tcp"));
        Awaitility.await().atMost(Duration.ONE_MINUTE)
            .pollDelay(Duration.ONE_SECOND)
            .until(() -> new URL("http://localhost:" + RestAssured.port + "/").openConnection().getContentLength() > 0);
        given()
            .content("{\"firstname\" : \"Matthieu\", \"lastname\" : \"Baechler\"}")
        .when()
            .post("users/user/mbaechler")
        .then()
            .statusCode(201);
    }

}
