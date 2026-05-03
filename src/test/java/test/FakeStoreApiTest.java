package test;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
@DisplayName("FakeStore API – Integrationstester")
public class FakeStoreApiTest {
    private static final String BASE_URL               = "https://fakestoreapi.com";
    private static final int    EXPECTED_PRODUCT_COUNT = 20;
    private static final int    TEST_PRODUCT_ID        = 5;

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = BASE_URL;
    }

    // ──────────────────────────────────────────────
    // Testfall 1 – GET /products returnerar 200
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("GET /products – statuskod")
    class ProductsStatusCode {

        @Test
        @DisplayName("Returnerar statuskod 200")
        void getProductsReturns200() {
            given()
                    .when()
                    .get("/products")
                    .then()
                    .statusCode(200);
        }
    }

    // ──────────────────────────────────────────────
    // Testfall 2 – Antal produkter
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("GET /products – antal produkter")
    class ProductCount {

        @Test
        @DisplayName("Svaret innehåller 20 produkter")
        void productListHasExpectedSize() {
            Response response = given()
                    .when()
                    .get("/products")
                    .then()
                    .statusCode(200)
                    .extract().response();

            List<?> products = response.jsonPath().getList("$");

            assertEquals(EXPECTED_PRODUCT_COUNT, products.size(),
                    "Förväntade " + EXPECTED_PRODUCT_COUNT + " produkter men fick " + products.size());
        }
    }

    // ──────────────────────────────────────────────
    // Testfall 3 – Produktfält validering (alla produkter)
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("GET /products – fältvalidering")
    class ProductFields {

        @Test
        @DisplayName("Varje produkt har fälten id, title, price och category")
        void everyProductHasRequiredFields() {
            Response response = given()
                    .when()
                    .get("/products")
                    .then()
                    .statusCode(200)
                    .extract().response();

            List<Map<String, ?>> products = response.jsonPath().getList("$");

            for (Map<String, ?> product : products) {
                assertAll("Produkt saknar fält: " + product.get("id"),
                        () -> assertNotNull(product.get("id"),       "Saknar: id"),
                        () -> assertNotNull(product.get("title"),    "Saknar: title"),
                        () -> assertNotNull(product.get("price"),    "Saknar: price"),
                        () -> assertNotNull(product.get("category"), "Saknar: category")
                );
            }
        }

        @Test
        @DisplayName("price är ett positivt tal för alla produkter")
        void allProductsHavePositivePrice() {
            Response response = given()
                    .when()
                    .get("/products")
                    .then()
                    .statusCode(200)
                    .extract().response();

            // Använd Number istället för Float – API:et returnerar både Integer och Float
            List<Number> prices = response.jsonPath().getList("price");

            for (Number price : prices) {
                assertTrue(price.doubleValue() > 0,
                        "Hittade produkt med ogiltigt pris: " + price);
            }
        }
    }

    // ──────────────────────────────────────────────
    // Testfall 4 – Produkt med ID 5
    // Produkt 5: "John Hardy Women's Legends Naga"
    // Kategori: jewelery  |  Pris: 695
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("GET /products/5 – specifik produkt")
    class SingleProduct {

        @Test
        @DisplayName("Produkt med ID 5 returnerar statuskod 200")
        void getProductByIdReturns200() {
            given()
                    .when()
                    .get("/products/" + TEST_PRODUCT_ID)
                    .then()
                    .statusCode(200);
        }

        @Test
        @DisplayName("Produkt med ID 5 har rätt ID i svaret")
        void productIdMatchesRequest() {
            given()
                    .when()
                    .get("/products/" + TEST_PRODUCT_ID)
                    .then()
                    .statusCode(200)
                    .body("id", equalTo(TEST_PRODUCT_ID));
        }

        @Test
        @DisplayName("Produkt med ID 5 har fälten title, price och category")
        void productHasRequiredFields() {
            given()
                    .when()
                    .get("/products/" + TEST_PRODUCT_ID)
                    .then()
                    .statusCode(200)
                    .body("title",    notNullValue())
                    .body("price",    notNullValue())
                    .body("category", notNullValue());
        }

        @Test
        @DisplayName("Produkt med ID 5 har en icke-tom title")
        void productTitleIsNotEmpty() {
            Response response = given()
                    .when()
                    .get("/products/" + TEST_PRODUCT_ID)
                    .then()
                    .statusCode(200)
                    .extract().response();

            String title = response.jsonPath().getString("title");

            assertFalse(title.isBlank(),
                    "Title är tom för produkt " + TEST_PRODUCT_ID);
        }

        @Test
        @DisplayName("Produkt med ID 5 tillhör kategorin jewelery")
        void productHasExpectedCategory() {
            given()
                    .when()
                    .get("/products/" + TEST_PRODUCT_ID)
                    .then()
                    .statusCode(200)
                    .body("category", equalTo("jewelery"));
        }

        @Test
        @DisplayName("Produkt med ID 5 har ett positivt pris")
        void productPriceIsPositive() {
            Response response = given()
                    .when()
                    .get("/products/" + TEST_PRODUCT_ID)
                    .then()
                    .statusCode(200)
                    .extract().response();

            // Pris kan vara Integer eller Float – använd Number för säker jämförelse
            Number price = response.jsonPath().get("price");

            assertTrue(price.doubleValue() > 0,
                    "Priset är inte positivt: " + price);
        }
    }


}
