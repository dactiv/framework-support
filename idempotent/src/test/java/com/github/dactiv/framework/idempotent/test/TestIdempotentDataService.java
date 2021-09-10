package com.github.dactiv.framework.idempotent.test;

import com.github.dactiv.framework.idempotent.exception.IdempotentException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.swing.text.html.parser.Entity;

@SpringBootTest
public class TestIdempotentDataService {

    private final static Logger LOGGER = LoggerFactory.getLogger(TestIdempotentDataService.class);

    @Autowired
    private IdempotentDataService idempotentDataService;

    @Test
    public void testIdempotent() {

        for(int i = 0; i < 10; i++) {

            try {

                idempotentDataService.saveEntity(new Entity("123", 1, null), 1);
                Assertions.assertEquals(idempotentDataService.getData().get(1).size(), 1);

                idempotentDataService.saveEntity(new Entity("321", 1, null), 2);
                Assertions.assertEquals(idempotentDataService.getData().get(2).size(), 1);

            } catch (IdempotentException e) {
                LOGGER.warn("出现 IdempotentException 异常，信息为:" + e.getMessage());
                Assertions.assertEquals(e.getMessage(), "请不要重复操作");
            }

        }

        for(int i = 0; i < 10; i++) {

            try {

                idempotentDataService.saveEntityExceptionMessage(new Entity("123", 1, null), 3);
                Assertions.assertEquals(idempotentDataService.getData().get(3).size(), 1);

                idempotentDataService.saveEntityExceptionMessage(new Entity("321", 1, null), 4);
                Assertions.assertEquals(idempotentDataService.getData().get(4).size(), 1);

            } catch (IdempotentException e) {
                LOGGER.warn("出现 IdempotentException 异常，信息为:" + e.getMessage());
                Assertions.assertEquals(e.getMessage(), "不要重复提交");
            }

        }

        for(int i = 0; i < 10; i++) {

            try {

                Thread.sleep(1000);

                idempotentDataService.saveEntityExpirationTime(new Entity("123", 1, null), 5);
                idempotentDataService.saveEntityExpirationTime(new Entity("321", 1, null), 6);

            } catch (IdempotentException | InterruptedException e) {
                LOGGER.warn("出现 IdempotentException 异常，信息为:" + e.getMessage());
                Assertions.assertEquals(e.getMessage(), "请不要重复操作");
            }

        }

        Assertions.assertEquals(idempotentDataService.getData().get(5).size(), 5);
        Assertions.assertEquals(idempotentDataService.getData().get(6).size(), 5);

        for (int i = 0; i < 9; i++) {
            idempotentDataService.nonIdempotentSaveEntity(new Entity("321", 1, null), 1);
            idempotentDataService.nonIdempotentSaveEntity(new Entity("321", 1, null), 2);
            idempotentDataService.nonIdempotentSaveEntity(new Entity("321", 1, null), 3);
            idempotentDataService.nonIdempotentSaveEntity(new Entity("321", 1, null), 4);
        }

        Assertions.assertEquals(idempotentDataService.getData().get(1).size(), 10);
        Assertions.assertEquals(idempotentDataService.getData().get(2).size(), 10);
        Assertions.assertEquals(idempotentDataService.getData().get(3).size(), 10);
        Assertions.assertEquals(idempotentDataService.getData().get(4).size(), 10);
    }
}
