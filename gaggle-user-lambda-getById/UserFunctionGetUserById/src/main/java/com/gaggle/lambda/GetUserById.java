package com.gaggle.lambda;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.hibernate.SessionFactory;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaggle.config.HibernateUtil;
import com.gaggle.exceptions.NoUserFoundException;
import com.gaggle.model.User;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Handler for requests to Lambda function.
 */
public class GetUserById implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
	
	
	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", "application/json");
		headers.put("X-Custom-Header", "application/json");

		try {
			EntityManager manager = sessionFactory.createEntityManager();
			Optional<User> result = Optional.ofNullable(manager.find(User.class, fromJson(input.getPathParameters().get("id"), Long.class)));
			manager.close();
			if (!result.isPresent()) {
				throw new NoUserFoundException(Long.parseLong(input.getPathParameters().get("id")));
			}
			

			return new APIGatewayProxyResponseEvent()
					.withHeaders(headers)
					.withStatusCode(200)
					.withBody(toJson(result));
		} finally {
			flushConnectionPool();
		}
	}

	private static <T> String toJson(T object) {
		try {
			return OBJECT_MAPPER.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	private static <T> T fromJson(String json, Class<T> type) {
		try {
			return OBJECT_MAPPER.readValue(json, type);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	private void flushConnectionPool() {
		ConnectionProvider connectionProvider = sessionFactory.getSessionFactoryOptions().getServiceRegistry()
				.getService(ConnectionProvider.class);
		HikariDataSource hikariDataSource = connectionProvider.unwrap(HikariDataSource.class);
		hikariDataSource.getHikariPoolMXBean().softEvictConnections();
	}
}
