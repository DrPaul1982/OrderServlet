package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/order")
public class OrderServlet extends HttpServlet {

    private final List<Order> orders = new ArrayList<>();
    private final ObjectMapper objectMapper;

    public OrderServlet() {
        orders.add(new Order(1, LocalDate.now(), 30.0, List.of(new Product(1, "Product 1", 10.0), new Product(2, "Product 2", 20.0))));
        orders.add(new Order(2, LocalDate.now(), 70.0, List.of(new Product(3, "Product 3", 30.0), new Product(4, "Product 4", 40.0))));
        this.objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");
        objectMapper.writeValue(resp.getWriter(), orders);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        BufferedReader reader = req.getReader();
        Order addOrder = objectMapper.readValue(reader, Order.class);
        orders.add(addOrder);
        resp.setContentType("application/json");
        resp.setStatus(HttpServletResponse.SC_CREATED);
        objectMapper.writeValue(resp.getWriter(), addOrder);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        BufferedReader reader = req.getReader();
        Order updateOrder = objectMapper.readValue(reader, Order.class);

        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            if (order.getId() == updateOrder.getId()) {
                orders.set(i, updateOrder);
                resp.setContentType("application/json");
                resp.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(resp.getWriter(), updateOrder);
                return;
            }
        }
        
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String rawId = req.getParameter("id");
        int id = Integer.parseInt(rawId);

        if (id <= 0) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        for (Order order : orders) {
            if (order.getId() == id) {
                orders.remove(order);
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return;
            }
        }

        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
}
