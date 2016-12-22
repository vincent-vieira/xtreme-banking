package io.vieira.xtremebanking;

import io.netty.buffer.ByteBuf;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import static java.time.temporal.TemporalAdjusters.firstDayOfYear;

public class LoansHandler {
    private Observable<LocalDateTime> dayGeneration;
    private final LocalDateTime initial = LocalDateTime.now().with(firstDayOfYear());
    private LocalDateTime currentTime;
    private final Subject<ByteBuf, ByteBuf> bufferObservable = PublishSubject.create();
    private BiConsumer<LocalDateTime, List<ByteBuf>> dayCollector;

    public LoansHandler(int dayDuration) {
        if(dayDuration <= 0) throw new IllegalArgumentException("A day must last at least 1 second");

        this.currentTime = initial;
        this.dayGeneration = Observable
                .interval(dayDuration, TimeUnit.SECONDS)
                .map(number -> initial.plus(number, ChronoUnit.DAYS))
                .doOnNext(localDateTime -> currentTime = localDateTime);
    }

    //TODO : type mapping to unserialize requests ?
    public void receiveLoanRequest(ByteBuf payload){
        bufferObservable.onNext(payload);
    }

    public void setDayCollector(BiConsumer<LocalDateTime, List<ByteBuf>> dayCollector) {
        this.dayCollector = dayCollector;
        this.bufferObservable.buffer(dayGeneration).subscribe(bufferList -> this.dayCollector.accept(currentTime, bufferList));
    }

    public LocalDateTime getCurrentTime() {
        return currentTime;
    }
}
