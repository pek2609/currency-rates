package org.example.currency_rates;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.example.currency_rates.domain.CurrencyRate;
import org.example.currency_rates.domain.CurrencyRateType;
import org.example.currency_rates.dto.CurrencyRatesResponseDto;
import org.example.currency_rates.repository.CurrencyRateRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureWebTestClient
public class CurrencyRatesIntegrationTest {


    private static WireMockServer wireMockServer;
    private static final BigDecimal RATE_THRESHOLD = new BigDecimal("0.0001");


    @BeforeAll
    static void start() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
    }

    @DynamicPropertySource
    public static void setUpMockBaseUrl(DynamicPropertyRegistry registry) {
        registry.add("currency.rates.external.url", wireMockServer::baseUrl);
    }

    @AfterAll
    static void stop() {
        wireMockServer.stop();
    }

    @BeforeEach
    void beforeEach(){
        currencyRateRepository.deleteAll().block();
    }

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private CurrencyRateRepository currencyRateRepository;

    @Test
    void getCurrencyRatesData_successfulProcessing() {
        BigDecimal expectedUsdRate = new BigDecimal("23.111998311823097");
        BigDecimal expectedBtcRate = new BigDecimal("39490.66971705329");

        wireMockServer.stubFor(get(urlEqualTo("/fiat-currency-rates"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"currency\":\"USD\",\"rate\":" + expectedUsdRate + "}]")));
        wireMockServer.stubFor(get(urlEqualTo("/crypto-currency-rates"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"name\":\"BTC\",\"value\":" + expectedBtcRate + "}]")));

        webTestClient.get()
                .uri("/currency-rates/")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CurrencyRatesResponseDto.class)
                .value(response -> {
                    assertEquals(1, response.fiat().size());
                    assertEquals("USD", response.fiat().getFirst().currency());
                    assertRateEquals(expectedUsdRate, response.fiat().getFirst().rate());

                    assertEquals(1, response.crypto().size());
                    assertEquals("BTC", response.crypto().getFirst().currency());
                    assertRateEquals(expectedBtcRate, response.crypto().getFirst().rate());
                });
    }

    @Test
    void getCurrencyRatesData_providerException_FiatFails() {
        BigDecimal expectedEurRate = new BigDecimal("7.69587229829366");
        BigDecimal expectedBtcRate = new BigDecimal("39490.66971705329");

        CurrencyRate savedRate = new CurrencyRate(CurrencyRateType.fiat.name(), "EUR", expectedEurRate);
        currencyRateRepository.save(savedRate).block();

        wireMockServer.stubFor(get(urlEqualTo("/fiat-currency-rates"))
                .willReturn(aResponse().withStatus(500)));
        wireMockServer.stubFor(get(urlEqualTo("/crypto-currency-rates"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"name\":\"BTC\",\"value\":" + expectedBtcRate + "}]")));

        webTestClient.get()
                .uri("/currency-rates/")
                .exchange()
                .expectStatus().isOk()
                .expectBody(CurrencyRatesResponseDto.class)
                .value(response -> {
                    assertEquals(1, response.fiat().size());
                    assertEquals("EUR", response.fiat().getFirst().currency());
                    assertRateEquals(expectedEurRate, response.fiat().getFirst().rate());

                    assertEquals(1, response.crypto().size());
                    assertEquals("BTC", response.crypto().getFirst().currency());
                    assertRateEquals(expectedBtcRate, response.crypto().getFirst().rate());
                });
    }

    @Test
    void getCurrencyRatesData_providerException_CryptoFails() {
        BigDecimal expectedUsdRate = new BigDecimal("23.111998311823097");
        BigDecimal expectedEthRate = new BigDecimal("33915.2530997146");

        CurrencyRate savedRate = new CurrencyRate(CurrencyRateType.crypto.name(), "ETH", expectedEthRate);
        currencyRateRepository.save(savedRate).block();

        wireMockServer.stubFor(get(urlEqualTo("/fiat-currency-rates"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"currency\":\"USD\",\"rate\":" + expectedUsdRate + "}]")));
        wireMockServer.stubFor(get(urlEqualTo("/crypto-currency-rates"))
                .willReturn(aResponse().withStatus(500)));

        webTestClient.get()
                .uri("/currency-rates/")
                .exchange()
                .expectStatus().isOk()
                .expectBody(CurrencyRatesResponseDto.class)
                .value(response -> {
                    assertEquals(1, response.fiat().size());
                    assertEquals("USD", response.fiat().getFirst().currency());
                    assertRateEquals(expectedUsdRate, response.fiat().getFirst().rate());

                    assertEquals(1, response.crypto().size());
                    assertEquals("ETH", response.crypto().getFirst().currency());
                    assertRateEquals(expectedEthRate, response.crypto().getFirst().rate());
                });
    }

    private void assertRateEquals(BigDecimal expected, BigDecimal actual) {
        BigDecimal difference = expected.subtract(actual).abs();
        assertTrue(difference.compareTo(RATE_THRESHOLD) <= 0);
    }

}

