package io.vieira.xtremebanking.http;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.protocol.http.server.HttpServer;
import io.vieira.xtremebanking.LoansHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiConsumer;

public class TradeServer {

    private HttpServer<ByteBuf, ByteBuf> server;
    private LoansHandler handler;

    public TradeServer withDayDuration(int seconds) {
        this.handler = new LoansHandler(seconds);
        return this;
    }

    public TradeServer onPort(int port) {
        if(port < 1) throw new IllegalArgumentException("The listen port must be positive");
        this.server = RxNetty.createHttpServer(
                port,
                (httpServerRequest, httpServerResponse) ->
                        httpServerRequest
                        .getContent()
                        .flatMap(payload -> {
                            this.handler.receiveLoanRequest(payload);
                            return httpServerResponse.close();
                        })
        );
        return this;
    }

    public TradeServer handleRequestsForTheDay(BiConsumer<LocalDateTime, List<ByteBuf>> dayConsumer){
        if(this.handler == null || this.server == null) throw new IllegalArgumentException("You must initialize properly the server and the day duration");
        this.handler.setDayCollector(dayConsumer);
        return this;
    }

    public void start() {
        if(this.server == null) throw new IllegalArgumentException("You must initialize properly the server and the day duration");
        server.startAndWait();
    }
}
