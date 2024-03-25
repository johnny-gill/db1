package com.db1.exception;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
public class UncheckedTest {

    @Test
    void unchecked_catch() {
        Service service = new Service();
        service.callCatch();
    }

    @Test
    void unchecked_throw() {
        CheckedTest.Service service = new CheckedTest.Service();
        assertThatThrownBy(service::callThrow).isInstanceOf(CheckedTest.MyCheckedException.class);
    }

    static class MyUncheckedException extends RuntimeException {
        public MyUncheckedException(String message) {
            super(message);
        }
    }

    static class Service {
        Repository repository = new Repository();

        public void callCatch() {
            try {
                repository.call();
            } catch (MyUncheckedException e) {
                log.info("e.getMessage()={}", e.getMessage(), e);
            }
        }

        public void callThrow() {
            repository.call();
        }

    }

    static class Repository {
        public void call() {
            throw new MyUncheckedException("ex");
        }
    }
}
