package com.ocommerce.services.security.wrapper;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.owasp.encoder.Encode;

import java.io.ByteArrayInputStream;
import java.io.IOException;


public class XSSRequestWrapper extends HttpServletRequestWrapper {

    // Constructor to initialize the wrapper with the original request
    public XSSRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        // Get the original input stream from the wrapped request
        ServletInputStream originalInputStream = super.getInputStream();

        // Read the entire request body into a String
        String requestBody = new String(originalInputStream.readAllBytes());

        // Sanitize the JSON body using the sanitizeInput method
        String sanitizedBody = sanitizeInput(requestBody);

        // Create a new ServletInputStream with the sanitized body
        return new ServletInputStream() {

            private final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                    sanitizedBody.getBytes()
            );

            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }

            @Override
            public boolean isFinished() {
                return byteArrayInputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                // No implementation needed for this example
            }
        };
    }

    // Override the method to get parameter values and sanitize each value
    @Override
    public String[] getParameterValues(String parameter) {
        // Get the original parameter values from the wrapped request
        String[] values = super.getParameterValues(parameter);

        // If the original values are null, return null
        if (values == null) {
            return null;
        }

        // Create an array to store the sanitized values
        int count = values.length;
        String[] sanitizedValues = new String[count];

        // Iterate through the original values, sanitizing each one
        for (int i = 0; i < count; i++) {
            sanitizedValues[i] = sanitizeInput(values[i]);
        }

        // Return the array of sanitized values
        return sanitizedValues;
    }

    // Method to sanitize the input using OWASP Java Encoder
    private String sanitizeInput(String input) {
        return Encode.forHtml(input);

    }
}