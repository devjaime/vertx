package com.walmart.demo;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.core.Promise;


public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> start) {
        //vertx.deployVerticle(new HelloVerticle());
        // vertx.deployVerticle("Hello.groovy");
        vertx.deployVerticle("Hello.js");

        Router router = Router.router(vertx);

        router.route().handler(ctx -> {
            String authToken = ctx.request().getHeader("AUTH_TOKEN"); // simula autorización por token
            if(authToken != null && "mySuperSecretAuthToken".contentEquals(authToken)) {
                ctx.next(); 
            }else {
                ctx.response().setStatusCode(401).setStatusMessage("UNAUTHORIZED").end();
            }
        });
        
        router.get("/api/v1/hello").handler(this::helloVertx);
        router.get("/api/v1/hello/:name").handler(this::helloName);
        router.route().handler(StaticHandler.create("web").setIndexPage("index.html")); //sirve contenido estatico


        ConfigStoreOptions defaultConfig = new ConfigStoreOptions()
            .setType("file")
            .setFormat("json")
            .setConfig(new JsonObject().put("path", "config.json"));

        ConfigRetrieverOptions opts = new ConfigRetrieverOptions()
            .addStore(defaultConfig);

        ConfigRetriever cfgRetriever = ConfigRetriever.create(vertx, opts);

        Handler<AsyncResult<JsonObject>> handler = asyncResult -> this.handleConfigResults(start, router, asyncResult);
        cfgRetriever.getConfig(handler);
   
    }

    void handleConfigResults(Promise<Void> start,Router router, AsyncResult<JsonObject> asyncResult) {
        if(asyncResult.succeeded()){
            JsonObject config = asyncResult.result();
            JsonObject http = config.getJsonObject("http");
            int httpPort = http.getInteger("port");
            vertx.createHttpServer().requestHandler(router).listen(httpPort);
            start.complete();
        } else {
            // Figure out what to do here ...
            start.fail("unable to load configuration.");
        }
    }

    void helloVertx(RoutingContext ctx) {
        vertx.eventBus().request("hello.vertx.addr","", reply -> {
            ctx.request().response().end((String)reply.result().body());
        });
    }

    void helloName(RoutingContext ctx) {
        String name = ctx.pathParam("name");
        vertx.eventBus().request("hello.named.addr", name, reply -> {
            ctx.request().response().end((String)reply.result().body());
        });
    }
}
