/*
* The contents of this file are subject to the terms of the Common Development and
* Distribution License (the License). You may not use this file except in compliance with the
* License.
*
* You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
* specific language governing permission and limitations under the License.
*
* When distributing Covered Software, include this CDDL Header Notice in each file and include
* the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
* Header, with the fields enclosed by brackets [] replaced by your own identifying
* information: "Portions copyright [year] [name of copyright owner]".
*
* Copyright 2014-2017 ForgeRock AS.
*/

package org.forgerock.jaspi.modules.openid.resolvers;

import static org.forgerock.caf.authentication.framework.AuthenticationFramework.LOG;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.forgerock.json.jose.exceptions.FailedToLoadJWKException;
import org.forgerock.util.SimpleHTTPClient;
import org.forgerock.json.jose.jwk.store.JwksStore;
import org.forgerock.json.jose.jwk.store.JwksStoreService;
import org.forgerock.json.JsonValue;
import org.forgerock.json.jose.utils.Utils;

/**
 * This class creates JWKOpenIdResolverImpl's from a supplied
 * well-known open id configuration url.
 */
public class WellKnownOpenIdConfigurationFactory {

    private final static String ISSUER = "issuer";
    private final static String JWKS_URI = "jwks_uri";


    private final SimpleHTTPClient simpleHTTPClient;
    private final JwksStoreService jwksStoreService;

    /**
     * Generates a factory that will use the given timeouts when attempting to
     * read the data form a remote location.
     *
     * @param readTimeout set the read timeout of HTTP operations in this factory
     * @param connTimeout set the connection timeout of HTTP operations in this factory
     */
    public WellKnownOpenIdConfigurationFactory(final int readTimeout, final int connTimeout) {
        this(new SimpleHTTPClient(readTimeout, connTimeout), new JwksStoreService(readTimeout, connTimeout));
    }

    /**
     * For tests.
     * @param simpleHTTPClient A passed-in simple client implementation
     */
    WellKnownOpenIdConfigurationFactory(SimpleHTTPClient simpleHTTPClient) {
        this(simpleHTTPClient, new JwksStoreService(simpleHTTPClient));
    }

    /**
     * For tests.
     * @param jwksStoreService a JwksStore service
     * @param simpleHTTPClient A passed-in simple client implementation
     */
    WellKnownOpenIdConfigurationFactory(SimpleHTTPClient simpleHTTPClient, JwksStoreService jwksStoreService) {
        this.simpleHTTPClient = simpleHTTPClient;
        this.jwksStoreService = jwksStoreService;
    }

    /**
     * Returns a JWKOpenIdResolverImpl representing the contents of the supplied URL.
     *
     * @param configUrl URL from which to read the JWKSet
     * @return a usable JWKOpenIdResolverIMpl
     * @throws FailedToLoadJWKException if there are issues reading or parsing the configUrl
     */
    public JWKOpenIdResolverImpl build(final URL configUrl) throws FailedToLoadJWKException {
        final String configurationContents;

        try {
            configurationContents = simpleHTTPClient.get(configUrl);
        } catch (IOException e) {
            LOG.debug("Unable to load the Configuration at  " + configUrl + " over HTTP", e);
            throw new FailedToLoadJWKException("Unable to load the Configuration over HTTP", e);
        }

        Map<String, Object> parsedJson =  Utils.parseJson(configurationContents);
        final JsonValue configuration = new JsonValue(parsedJson);

        final String issuer = configuration.get(ISSUER).asString();
        final String jwkUri = configuration.get(JWKS_URI).asString();

        if (issuer == null || issuer.isEmpty()) {
            LOG.debug("Invalid configuration - must include an issuer key");
            throw new FailedToLoadJWKException("Invalid configuration - must include an issuer key");
        }

        if (jwkUri == null || jwkUri.isEmpty()) {
            LOG.debug("No JWK URI in the supplied configuration");
            throw new FailedToLoadJWKException("No JWK URI in the supplied configuration");
        }

        URL jwkUrl;

        try {
            jwkUrl = new URL(jwkUri);
        } catch (MalformedURLException e) {
            LOG.debug("Invalid URL supplied to generate JWKs");
            throw new FailedToLoadJWKException("Invalid URL supplied to generate JWKs", e);
        }

        JwksStore jwksStore = jwksStoreService.configureJwksStore(issuer,
                JwksStoreService.JWKS_STORE_DEFAULT_CACHE_TIMEOUT_MS,
                JwksStoreService.JWKS_STORE_DEFAULT_CACHE_MISS_CACHE_TIME_MS, jwkUrl);
        return new JWKOpenIdResolverImpl(jwksStore);
    }

    /**
     * Get the JWKS store service.
     *
     * @return JWKS store service.
     */
    public JwksStoreService getJwksStoreService() {
        return jwksStoreService;
    }
}
