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
* Copyright 2017 ForgeRock AS.
*/
package org.forgerock.json.jose.jwk.store;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.forgerock.json.jose.exceptions.FailedToLoadJWKException;
import org.forgerock.util.SimpleHTTPClient;
import org.forgerock.util.time.Duration;

/**
 * Manage the jwks store, to avoid having more than one jwks store for the same JWKs_URI unnecessary.
 */
public class JwksStoreService {

    /** The default cache timeout in ms. */
    public final static Duration JWKS_STORE_DEFAULT_CACHE_TIMEOUT_MS = Duration.duration(1L, TimeUnit.HOURS);

    /** The default cache time before reload the cache in case of cache miss ms. */
    public final static Duration JWKS_STORE_DEFAULT_CACHE_MISS_CACHE_TIME_MS = Duration.duration(1L, TimeUnit.MINUTES);

    private final SimpleHTTPClient simpleHTTPClient;

    private final Map<String, JwksStore> jwksStoreByUID = new HashMap<>();

    /**  Default constructor. */
    public JwksStoreService() {
        this(new SimpleHTTPClient());
    }

    /**
     * Constructor with read and connection timeout. It's used for the connection to the JWKs_URI.
     *
     * @param readTimeout the read timeout
     * @param connTimeout the connection timeout
     */
    public JwksStoreService(int readTimeout, int connTimeout) {
        this(new SimpleHTTPClient(readTimeout, connTimeout));
    }

    /**
     * Constructor with a HTTP client, that will be used to connect to the JWKS_URI.
     *
     * @param simpleHTTPClient the HTTP client
     */
    public JwksStoreService(SimpleHTTPClient simpleHTTPClient) {
        this.simpleHTTPClient = simpleHTTPClient;
    }

    /**
     * Returns the appropriate JWKs store.
     *
     * @param uid Reference to the jwks store. Note that the uid check is case insensitive
     * @return a JWKs Store for the corresponding UID. If doesn't exist, returns null
     */
    public synchronized JwksStore getJwksStore(String uid) {
        return jwksStoreByUID.get(uid.toLowerCase());
    }

    /**
     * Configure a JWKs store.
     *
     * @param uid the unique identifier for this store
     * @param cacheTimeout a cache timeout to avoid reloading the cache all the time when doing encryption
     * @param cacheMissCacheTime the cache time before reload the cache in case of cache miss.
     * @param jwkUrl the jwk url hosted by the client application
     * @return the JWKs store corresponding
     * @throws FailedToLoadJWKException if the jwks can't be reloaded.
     */
    public synchronized JwksStore configureJwksStore(String uid, final Duration cacheTimeout,
            final Duration cacheMissCacheTime, final URL jwkUrl) throws FailedToLoadJWKException {
        uid = uid.toLowerCase();
        JwksStore jwksStore = getJwksStore(uid);
        if (jwksStore != null) {
            jwksStore.setCacheTimeout(cacheTimeout);
            jwksStore.setCacheMissCacheTime(cacheMissCacheTime);
            jwksStore.setJwkUrl(jwkUrl);
            return jwksStore;
        } else {
            jwksStore = new JwksStore(uid, cacheTimeout, cacheMissCacheTime, jwkUrl, simpleHTTPClient);
            jwksStoreByUID.put(uid, jwksStore);
            return jwksStore;
        }
    }

    /**
     * Remove the corresponding jwks store if exist.
     *
     * @param uid the uid. Note that the uid check isn't case sensitive
     */
    public synchronized void removeJwksStore(String uid) {
        jwksStoreByUID.remove(uid.toLowerCase());
    }
}
