package org.example.loadbalancer;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class DataSourceLoadBalancerTest {

    @Test
    void 요청에_대해서_비율_따라_부하_분산되는_동시성_테스트() throws InterruptedException {
        int slaveRatio = 1;
        int slaveSubRatio = 1;
        List<DataSourceType> dataSourcePool = createDataSourcePool(slaveRatio, slaveSubRatio);
        DataSourceLoadBalancer dataSourceLoadBalancer = new DataSourceLoadBalancer(dataSourcePool);

        AtomicInteger slaveCount = new AtomicInteger(0);
        AtomicInteger slaveSubCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        int numberOfThreads = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch countDownLatch = new CountDownLatch(numberOfThreads);
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                final DataSourceType dataSource = dataSourceLoadBalancer.getDataSource();
                if (dataSource == DataSourceType.SLAVE) {
                    slaveCount.getAndIncrement();
                    return;
                }
                if (dataSource == DataSourceType.SLAVE_SUB) {
                    slaveSubCount.getAndIncrement();
                    return;
                }
                errorCount.getAndIncrement();
            });
            countDownLatch.countDown();
        }
        countDownLatch.await();

        assertAll(
                () -> assertEquals(slaveCount.get(), 500),
                () -> assertEquals(slaveSubCount.get(), 500),
                () -> assertEquals(errorCount.get(), 0)
        );
    }

    private static List<DataSourceType> createDataSourcePool(final int slaveRatio, final int slaveSubRatio) {
        List<DataSourceType> dataSourcePool = new ArrayList<>();
        for (int i = 0; i < slaveRatio; i++) {
            dataSourcePool.add(DataSourceType.SLAVE);
        }
        for (int i = 0; i < slaveSubRatio; i++) {
            dataSourcePool.add(DataSourceType.SLAVE_SUB);
        }
        return dataSourcePool;
    }
}
