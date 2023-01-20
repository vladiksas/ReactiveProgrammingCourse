import java.time.Duration;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

public class Task {

	static final int RETRY_COUNT = 5;
	static final String IF_MESSAGE_STARTS_WITH = "[Retry]";


	public static Publisher<String> retryWithBackoffOnError(Flux<String> publisher) {
		return publisher
				.retryWhen(
						Retry.backoff(RETRY_COUNT, Duration.ofMillis(100))
								.filter(exception -> {
									String message = exception.getMessage();
									return message != null && message.startsWith(IF_MESSAGE_STARTS_WITH);
								})
								.maxBackoff(Duration.ofMillis(1600))
				);
	}
}