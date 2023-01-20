import reactor.core.publisher.Mono;

public class GenerateUUIDTask {

	static UUIDGenerator uuidGenerator;

	public static Mono<String> generateRandomUUID() {
		return Mono.defer(() -> Mono.just(uuidGenerator.secureUUID()));
	}
}