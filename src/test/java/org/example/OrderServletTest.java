package org.example;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServletTest {

    private OrderServlet orderServlet;
    private ObjectMapper objectMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private StringWriter responseWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        orderServlet = new OrderServlet();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        responseWriter = new StringWriter();
        printWriter = new PrintWriter(responseWriter);

        when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    void testDoGet() throws IOException, ServletException {
        orderServlet.doGet(request, response);
        printWriter.flush();

        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(response).setContentType("application/json");
        assertNotNull(responseWriter.toString());
    }

    @Test
    void testDoPost() throws IOException, ServletException {
        Order newOrder = new Order(3, LocalDate.now(), 150.0, List.of(new Product(5, "Product 5", 50.0),
                new Product(6, "Product 6", 100.0)));
        String jsonOrder = objectMapper.writeValueAsString(newOrder);

        BufferedReader reader = new BufferedReader(new StringReader(jsonOrder));
        when(request.getReader()).thenReturn(reader);

        orderServlet.doPost(request, response);
        printWriter.flush();

        verify(response).setStatus(HttpServletResponse.SC_CREATED);
        verify(response).setContentType("application/json");
        assertTrue(responseWriter.toString().contains("Product 5"));
    }

    @Test
    void testDoPut() throws IOException, ServletException {
        Order updatedOrder = new Order(1, LocalDate.now(), 300.0, List.of(new Product(1, "Updated Product", 100.0),
                new Product(2, "Product 2", 200.0)));
        String jsonOrder = objectMapper.writeValueAsString(updatedOrder);

        BufferedReader reader = new BufferedReader(new StringReader(jsonOrder));
        when(request.getReader()).thenReturn(reader);

        orderServlet.doPut(request, response);
        printWriter.flush();

        verify(response).setStatus(HttpServletResponse.SC_OK);
        assertTrue(responseWriter.toString().contains("Updated Product"));
    }

    @Test
    void testDoDelete() throws IOException, ServletException {
        when(request.getParameter("id")).thenReturn("1");

        orderServlet.doDelete(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    @Test
    void testDoDelete_NotFound() throws IOException, ServletException {
        when(request.getParameter("id")).thenReturn("999");

        orderServlet.doDelete(request, response);

        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
}