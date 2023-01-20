import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

public class Task {

	public static Publisher<String> transformToHotWithOperator(Flux<String> coldSource) {
		return coldSource
				.publish()
				.refCount(3);
	}
}