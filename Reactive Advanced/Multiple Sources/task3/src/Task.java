import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

public class Task {

	public static Publisher<String> concatSeveralSourcesOrdered(Publisher<String>... sources) {
		return Flux.concat(sources);
	}
}