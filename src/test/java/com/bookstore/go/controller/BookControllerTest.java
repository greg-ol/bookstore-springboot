package com.bookstore.go.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearDatabase() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "books");
    }

    @Test
    void addNewGetUpdate() throws Exception {
        createBook("""
                {"barcode": "B-1", "name": "Book1", "author": "Author1", "quantity":2, "price": 20.00, "type": "STANDARD"}""");

        mvc.perform(get("/api/books/B-1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Book1"));

        mvc.perform(patch("/api/books/B-1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": 20}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(20));
    }

    @Test
    void addRejectsInvalidBod() throws Exception {
        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content("""
                        {"barcode": "test", "quantity": -100}"""))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").value("Validation errors"))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.quantity").exists())
                .andExpect(jsonPath("$.errors.price").exists());

    }

    @Test
    void duplicatedBarcode() throws Exception {
        createBook("""
                {"barcode": "T-1", "name": "Book1", "author": "Author1", "quantity":2, "price": 20.00, "type": "STANDARD"}""");
        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content("""
                        {"barcode": "T-1", "name": "Book1", "author": "Author1", "quantity":2, "price": 20.00, "type": "STANDARD"}"""))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value(containsString("Request conflicting with existing data")));

    }

    @Test
    void searchMultiParam() throws Exception {

        createBook("""
                {"barcode": "B-1", "name": "Book1", "author": "Author1", "quantity":2, "price": 20.00, "type": "STANDARD"}""");
        createBook("""
                {"barcode": "B-2", "name": "Book2", "author": "Author2", "quantity":2, "price": 40.00, "type": "STANDARD"}""");
        createBook("""
                {"barcode": "J-1", "name": "Journal1", "author": "AuthorJ1", "quantity":10, "price": 20.00, "type": "SCIENCE_JOURNAL", "scienceIndex": 5}""");

        mvc.perform(post("/api/books/search").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity":2, "author": "Author2"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].barcode").value("B-2"));
    }

    @Test
    void searchUnknownType() throws Exception {
        mvc.perform(post("/api/books/search").contentType(MediaType.APPLICATION_JSON).content("""
                        {"type": "UNKNOWN"}"""))
                .andExpect(status().isBadRequest());

    }

    @Test
    void unknownBarcode() throws Exception {
        mvc.perform(get("/api/books/xxx"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Not found book with barcode: xxx"));
    }

    @Test
    void testTotalPrice() throws Exception {

        mvc.perform(get("/api/books/total-price"))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/api/books/total-price").param("barcodes", "x"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(containsString("x")));
    }

    @Test
    void totalPriceRun() throws Exception {

        createBook("""
                {"barcode": "B-1", "name": "Book1", "author": "Author1", "quantity":2, "price": 20.00, "type": "STANDARD"}""");
        createBook("""
                {"barcode": "B-2", "name": "Book2", "author": "Author2", "quantity":2, "price": 40.00, "type": "STANDARD"}""");
        createBook("""
                {"barcode": "J-1", "name": "Journal1", "author": "AuthorJ1", "quantity":10, "price": 20.00, "type": "SCIENCE_JOURNAL", "scienceIndex": 5}""");

        mvc.perform(get("/api/books/total-price").param("barcodes", "B-1").param("barcodes", "B-2").param("barcodes", "J-1"))
                .andExpect(status().isOk());
    }

    @Test
    void testBarcodesByQuantity() throws Exception {

        createBook("""
                {"barcode": "B-1", "name": "Book1", "author": "Author1", "quantity":2, "price": 20.00, "type": "STANDARD"}""");
        createBook("""
                {"barcode": "B-2", "name": "Book2", "author": "Author2", "quantity":2, "price": 40.00, "type": "STANDARD"}""");
        createBook("""
                {"barcode": "J-1", "name": "Journal1", "author": "AuthorJ1", "quantity":10, "price": 20.00, "type": "SCIENCE_JOURNAL", "scienceIndex": 5}""");

        String body = mvc.perform(get("/api/books/barcodes-by-quantity"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['2'][1]").value("B-2"))
                .andExpect(jsonPath("$['2'][0]").value("B-1"))
                .andExpect(jsonPath("$['10'][0]").value("J-1"))
                .andReturn().getResponse().getContentAsString();

        assertTrue(body.indexOf("\"2\"") < body.indexOf("\"10\""), "groups must keep numeric order: " + body);

    }

    @Test
    void exportBooks() throws Exception {

        createBook("""
                {"barcode": "B-1", "name": "Book1", "author": "Author1", "quantity":2, "price": 20.00, "type": "STANDARD"}""");
        createBook("""
                {"barcode": "B-2", "name": "Book2", "author": "Author2", "quantity":2, "price": 40.00, "type": "STANDARD"}""");

        mvc.perform(get("/api/books/export"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].barcode").value("B-1"))
                .andExpect(jsonPath("$[0].type").value("STANDARD"))
                .andExpect(jsonPath("$[1].barcode").value("B-2"))
                .andExpect(jsonPath("$[1].name").value("Book2"));

    }

    @Test
    void validImport() throws Exception {

        mvc.perform(post("/api/books/import").contentType(MediaType.APPLICATION_JSON).content("""
                        [{"barcode":"b-200","name":"Dune","author":"Frank Herbert","quantity":2,"price":12.50, "type": "STANDARD"},
                         {"barcode":"j-1","name":"Physics","author":"Various","quantity":3,"price":10.50,
                          "type":"SCIENCE_JOURNAL","scienceIndex":4}]"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdRecords").value(2));
    }

    @Test
    void invalidImportBody() throws Exception {
        mvc.perform(post("/api/books/import").contentType(MediaType.APPLICATION_JSON).content("{\"barcode\":\"b-1\"}"))
                .andExpect(status().isBadRequest());
    }

    private void createBook(String json) throws Exception {
        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk());
    }
}