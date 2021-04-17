package com.github.dactiv.framework.spring.security.concurrent;

import com.github.dactiv.framework.spring.security.concurrent.service.ConcurrentDataService;
import com.github.dactiv.framework.spring.security.entity.SocketUserDetails;
import com.github.dactiv.framework.spring.security.enumerate.ConnectStatus;
import com.github.dactiv.framework.spring.web.mobile.DeviceType;
import com.github.dactiv.framework.spring.web.mobile.LiteDevice;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ConcurrentDataServiceTest {

    @Autowired
    private ConcurrentDataService concurrentDataService;

    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void testIncrement() throws InterruptedException {

        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();

        threadPoolTaskExecutor.setMaxPoolSize(1000);
        threadPoolTaskExecutor.setCorePoolSize(100);

        threadPoolTaskExecutor.initialize();

        for (int i = 1; i <= 10; i++) {
            threadPoolTaskExecutor.execute(() -> concurrentDataService.increment());
        }

        Thread.sleep(3000);

        while(threadPoolTaskExecutor.getActiveCount() == 0){
            break;
        }

        Assert.assertEquals(concurrentDataService.getCount(), 1);
        concurrentDataService.setCount(0);

        for (int i = 1; i <= 10; i++) {
            threadPoolTaskExecutor.execute(() -> concurrentDataService.incrementWait());
        }

        Thread.sleep(3000);

        while(threadPoolTaskExecutor.getActiveCount() == 0){
            break;
        }

        Assert.assertEquals(concurrentDataService.getCount(), 10);
        concurrentDataService.setCount(0);

        concurrentDataService.incrementSpringEl();
        concurrentDataService.setCount(1);

        concurrentDataService.incrementArgs(new SocketUserDetails(1, "test", "123456", UUID.randomUUID().toString().replace("-", ""), new LiteDevice(DeviceType.MOBILE), ConnectStatus.Connect.getValue()));
        concurrentDataService.setCount(2);
    }

}
