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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;


/**
 * The Class GetActiveUsersWebscriptIT.
 */
public class GetActiveUsersWebscriptIT {

    /** The Constant ACS_ENDPOINT_PROP. */
    private static final String ACS_ENDPOINT_PROP = "acs.endpoint.path";
    
    /** The Constant ACS_DEFAULT_ENDPOINT. */
    private static final String ACS_DEFAULT_ENDPOINT = "http://localhost:8080/alfresco";

    /**
     * Test get active users.
     *
     * @throws Exception the exception
     */
    @Test
    public void testGetActiveUsers() throws Exception {
    	final String webscriptURL = getPlatformEndpoint() + "/service/audit/getActiveUsers";
        // Login credentials for Alfresco Repo
    	final CredentialsProvider provider = new BasicCredentialsProvider();
    	final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("admin", "admin");
        provider.setCredentials(AuthScope.ANY, credentials);

        // Create HTTP Client with credentials
        final CloseableHttpClient httpclient = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider)
                .build();

        // Execute Web Script call
        try {
        	final HttpGet httpget = new HttpGet(webscriptURL);
        	final HttpResponse httpResponse = httpclient.execute(httpget);
            assertEquals("Incorrect HTTP Response Status",
                    HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
            final HttpEntity entity = httpResponse.getEntity();
            assertNotNull("Response from Web Script is null", entity);
        } finally {
            httpclient.close();
        }
    }

    /**
     * Gets the platform endpoint.
     *
     * @return the platform endpoint
     */
    private String getPlatformEndpoint() {
        final String platformEndpoint = System.getProperty(ACS_ENDPOINT_PROP);
        return StringUtils.isNotBlank(platformEndpoint) ? platformEndpoint : ACS_DEFAULT_ENDPOINT;
    }
}