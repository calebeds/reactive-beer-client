package com.calebe.reactivebeerclient.client;

import com.calebe.reactivebeerclient.config.WebClientConfig;
import com.calebe.reactivebeerclient.model.BeerPagedList;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
    @Test
    void getBeerById() {
    }



    @Test
    void createBeer() {
    }

    @Test
    void updateBeer() {
    }

    @Test
    void deleteBeerById() {
    }

    @Test
    void getBeerByUPC() {
    }
}