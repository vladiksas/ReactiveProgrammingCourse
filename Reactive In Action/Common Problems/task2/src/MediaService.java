import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class MediaService {

	private final ServersCatalogue catalogue;

	public MediaService(ServersCatalogue catalogue) {
		this.catalogue = catalogue;
	}

	public Mono<Video> findVideo(String videoName) {
		return Flux.fromIterable(catalogue.list())
				.map(s -> s.searchOne(videoName))
				.collectList()
				.flatMap(Mono::firstWithSignal);
	}
}
