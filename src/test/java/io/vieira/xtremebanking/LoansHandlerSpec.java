package io.vieira.xtremebanking;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.EmptyByteBuf;
import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

import static org.awaitility.Awaitility.await;

public class LoansHandlerSpec {

    private class RequestsCollector implements BiConsumer<LocalDateTime, List<ByteBuf>>, Callable<Integer> {
        private Integer count;

        @Override
        public Integer call() throws Exception {
            return count;
        }

        @Override
        public void accept(LocalDateTime localDateTime, List<ByteBuf> byteBufs) {
            count = byteBufs.size();
        }
    }

    private class DayOfYearCollector implements BiConsumer<LocalDateTime, List<ByteBuf>>, Callable<Integer> {

        private LocalDateTime dateTime;

        @Override
        public Integer call() throws Exception {
            return dateTime == null ? 0 : dateTime.getDayOfYear();
        }

        @Override
        public void accept(LocalDateTime localDateTime, List<ByteBuf> byteBufs) {
            this.dateTime = localDateTime;
        }
    }

    private LoansHandler handler;
    private final RequestsCollector requestsAsserter = new RequestsCollector();
    private DayOfYearCollector dateAsserter;
    private final int dayDuration = 2;

    @Before
    public void initHandler() {
        handler = new LoansHandler(dayDuration);
        this.dateAsserter = new DayOfYearCollector();
    }

    @Test
    public void shouldBeginAtTheBeginningOfTheCurrentYear() {
        Assert.assertEquals(
                1,
                handler.getCurrentTime().getDayOfYear()
        );
    }

    @Test
    public void shouldUpdateTheCurrentTimeWhenPassingToAnotherDay() {
        handler.setDayCollector(dateAsserter);

        await().atMost(dayDuration * 2 + 1, TimeUnit.SECONDS).until(dateAsserter, new IsEqual<>(2));
    }

    @Test
    public void shouldProperlyReceiveLoansDuringADay(){
        handler.setDayCollector(requestsAsserter);

        IntStream
                .range(0, 5)
                .forEach(operand -> handler.receiveLoanRequest(new EmptyByteBuf(ByteBufAllocator.DEFAULT)));

        await().atMost(dayDuration, TimeUnit.SECONDS).until(requestsAsserter, new IsEqual<>(5));
    }
}
