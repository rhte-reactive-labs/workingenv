package io.vertx.starter;

import io.vertx.core.Future;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.http.HttpServerResponse;          
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.reactivex.Maybe;
import io.vertx.core.json.JsonObject;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.reactivex.config.ConfigRetriever;
import io.vertx.core.DeploymentOptions;

public class MainVerticle extends AbstractVerticle {
  @Override
  public void start(Future<Void> startFuture) {

    initConfigRetriever()     
      .doOnError(startFuture::fail)     
      .subscribe(ar -> {     
        vertx.deployVerticle(InsultGatewayVerticle.class.getName(), new DeploymentOptions().setConfig(ar));
        
        startFuture.complete();     
      });
      }
      private Maybe<JsonObject> initConfigRetriever() {     

		// Load the default configuration from the classpath
		ConfigStoreOptions defaultConfig = new ConfigStoreOptions()     
			.setType("file")
			.setFormat("json")
			.setOptional(true)
			.setConfig(new JsonObject().put("path", "insult-config.json"));
		
		ConfigStoreOptions kubeConfig = new ConfigStoreOptions()     
				.setType("file")
				.setFormat("json")
				.setOptional(true)
				.setConfig(new JsonObject().put("path", "conf/insult-config.json"));

		// Add the default and container config options into the ConfigRetriever
		
		
		ConfigRetrieverOptions retrieverOptions = new ConfigRetrieverOptions()     
				.addStore(defaultConfig)
				.addStore(kubeConfig);
		

		// Create the ConfigRetriever and return the Maybe when complete
		return ConfigRetriever.create(vertx, retrieverOptions).rxGetConfig().toMaybe(); 
	}
	}
