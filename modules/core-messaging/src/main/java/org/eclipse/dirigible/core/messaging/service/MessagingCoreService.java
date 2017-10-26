package org.eclipse.dirigible.core.messaging.service;

import static java.text.MessageFormat.format;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;

import org.eclipse.dirigible.api.v3.security.UserFacade;
import org.eclipse.dirigible.commons.api.helpers.GsonHelper;
import org.eclipse.dirigible.core.messaging.api.IMessagingCoreService;
import org.eclipse.dirigible.core.messaging.api.DestinationType;
import org.eclipse.dirigible.core.messaging.api.MessagingException;
import org.eclipse.dirigible.core.messaging.definition.ListenerDefinition;
import org.eclipse.dirigible.database.persistence.PersistenceManager;
import org.eclipse.dirigible.database.sql.SqlFactory;

@Singleton
public class MessagingCoreService implements IMessagingCoreService {

	@Inject
	private DataSource dataSource;

	@Inject
	private PersistenceManager<ListenerDefinition> listenerPersistenceManager;

	// Listener

	@Override
	public ListenerDefinition createListener(String location, String name, DestinationType type, String module, String description)
			throws MessagingException {
		ListenerDefinition listenerDefinition = new ListenerDefinition();
		listenerDefinition.setLocation(location);
		listenerDefinition.setName(name);
		listenerDefinition.setType(new Integer(type.ordinal()).byteValue());
		listenerDefinition.setModule(module);
		listenerDefinition.setDescription(description);
		listenerDefinition.setCreatedBy(UserFacade.getName());
		listenerDefinition.setCreatedAt(new Timestamp(new java.util.Date().getTime()));

		try {
			Connection connection = dataSource.getConnection();
			try {
				listenerPersistenceManager.insert(connection, listenerDefinition);
				return listenerDefinition;
			} finally {
				if (connection != null) {
					connection.close();
				}
			}
		} catch (SQLException e) {
			throw new MessagingException(e);
		}
	}

	@Override
	public ListenerDefinition getListener(String location) throws MessagingException {
		try {
			Connection connection = dataSource.getConnection();
			try {
				return listenerPersistenceManager.find(connection, ListenerDefinition.class, location);
			} finally {
				if (connection != null) {
					connection.close();
				}
			}
		} catch (SQLException e) {
			throw new MessagingException(e);
		}
	}

	@Override
	public ListenerDefinition getListenerByName(String name) throws MessagingException {
		try {
			Connection connection = dataSource.getConnection();
			try {
				String sql = SqlFactory.getNative(connection).select().column("*").from("DIRIGIBLE_LISTENERS").where("LISTENER_NAME = ?").toString();
				List<ListenerDefinition> listenerDefinitions = listenerPersistenceManager.query(connection, ListenerDefinition.class, sql,
						Arrays.asList(name));
				if (listenerDefinitions.isEmpty()) {
					return null;
				}
				if (listenerDefinitions.size() > 1) {
					throw new MessagingException(format("There are more that one Listeners with the same name [{0}] at locations: [{1}] and [{2}].",
							name, listenerDefinitions.get(0).getLocation(), listenerDefinitions.get(1).getLocation()));
				}
				return listenerDefinitions.get(0);
			} finally {
				if (connection != null) {
					connection.close();
				}
			}
		} catch (SQLException e) {
			throw new MessagingException(e);
		}
	}

	@Override
	public void removeListener(String location) throws MessagingException {
		try {
			Connection connection = dataSource.getConnection();
			try {
				listenerPersistenceManager.delete(connection, ListenerDefinition.class, location);
			} finally {
				if (connection != null) {
					connection.close();
				}
			}
		} catch (SQLException e) {
			throw new MessagingException(e);
		}
	}

	@Override
	public void updateListener(String location, String name, DestinationType type, String module, String description) throws MessagingException {
		try {
			Connection connection = dataSource.getConnection();
			try {
				ListenerDefinition listenerDefinition = getListener(location);
				listenerDefinition.setName(name);
				listenerDefinition.setType(new Integer(type.ordinal()).byteValue());
				listenerDefinition.setModule(module);
				listenerDefinition.setDescription(description);
				listenerPersistenceManager.update(connection, listenerDefinition, location);
			} finally {
				if (connection != null) {
					connection.close();
				}
			}
		} catch (SQLException e) {
			throw new MessagingException(e);
		}
	}

	@Override
	public List<ListenerDefinition> getListeners() throws MessagingException {
		try {
			Connection connection = dataSource.getConnection();
			try {
				return listenerPersistenceManager.findAll(connection, ListenerDefinition.class);
			} finally {
				if (connection != null) {
					connection.close();
				}
			}
		} catch (SQLException e) {
			throw new MessagingException(e);
		}
	}

	@Override
	public boolean existsListener(String location) throws MessagingException {
		return getListener(location) != null;
	}

	@Override
	public ListenerDefinition parseListener(String json) {
		return GsonHelper.GSON.fromJson(json, ListenerDefinition.class);
	}

	@Override
	public ListenerDefinition parseListener(byte[] json) {
		return GsonHelper.GSON.fromJson(new InputStreamReader(new ByteArrayInputStream(json), StandardCharsets.UTF_8), ListenerDefinition.class);
	}

	@Override
	public String serializeListener(ListenerDefinition listenerDefinition) {
		return GsonHelper.GSON.toJson(listenerDefinition);
	}

}