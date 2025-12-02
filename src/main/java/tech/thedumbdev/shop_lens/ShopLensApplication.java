package tech.thedumbdev.shop_lens;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ShopLensApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopLensApplication.class, args);
	}

}
