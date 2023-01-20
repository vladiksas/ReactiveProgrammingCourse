import reactor.core.publisher.Mono;

public class Task {

	public static Mono<String> grabDataFromTheGivenContext(Object key) {
		return Mono.deferContextual(contextView ->
				contextView.hasKey(key)
						? Mono.just(contextView.get(key))
						: Mono.empty()
		);
	}
}