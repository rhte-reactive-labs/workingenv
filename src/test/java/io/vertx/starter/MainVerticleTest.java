package io.vertx.starter;

import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static io.vertx.starter.ApplicationProperties.*;

@RunWith(VertxUnitRunner.class)
public class MainVerticleTest {

  private Vertx vertx;

  @Before
  public void setUp(TestContext tc) {
    vertx = Vertx.vertx();

    JsonObject localConfig=new JsonObject(); 
    localConfig.put(GATEWAY_HOST_SPRINGBOOT_NOUN, "springboot-noun-service-devenv-user2.apps.9249.rhte.opentlc.com");
    localConfig.put(GATEWAY_HOST_SPRINGBOOT_NOUN_PORT, 80);
    localConfig.put(GATEWAY_HOST_WILDFLYSWARM_ADJ, "wildflyswarm-adj-devenv-user2.apps.9249.rhte.opentlc.com");
    localConfig.put(GATEWAY_HOST_WILDFLYSWARM_ADJ_PORT, 80);
    localConfig.put(GATEWAY_HOST_VERTX_ADJ, "vertx-adjective-service-devenv-user2.apps.9249.rhte.opentlc.com");
    localConfig.put(GATEWAY_HOST_VERTX_ADJ_PORT, 80);


    vertx.deployVerticle(MainVerticle.class.getName(), tc.asyncAssertSuccess());
    vertx.deployVerticle(InsultGatewayVerticle.class.getName(),new DeploymentOptions().setConfig(localConfig), tc.asyncAssertSuccess());
  }

  @After
  public void tearDown(TestContext tc) {
    vertx.close(tc.asyncAssertSuccess());
  }

  @Test
  public void testThatTheServerIsStarted(TestContext tc) {
    Async async = tc.async();
    vertx.createHttpClient().getNow(8080, "localhost", "/api/insult", response -> {

      response.bodyHandler(body -> {
    	tc.assertTrue(body.length() > 0);
    	tc.assertTrue(body.toJsonObject().containsKey("noun")); 
        async.complete();
      });
    });
  }

}