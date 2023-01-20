package repository.impl;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;

import domain.Trade;
import io.r2dbc.client.Handle;
import io.r2dbc.client.R2dbc;
import io.r2dbc.client.Update;
import io.r2dbc.spi.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import repository.TradeRepository;

public class H2TradeRepository implements TradeRepository {
    private static final Logger log = LoggerFactory.getLogger("h2-repo");

    private static String INIT_DB =
        "CREATE TABLE trades (" +
            "id varchar(48), " +
            "trade_timestamp long, " +
            "price float, " +
            "amount float, " +
            "currency varchar(8)," +
            "market varchar(64))";

    private static final String TRADES_COUNT_QUERY = "SELECT COUNT(*) as cnt FROM trades";

    private static final String INSERT_TRADE_QUERY =
        "INSERT INTO trades (id, trade_timestamp, price, amount, currency, market) " +
        "VALUES ($1, $2, $3, $4, $5, $6)";

    private final R2dbc h2Client;

    public H2TradeRepository(ConnectionFactory connectionFactory) {
//        H2ConnectionConfiguration conf = H2ConnectionConfiguration.builder()
//            .url("mem:db;DB_CLOSE_DELAY=-1;TRACE_LEVEL_SYSTEM_OUT=2")
//            .build();
//
//        H2ConnectionFactory h2ConnectionFactory = new H2ConnectionFactory(conf);
        // TODO: Add connection pool
        h2Client = new R2dbc(connectionFactory);
        initDB();
        pingDB();
        reportDbStatistics();
    }

    private void initDB() {
        h2Client.inTransaction(session -> session
            .execute(INIT_DB)
            .doOnNext(i -> log.info("DB SCHEMA WAS INITIALIZED"))
        ).blockLast();
    }

    private void pingDB() {
        h2Client.withHandle(t -> t
            .createQuery("SELECT 6")
            .mapResult(result -> result.map((row, metadata) -> row.get(0))))
            .doOnNext(e -> log.warn("RESULT FOR SELECT 6 QUERY: " + e))
            .subscribe();
    }

    // Stats: log the amount of stored trades to log every 5 seconds
    private void reportDbStatistics() {
        Flux.interval(Duration.ofSeconds(5))
            .flatMap(i -> this.getTradeStats())
            .doOnNext(count -> log.info("------------- [DB STATS] ------------ Trades stored in DB: " + count))
            .subscribeOn(Schedulers.elastic())
            .subscribe();
    }

    @Override
    public Mono<Void> saveAll(List<Trade> trades) {
        return this
            .storeTradesInDb(trades)
            .doOnNext(e -> log.info("--- [DB] --- Inserted " + e + " trades into DB"))
            .then();
    }

    private Mono<Long> getTradeStats() {
        // TODO: Return the current amount of stored trades
        return Mono.defer(() ->
            // TODO: Instead of Mono.empty(), do a query to H2 database using h2Client.withHandle(...)
            // TODO: Use Handle.createQuery & TRADES_COUNT_QUERY with SQL
            // TODO: Map result row by row to get the result of query
            h2Client
                .withHandle(handle ->
                    handle.createQuery(TRADES_COUNT_QUERY)
                          .mapRow(row -> row.get(0, Long.class))
                )
                .single()
        );
    }

    private Mono<Integer> storeTradesInDb(List<Trade> trades) {
        // TODO: Instead of Mono.never()
        // TODO: Use h2Client to create handle, build UPDATE statement, use transactional support!
        // TODO: Add all trades to update using buildInsertStatement(...) method
        // TODO: Return the amount of stored rows
        return Mono.fromDirect(
                h2Client.inTransaction(handle ->
                        this.buildInsertStatement(handle, trades)
                            .execute()
                )
        );
    }

    // --- Helper methods --------------------------------------------------

    // TODO: Use this method in storeTradesInDb(...) method
    private Update buildInsertStatement(Handle handle, List<Trade> trades) {
        Update update = handle.createUpdate(INSERT_TRADE_QUERY);

        Iterator<Trade> tradeIterator = trades.iterator();
        for (int i = 0; tradeIterator.hasNext(); i++) {
            Trade trade = tradeIterator.next();
            if (i != 0) {
                update.add();
            }
            update
                .bind("$1", trade.getId())
                .bind("$2", trade.getTimestamp())
                .bind("$3", trade.getPrice())
                .bind("$4", trade.getAmount())
                .bind("$5", trade.getCurrency())
                .bind("$6", trade.getCurrency());
        }
        return update;
    }

}
