import reactor.core.publisher.Flux;

public class PropertiesSourceTask {

	static Properties settings;

	public static Flux<Property<?>> createSequence() {
		return Flux.defer(() -> Flux.fromIterable(settings.asList()));
	}
}