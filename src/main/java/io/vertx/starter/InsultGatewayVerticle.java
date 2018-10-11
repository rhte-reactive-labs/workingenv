package io.vertx.starter;

import io.vertx.core.Future;
import io.vertx.core.AsyncResult;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.StaticHandler;
import io.vertx.reactivex.config.ConfigRetriever;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.CompositeFuture;
import static io.vertx.starter.ApplicationProperties.*;

public class InsultGatewayVerticle extends AbstractVerticle{
	private static final Logger LOG = LoggerFactory.getLogger(InsultGatewayVerticle.class);

	private WebClient clientSpringboot;
    private WebClient clientSwarm;
    private WebClient clientVertx;
    private ConfigRetriever conf;

	@Override
	  public void start(Future<Void> startFuture) {

		conf = ConfigRetriever.create(vertx);
		Router router = Router.router(vertx);



	    clientSpringboot = WebClient.create(vertx, new WebClientOptions()
	    	      .setDefaultHost(config().getString(GATEWAY_HOST_SPRINGBOOT_NOUN, "springboot-noun-service.vertx-adjective.svc")) 
	    	      .setDefaultPort(config().getInteger(GATEWAY_HOST_SPRINGBOOT_NOUN_PORT, 8080)));

	    	    clientSwarm = WebClient.create(vertx, new WebClientOptions()
	    	      .setDefaultHost(config().getString(GATEWAY_HOST_WILDFLYSWARM_ADJ, "wildflyswarm-adj.vertx-adjective.svc"))
	    	      .setDefaultPort(config().getInteger(GATEWAY_HOST_WILDFLYSWARM_ADJ_PORT, 8080))); 



	    	    clientVertx = WebClient.create(vertx, new WebClientOptions()
	    	            .setDefaultHost(config().getString(GATEWAY_HOST_VERTX_ADJ,"wildflyswarm-adj.vertx-adjective.svc"))
	    	            .setDefaultPort(config().getInteger(GATEWAY_HOST_VERTX_ADJ_PORT,8080))); 
	    	    
	    	    
	    	    
	    	    System.out.println("GATEWAY_HOST_WILDFLYSWARM_ADJ="+config().getString(GATEWAY_HOST_VERTX_ADJ,"springboot-noun-service.vertx-adjective.svc"));
	    	    System.out.println("GATEWAY_HOST_SPRINGBOOT_NOUN="+config().getString(GATEWAY_HOST_SPRINGBOOT_NOUN,"wildflyswarm-adj.vertx-adjective.svc"));
	    	    System.out.println("GATEWAY_HOST_VERTX_ADJ="+config().getString(GATEWAY_HOST_VERTX_ADJ,"wildflyswarm-adj.vertx-adjective.svc"));
	    	    
	    	    

	    	    vertx.createHttpServer().requestHandler(router::accept).listen(8080);
	    	    router.get("/api/insult").handler(this::insultHandler);
	    	    router.get("/*").handler(StaticHandler.create());


	    startFuture.complete();


	}
	Future<JsonObject> getNoun() {    
        Future<JsonObject> fut = Future.future();
        clientSpringboot.get("/api/noun")
                .timeout(3000)
                .rxSend()  

                .map(HttpResponse::bodyAsJsonObject) 
                .doOnError(fut::fail)
                .subscribe(fut::complete);
        return fut;
    }


	Future<JsonObject> getAdjective() {
        Future<JsonObject> fut = Future.future();
        clientSwarm.get("/api/adjective")
                .timeout(3000)
                .rxSend()

                .map(HttpResponse::bodyAsJsonObject)
                .doOnError(fut::fail)
                .subscribe(fut::complete);
        return fut;
    }
	Future<JsonObject> getAdjective2() {
        Future<JsonObject> fut = Future.future();
        clientVertx.get("/api/adjective")
                .timeout(3000)
                .rxSend()

                .map(HttpResponse::bodyAsJsonObject)
                .doOnError(fut::fail)
                .subscribe(fut::complete);
        return fut;
    }
	private AsyncResult<JsonObject> buildInsult(CompositeFuture cf) { 
        JsonObject insult = new JsonObject();
        JsonArray adjectives = new JsonArray();

        // Because there is no garanteed order of the returned futures, we need to parse the results

        for (int i=0; i<=cf.size()-1; i++) {
        	 JsonObject item = cf.resultAt(i);
             if (item.containsKey("adjective")) {
                 adjectives.add(item.getString("adjective"));
             } else {
                 insult.put("noun", item.getString("noun"));
             }

        }
        insult.put("adjectives", adjectives);


        return Future.succeededFuture(insult);
    }
	private void insultHandler(RoutingContext rc) {

		CompositeFuture.all(getNoun(), getAdjective(), getAdjective2()) 
        .setHandler(ar -> {

        	if (ar.succeeded()) {
        		AsyncResult<JsonObject> result=buildInsult(ar.result());
        		 rc.response().putHeader("content-type", "application/json").end(result.result().encodePrettily());
        	}
        	else
        	{
        		System.out.println("error");

        		rc.response().putHeader("content-type", "application/json").end(new JsonObject("Error").encodePrettily());
        	}



          });
	  }

	}