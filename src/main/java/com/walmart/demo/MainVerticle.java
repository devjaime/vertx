package com.walmart.demo;

import io.vertx.core.AbstractVerticle;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start() {
        //La interfaz RequestHandler es un tipo genérico que toma dos parámetros: el tipo de entrada y el tipo de salida. Los dos tipos deben ser objetos. Cuando se utiliza esta interfaz, el entorno de ejecución de Java deserializa el evento en un objeto con el tipo de entrada y serializa la salida en texto.
        // funcion lambda
        vertx.createHttpServer().requestHandler(req ->{
            req.response().end("Hello Vert.x World!");
        }).listen(8080); // escucha en el puerto
    }

}
