package com.restaurant.manager.client;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Gọi AI service (FastAPI, repo recommender-system) qua mạng nội bộ với X-Internal-Key; FE không gọi thẳng service này. */
@Component
public class AiServiceClient {

	private final RestClient http;
	private final String key;

	public AiServiceClient(@Value("${app.ai.url}") String url, @Value("${app.ai.internal-key}") String key) {
		var factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(Duration.ofSeconds(3));
		factory.setReadTimeout(Duration.ofSeconds(25)); // LLM trung vị khoảng 2 giây, dư cho phân vị cao
		this.http = RestClient.builder().baseUrl(url).requestFactory(factory).build();
		this.key = key;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> chat(Map<String, Object> body) {
		return http.post().uri("/chat").header("X-Internal-Key", key).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.body(body).retrieve().body(Map.class);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> retrain(List<Map<String, Object>> interactions) {
		return http.post().uri("/admin/retrain").header("X-Internal-Key", key).contentType(MediaType.APPLICATION_JSON).body(interactions).retrieve()
				.body(Map.class);
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> health() {
		return http.get().uri("/health").retrieve().body(Map.class);
	}

}
