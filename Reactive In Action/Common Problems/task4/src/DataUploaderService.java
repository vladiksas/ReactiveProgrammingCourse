import java.time.Duration;
import java.util.concurrent.TimeoutException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

public class DataUploaderService {

	private final HttpClient client;

	public DataUploaderService(HttpClient client) {
		this.client = client;
	}

	public Mono<Void> upload(Flux<OrderedByteBuffer> input) {
		// TODO: send data to a server using the given client
		// TODO: MAX amount of sent buffers MUST be less or equals to 50 per request
		// TODO: frequency of client#send invocation MUST be not often than once per 500 Milliseconds
		// TODO: delivered results MUST be ordered
		// TODO: in case if send operation take more than 1 second it MUST be considered as hanged and be restarted

		// HINT: consider usage of .windowTimeout, onBackpressureBuffer, concatMap, timeout, retryWhen or retryBackoff
		return input.windowTimeout(50, Duration.ofMillis(500))
				.onBackpressureBuffer()
				.concatMap(flux -> {
					long startTime = System.currentTimeMillis();

					return client.send(flux)
							.timeout(Duration.ofSeconds(1))
							.retryWhen(Retry.max(10).filter(t -> t instanceof TimeoutException))
							.retryWhen(Retry.fixedDelay(10, Duration.ofMillis(500)))
							.then(Mono.defer(() -> {
								long diffInTime =
										System.currentTimeMillis() - startTime;

								if (diffInTime < 500) {
									return Mono.delay(Duration.ofMillis(500 - diffInTime))
											.then();
								}

								return Mono.empty();
							}));
				})
				.then();
	}
}
