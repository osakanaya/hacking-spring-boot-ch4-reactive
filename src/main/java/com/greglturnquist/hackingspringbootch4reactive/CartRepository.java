package com.greglturnquist.hackingspringbootch4reactive;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CartRepository extends ReactiveCrudRepository<Cart, String>{

}
