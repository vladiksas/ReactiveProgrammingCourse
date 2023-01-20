import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class UserActivityUtils {

	public static Mono<Product> findMostExpansivePurchase(Flux<Order> ordersHistory,
														  ProductsCatalog productsCatalog) {

		return ordersHistory.flatMapIterable(Order::getProductsIds)
			.map(productsCatalog::findById)
			.reduce((product, product2) -> {
				if (product.getPrice() > product2.getPrice()) {
					return product;
				}
				else {
					return product2;
				}
			});
	}
}
