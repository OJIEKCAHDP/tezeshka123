package dev.ojiekcahdp.tezeshka.database;

public interface ResponseHandler<H, R> {
    R handleResponse(H handle) throws Exception;
}
