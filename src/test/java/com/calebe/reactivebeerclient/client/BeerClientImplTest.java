package com.calebe.reactivebeerclient.client;

import com.calebe.reactivebeerclient.config.WebClientConfig;
import com.calebe.reactivebeerclient.model.BeerDto;
import com.calebe.reactivebeerclient.model.BeerPagedList;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BeerClientImplTest {

    BeerClientImpl beerClient;

    @BeforeEach
    void setUp() {
        beerClient = new BeerClientImpl(new WebClientConfig().webClient());
    }

    @Test
    void listBeers() {
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(null, null,
                null, null, null);

        BeerPagedList pagedList = beerPagedListMono.block();

        Assertions.assertThat(pagedList).isNotNull();
        Assertions.assertThat(pagedList.getContent().size()).isGreaterThan(1);
        System.out.println(pagedList.toList());
    }

    @Test
    void listBeersPageSize10() {
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(1, 10,
                null, null, null);

        BeerPagedList pagedList = beerPagedListMono.block();

        Assertions.assertThat(pagedList).isNotNull();
        Assertions.assertThat(pagedList.getContent().size()).isEqualTo(10);
    }

    @Test
    void listBeersNoRecords() {
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(10, 20,
                null, null, null);

        BeerPagedList pagedList = beerPagedListMono.block();

        Assertions.assertThat(pagedList).isNotNull();
        Assertions.assertThat(pagedList.getContent().size()).isEqualTo(0);
    }

    @Disabled("API returning inventory when should not be")
    @Test
    void getBeerById() {
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(null, null,
                null, null, null);

        BeerPagedList pagedList = beerPagedListMono.block();


        UUID beerId = pagedList.getContent().get(0).getId();

        Mono<BeerDto> beerDtoMono = beerClient.getBeerById(beerId, false);

        BeerDto beerDto = beerDtoMono.block();

        Assertions.assertThat(beerDto.getId()).isEqualTo(beerId);
        Assertions.assertThat(beerDto.getQuantityOnHand()).isNull();
    }

    @Test
    void getBeerByIdShowInventoryTrue() {
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(null, null,
                null, null, null);

        BeerPagedList pagedList = beerPagedListMono.block();


        UUID beerId = pagedList.getContent().get(0).getId();

        Mono<BeerDto> beerDtoMono = beerClient.getBeerById(beerId, true);

        BeerDto beerDto = beerDtoMono.block();

        Assertions.assertThat(beerDto.getId()).isEqualTo(beerId);
        Assertions.assertThat(beerDto.getQuantityOnHand()).isNotNull();
    }
    @Test
    void getBeerByUPC() {
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(null, null,
                null, null, null);

        BeerPagedList pagedList = beerPagedListMono.block();

        String upc = pagedList.getContent().get(0).getUpc();

        Mono<BeerDto> beerDtoMono = beerClient.getBeerByUPC(upc);

        BeerDto beerDto = beerDtoMono.block();

        Assertions.assertThat(beerDto.getUpc()).isEqualTo(upc);
    }

    @Test
    void createBeer() {
        BeerDto beerDto = BeerDto.builder()
                .beerName("Dogfishhead 90 Min IPA")
                .beerStyle("IPA")
                .upc("23484854955")
                .price(new BigDecimal("10.99"))
                .build();

        Mono<ResponseEntity<Void>> responseEntityMono = beerClient.createBeer(beerDto);

        ResponseEntity<Void> responseEntity = responseEntityMono.block();

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    }

    @Test
    void updateBeer() {
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(null, null,
                null, null, null);

        BeerPagedList pagedList = beerPagedListMono.block();
        BeerDto beerDto = pagedList.getContent().get(0);

        BeerDto updatedBeer = BeerDto.builder()
                .beerName("Really Good Beer")
                .beerStyle(beerDto.getBeerStyle())
                .price(beerDto.getPrice())
                .upc(beerDto.getUpc())
                .build();

        Mono<ResponseEntity<Void>> responseEntityMono = beerClient.updateBeer(beerDto.getId(), updatedBeer);
        ResponseEntity<Void> responseEntity = responseEntityMono.block();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    }

    @Test
    void testDeleteBeerHandleException() {
        Mono<ResponseEntity<Void>> responseEntityMono = beerClient.deleteBeerById(UUID.randomUUID());
        ResponseEntity<Void> responseEntity = responseEntityMono.onErrorResume(throwable -> {
            if(throwable instanceof WebClientResponseException) {
                WebClientResponseException exception = (WebClientResponseException) throwable;
                return Mono.just(ResponseEntity.status(exception.getStatusCode()).build());
             } else {
                throw new RuntimeException(throwable);
            }
        }).block();
        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteBeerByIdNotFound() {
        Mono<ResponseEntity<Void>> responseEntityMono = beerClient.deleteBeerById(UUID.randomUUID());
        assertThrows(WebClientResponseException.class, () -> {
            ResponseEntity<Void> responseEntity = responseEntityMono.block();
            Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        });
    }

    @Test
    void deleteBeerById() {
        Mono<BeerPagedList> beerPagedListMono = beerClient.listBeers(null, null,
                null, null, null);

        BeerPagedList pagedList = beerPagedListMono.block();
        BeerDto beerDto = pagedList.getContent().get(0);

        Mono<ResponseEntity<Void>> responseEntityMono = beerClient.deleteBeerById(beerDto.getId());
        ResponseEntity<Void> responseEntity = responseEntityMono.block();

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }


}