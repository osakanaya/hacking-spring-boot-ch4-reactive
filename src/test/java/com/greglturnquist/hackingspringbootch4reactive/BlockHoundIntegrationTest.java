package com.greglturnquist.hackingspringbootch4reactive;

import java.time.Duration;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(SpringExtension.class)
public class BlockHoundIntegrationTest {

	InventoryService inventoryService;
	AltInventoryService altInventoryService;
	
	@MockBean
	ItemRepository itemRepository;
	@MockBean
	CartRepository cartRepository;
	
	@BeforeEach
	void setUp() {
		Item sampleItem = new Item("item1", "TV tray", "Alf TV tray", 19.99);
		CartItem sampleCartItem = new CartItem(sampleItem);
		Cart sampleCart = new Cart("My Cart", Collections.singletonList(sampleCartItem));
		
		when(cartRepository.findById(anyString())).thenReturn(Mono.<Cart> empty().hide());
		when(itemRepository.findById(anyString())).thenReturn(Mono.just(sampleItem));
		when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(sampleCart));
		
		altInventoryService = new AltInventoryService(itemRepository, cartRepository);
		inventoryService = new InventoryService(itemRepository, cartRepository);
	}
	
	@Test
	void blockHoundShouldTrapBlockingCall() {
		Mono.delay(Duration.ofSeconds(1))
			.flatMap(tick -> altInventoryService.addToCart("My Cart", "item1"))
			.as(StepVerifier::create)
			.verifyErrorSatisfies(throwable -> {
				assertThat(throwable).hasMessageContaining("block()/blockFirst()/blockLast() are blocking");
			});
	}
	
	@Test
	void blockHoundShouldNotTrapBlockingCall() {
		Mono.delay(Duration.ofSeconds(1))
			.flatMap(tick -> inventoryService.addToCart("My Cart", "item1"))
			.as(StepVerifier::create)
			.expectNextMatches(cart -> {
				assertThat(cart.getCartItems()).extracting(CartItem::getQuantity)
					.containsExactlyInAnyOrder(1);
				assertThat(cart.getCartItems()).extracting(CartItem::getItem)
					.containsExactly(new Item("item1", "TV tray", "Alf TV tray", 19.99));
				
				return true;
			})
			.verifyComplete();
	}

}
