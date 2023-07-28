package com.github.dactiv.framework.idempotent.test.concurrent;

import com.github.dactiv.framework.idempotent.advisor.concurrent.ConcurrentInterceptor;
import com.github.dactiv.framework.idempotent.exception.ConcurrentException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.swing.text.html.parser.Entity;

/**
 * 并发注解单元测试
 *
 * @author maurice.chen
 */
@SpringBootTest
public class ConcurrentDataServiceTest {

    @Autowired
    private ConcurrentDataService concurrentDataService;

    @Test
    public void testConcurrent() throws InterruptedException {

        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();

        threadPoolTaskExecutor.setMaxPoolSize(100);
        threadPoolTaskExecutor.setCorePoolSize(20);

        threadPoolTaskExecutor.initialize();

        for (int i = 1; i <= 10; i++) {
            threadPoolTaskExecutor.execute(() -> {
                try {
                    concurrentDataService.increment();
                } catch (ConcurrentException e) {
                    Assertions.assertEquals(e.getMessage(), ConcurrentInterceptor.DEFAULT_EXCEPTION);
                }
            });
        }

        Thread.sleep(3000);

        while (threadPoolTaskExecutor.getActiveCount() == 0) {
            break;
        }

        Assertions.assertEquals(concurrentDataService.getCount(), 10);
        concurrentDataService.setCount(0);

        concurrentDataService.incrementSpringEl();
        concurrentDataService.setCount(1);

        concurrentDataService.incrementArgs(new Entity("123", 0, "333".toCharArray()));
        concurrentDataService.setCount(2);

        concurrentDataService.incrementConditionArgs(new Entity(null, 0, "333".toCharArray()));
    }

}
