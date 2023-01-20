import java.time.Duration;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

public class DatabasesIntegration {

	private final DatabaseApi oracleDb;
	private final DatabaseApi fileDb;

	public DatabasesIntegration(DatabaseApi oracleDb, DatabaseApi fileDb) {
		this.oracleDb = oracleDb;
		this.fileDb = fileDb;
	}

	public Mono<Void> storeToDatabases(Flux<Integer> integerFlux) {
		// TODO: Main) Write data to both databases
		// TODO: 1) Ensure Transaction is rolled back in case of failure
		// TODO: 2) Ensure All transactions are rolled back ion case any of write operations fails
		// TODO: 3) Ensure Transaction lasts less than 1 sec

		return integerFlux
				.publish(sharedFlux -> {

					Mono<Result> oracleDbResultTransactionId =
							dbWriteInTransaction(oracleDb, sharedFlux);
					Mono<Result> fileDbResultTransactionId = dbWriteInTransaction(fileDb, sharedFlux);

					Mono<Void> result = fileDbResultTransactionId
							.zipWith(oracleDbResultTransactionId)
							.flatMap((tuple) -> {
								Result fileDbResult = tuple.getT1();
								Result oracleDbResult = tuple.getT2();

								if (fileDbResult.error() == null && oracleDbResult.error() == null) {
									return Mono.empty();
								}
								else {
									if (fileDbResult.error() != null && oracleDbResult.error() != null) {
										Throwable error = fileDbResult.error();
										error.addSuppressed(oracleDbResult.error());

										return Mono.error(error);
									}
									else if (fileDbResult.error() != null) {
										long transactionId = oracleDbResult.transactionId();

										Mono<Void> voidMono =
												oracleDb.rollbackTransaction(transactionId);

										return voidMono.then(Mono.error(fileDbResult.error()));
									}
									else {
										long transactionId = fileDbResult.transactionId();

										Mono<Void> voidMono =
												fileDb.rollbackTransaction(transactionId);

										return voidMono.then(Mono.error(oracleDbResult.error()));
									}
								}
							});

					return result;
				})
				.then();
	}

	static Mono<Result> dbWriteInTransaction(DatabaseApi db, Flux<Integer> dataSource) {
		return Mono
				.usingWhen(
						db.<Integer>open().retryWhen(Retry.max(10).filter(t -> t instanceof IllegalAccessError)),
						objectConnection -> objectConnection.write(dataSource),
						objectConnection -> objectConnection.close(),
						(connection, t) -> connection.rollback()
								.then(connection.close()),
						(connection) -> connection.rollback()
								.then(connection.close())
				)
				.map(id -> (Result) new SuccessResult(id))
				.timeout(Duration.ofMillis(1000))
				.onErrorResume(throwable -> Mono.<Result>just(new ErrorResult(throwable)));
	}
}
