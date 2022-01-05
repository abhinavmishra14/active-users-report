/*
 * Created By: Abhinav Kumar Mishra
 * Copyright &copy; 2022. Abhinav Kumar Mishra. 
 * All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.abhinavmishra14.audit.webscript;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.TicketComponent;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class GetActiveUsersWebscript.
 */
public class GetActiveUsersWebscript extends DeclarativeWebScript {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(GetActiveUsersWebscript.class);

	/** The Constant ACTIVE_USERS. */
	private static final String ACTIVE_USERS = "activeUsers";

	/** The Constant ACTIVE_USERS_COUNT. */
	private static final String ACTIVE_USERS_COUNT = "activeUsersCount";

	/** The Constant ACTIVE_TICKETS_COUNT. */
	private static final String ACTIVE_TICKETS_COUNT = "activeTicketsCount";

	/** The Constant COMMENT_DATA. */
	private static final String COMMENT_DATA = "_comment";

	/** The Constant RESPONSE. */
	private static final String RESPONSE = "response";

	/** The ticket component. */
	private final TicketComponent ticketComponent;

	/**
	 * The Constructor.
	 *
	 * @param ticketComponent the ticket component
	 */
	public GetActiveUsersWebscript(final TicketComponent ticketComponent) {
		super();
		this.ticketComponent = ticketComponent;
	}

	/* (non-Javadoc)
	 * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.Status, org.springframework.extensions.webscripts.Cache)
	 */
	@Override
	public Map<String, Object> executeImpl(final WebScriptRequest req,
			final Status status, final Cache cache) {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Extracting active users..");
		}
		final Map<String, Object> model = new ConcurrentHashMap<String, Object>(3);
		try {
			//get nonExpiredOnly users with tickets
			final Set<String> activeUsers = ticketComponent.getUsersWithTickets(true);
			final ObjectMapper objMapper = new ObjectMapper();
			objMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

			if (activeUsers != null && !activeUsers.isEmpty()) {
				final JSONObject activeUsersJson = new JSONObject();
				//This may be lower than the ticket count, since a user can have more than one
				// ticket/session
				activeUsersJson.put(ACTIVE_USERS, objMapper.writeValueAsString(activeUsers));
				activeUsersJson.put(ACTIVE_USERS_COUNT, activeUsers.size());

				//This may be higher than the user count, since a user can have more than one   
				// ticket/session
				//get nonExpiredOnly ticket count
				activeUsersJson.put(ACTIVE_TICKETS_COUNT, ticketComponent.countTickets(true));

				activeUsersJson.put(COMMENT_DATA, "Active user count may be lower than the ticket count, since a user can have more than one ticket/session. Ticket count may be higher than the active user count, since a user can have more than one ticket/session.");
				model.put(RESPONSE, activeUsersJson);
			}
		} catch (JsonProcessingException | JSONException excp) {
			LOGGER.error("Exception occurred while preparing json for active users ", excp);
			throw new WebScriptException(
					Status.STATUS_INTERNAL_SERVER_ERROR, excp.getMessage(), excp);
		} catch (AlfrescoRuntimeException alfErr) {
			LOGGER.error("Unexpected error occurred while getting active users ", alfErr);
			throw new WebScriptException(
					Status.STATUS_INTERNAL_SERVER_ERROR, alfErr.getMessage(), alfErr);
		}
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Extracted active users.");
		}
		return model;
	}
}
