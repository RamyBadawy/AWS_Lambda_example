package com.gaggle.lambda;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.SessionFactory;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaggle.config.AbstractHandler;
import com.gaggle.config.HibernateUtil;
import com.gaggle.config.SpringConfig;
import com.gaggle.exceptions.NoUserFoundException;
import com.gaggle.model.User;
import com.gaggle.service.UserServiceImpl;
import com.zaxxer.hikari.HikariDataSource;


// Approach 1
// extend AbstractHandler<SpringConfig>
// SpringConfig is a spring configuration class that has @ComponentScan for the base package
public class GetUserByNameApproach1 extends AbstractHandler<SpringConfig>
		implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type", "application/json");
		headers.put("X-Custom-Header", "application/json");

		try {
			// approach 1 - should be commented out if we use Approach 2
			UserServiceImpl userService = getApplicationContext().getBean(UserServiceImpl.class);

			List<User> users = userService
					.getUserByNameContains(fromJson(input.getPathParameters().get("search"), String.class));

			if (users.isEmpty()) {
				throw new NoUserFoundException(input.getPathParameters().get("search"));
			}

			context.getLogger().log(String.format("returning %d users", users.size()));

			return new APIGatewayProxyResponseEvent().withHeaders(headers).withStatusCode(200).withBody(toJson(users));
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
