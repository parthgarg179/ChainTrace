package com.chaintrace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of ChainTrace.
 *
 * ChainTrace is a licensed verification layer: a company plugs it into its own
 * website, and its customers can look up the full ownership history of a product
 * by its unique ID (IMEI, VIN, ...). ChainTrace reads the company's own database
 * on every lookup and never stores a copy of the ownership data.
 */
@SpringBootApplication
public class ChainTraceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChainTraceApplication.class, args);
    }
}
