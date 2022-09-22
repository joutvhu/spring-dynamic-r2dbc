package com.joutvhu.dynamic.r2dbc;

import com.joutvhu.dynamic.r2dbc.entity.TableA;
import com.joutvhu.dynamic.r2dbc.model.ModelC;
import com.joutvhu.dynamic.r2dbc.repository.TableARepository;
import com.joutvhu.dynamic.r2dbc.repository.TableBRepository;
import com.joutvhu.dynamic.r2dbc.repository.TableCRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = R2dbcDynamicApplication.class)
public class R2dbcDynamicApplicationTest {
    @Autowired
    private TableARepository tableARepository;
    @Autowired
    private TableBRepository tableBRepository;
    @Autowired
    private TableCRepository tableCRepository;

    @Test
    public void findA1() {
        tableARepository.findA1(410L, "DSFGT4510A")
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void findA1CNull() {
        tableARepository.findA1(104L, null)
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void findA1CEmpty() {
        tableARepository.findA1(104L, "")
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void findA2() {
        tableARepository.findA2(195L, "DSFGT4510A")
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void findAllA() {
        Flux<TableA> result = tableARepository.findAll();
        Assertions.assertEquals(3, result.count().block());
    }

    @Test
    public void findJ1() {
        tableARepository.findJ(101L, 12042107L)
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void findJ2() {
        tableARepository.findJ(104L, null)
                .as(StepVerifier::create)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    public void findJ3() {
        tableARepository.findJ(null, 41017100L)
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void findJ4() {
        tableARepository.findJ(null, null)
                .as(StepVerifier::create)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    public void findB1StartH() {
        tableBRepository.findB1("HBTVB")
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void findB1StartG() {
        tableBRepository.findB1("GSDRB")
                .as(StepVerifier::create)
                .expectNextCount(5)
                .verifyComplete();
    }

    @Test
    public void findB1All() {
        tableBRepository.findB1(null)
                .as(StepVerifier::create)
                .expectNextCount(5)
                .verifyComplete();
    }

    @Test
    public void findB2() {
        tableBRepository.findB2(50000000L)
                .as(StepVerifier::create)
                .expectNextCount(4)
                .verifyComplete();
    }

    @Test
    public void findB2UL() {
        tableBRepository.findB2(null)
                .as(StepVerifier::create)
                .expectNextCount(5)
                .verifyComplete();
    }

    @Test
    public void findB2P() {
        tableBRepository.findB2(50000000L)
                .as(StepVerifier::create)
                .expectNextCount(4)
                .verifyComplete();
    }

    @Test
    public void sumB1() {
        tableBRepository.sumB1(40000000L)
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void findB4() {
        tableBRepository.findB4(new ModelC(0L, "HTYRB"))
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void findC1() {
        List<Long> c = new ArrayList<>();
        c.add(101L);
        c.add(104L);
        c.add(410L);
        tableCRepository.search(null, "T", c)
                .as(StepVerifier::create)
                .expectNextCount(3)
                .verifyComplete();
    }
}