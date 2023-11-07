package com.example;

import com.example.cache.aerospike.configuration.AerospikeClientWrapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

@SpringBootApplication
@EnableCaching
@Slf4j
public class SpringBootCachePocApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootCachePocApplication.class, args);
	}

	@Bean
	RedissonClient getRedissonClient() {
		log.info("Registering Redisson bean [Initializing Redisson Client]");
		try {
			Config config = new Config();

			/*// Make sure same codec used in another service
			StringCodec codec = new StringCodec();
			config.setCodec(codec);*/

			//No need of security for POC but we should use at enterprise level application
			/*if(!StringUtils.isBlank(REDIS_CONNECTION_PASSWORD)){
				singleServerConfig.setPassword(REDIS_CONNECTION_PASSWORD);
			}*/

			log.info("localhost" + "6379" + "product");

			SingleServerConfig singleServerConfig = config.useSingleServer()
					.setAddress(String.format("redis://%s:%s", "localhost", 6379))
					.setDatabase(0);

			return Redisson.create(config);
		} catch (Exception e) {
			log.error("Error in registering Redisson bean [Cannot initialize Redisson Client]", e);
			return null;
		}
	}

	@Bean
	public boolean createAeroConnection(final AerospikeClientWrapper clientWrapper) {
		clientWrapper.createConn();
		return true;
	}

}
