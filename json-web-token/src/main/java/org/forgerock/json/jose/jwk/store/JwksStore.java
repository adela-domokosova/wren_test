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
package org.forgerock.json.jose.jwk.store;


import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.forgerock.json.jose.exceptions.FailedToLoadJWKException;
import org.forgerock.json.jose.jwk.JWK;
import org.forgerock.json.jose.jwk.JWKSet;
import org.forgerock.json.jose.jwk.JWKSetParser;
import org.forgerock.json.jose.jwk.KeyUse;
import org.forgerock.json.jose.jwt.Algorithm;
import org.forgerock.util.Reject;
import org.forgerock.util.SimpleHTTPClient;
import org.forgerock.util.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Store JWKs into a jwkSet from a JWKs_URI and refresh the jwkSet when necessary. */
public class JwksStore {
    private static final Logger logger = LoggerFactory.getLogger(JwksStore.class);

    private final String uid;
    private final JWKSetParser jwkParser;

    /** To prevent attackers reloading the cache too often. */
    private long cacheMissCacheTimeInMs;
    private long cacheTimeoutInMs;
    private URL jwkUrl;

    private JWKSet jwksSet;
    private long lastReloadJwksSet;

    /**
     * Create a new JWKs store.
     *
     * @param uid the unique identifier for this store
     * @param cacheTimeout a cache timeout to avoid reloading the cache all the time when doing encryption
     * @param cacheMissCacheTime the cache time before reload the cache in case of a cache miss.
     *                           This avoid polling the client application too often.
     * @param jwkUrl the jwk url of the JWKs hosted by the client application
     * @param httpClient The http client through which we will attempt to read the jwkUrl
     * @throws FailedToLoadJWKException if the jwks can't be reloaded.
     */
    JwksStore(final String uid, final Duration cacheTimeout, final Duration cacheMissCacheTime,
            final URL jwkUrl, final SimpleHTTPClient httpClient) throws FailedToLoadJWKException {
        this(uid, cacheTimeout, cacheMissCacheTime, jwkUrl, new JWKSetParser(httpClient));
    }

    /**
     * Create a new JWKs store.
     *
     * @param uid the unique identifier for this store
     * @param cacheTimeout a cache timeout to avoid reloading the cache all the time when doing encryption
     * @param cacheMissCacheTime the cache time before reload the cache in case of a cache miss.
     *                           This avoid polling the client application too often.
     * @param jwkUrl the jwk url  of the JWKs hosted by the client application
     * @param jwkSetParser the jwks set parser
     * @throws FailedToLoadJWKException if the jwks can't be reloaded.
     */
    JwksStore(final String uid, final Duration cacheTimeout, final Duration cacheMissCacheTime,
            final URL jwkUrl, JWKSetParser jwkSetParser) throws FailedToLoadJWKException {
        this.uid = uid;
        this.cacheTimeoutInMs = cacheTimeout.to(TimeUnit.MILLISECONDS);
        this.cacheMissCacheTimeInMs = cacheMissCacheTime.to(TimeUnit.MILLISECONDS);
        this.jwkUrl = jwkUrl;
        this.jwkParser = jwkSetParser;

        try {
            reloadJwks();
        } catch (FailedToLoadJWKException e) {
            logger.debug("Unable to load keys from the JWK over HTTP");
            throw new FailedToLoadJWKException("Unable to load keys from the JWK over HTTP", e);
        }
    }

    /**
     * Communicates with the configured server, attempting to download the latest JWKs for use.
     *
     * @throws FailedToLoadJWKException if there were issues parsing the supplied URL
     */
    private synchronized void reloadJwks() throws FailedToLoadJWKException {
        jwksSet = jwkParser.jwkSet(jwkUrl);
        lastReloadJwksSet = System.currentTimeMillis();
    }

    /**
     * Search for a JWK that matches the algorithm and the key usage.
     *
     * @param algorithm the algorithm needed
     * @param keyUse the key usage. If null, only the algorithm will be used as a search criteria.
     * @return A jwk that matches the search criteria. If no JWK found for the key usage, then it searches for a JWK
     * without key usage defined. If still no JWK found, then returns null.
     * @throws FailedToLoadJWKException if the jwks can't be reloaded.
     */
    public JWK findJwk(Algorithm algorithm, KeyUse keyUse) throws FailedToLoadJWKException {
        if (keyUse == KeyUse.ENC && hasJwksCacheTimedOut()) {
            reloadJwks();
        }

        JWK jwk = jwksSet.findJwk(algorithm, keyUse);
        if (jwk == null && isCacheMissCacheTimeExpired()) {
            reloadJwks();
            return jwksSet.findJwk(algorithm, keyUse);
        }
        return jwk;
    }

    /**
     * Search for a JWK that matches the kid.
     *
     * @param kid Key ID
     * @return A jwk that matches the kid. If no JWK found, returns null
     * @throws FailedToLoadJWKException if the jwks can't be reloaded.
     */
    public JWK findJwk(String kid) throws FailedToLoadJWKException {
        JWK jwk = jwksSet.findJwk(kid);
        if (jwk == null && isCacheMissCacheTimeExpired()) {
            reloadJwks();
            return jwksSet.findJwk(kid);
        }
        return jwk;
    }

    /**
     * Get the UID.
     * @return the uid.
     */
    public String getUid() {
        return uid;
    }

    /**
     * Get the cache timeout.
     * @return the cache timeout.
     */
    public Duration getCacheTimeout() {
        return Duration.duration(cacheTimeoutInMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Get the cache time before reload the cache in case of cache miss.
     * @return the cache miss cache time.
     */
    public Duration getCacheMissCacheTime() {
        return Duration.duration(cacheMissCacheTimeInMs, TimeUnit.MILLISECONDS);
    }

    /**
     * The JWKs URI.
     * @return the jwk uri.
     */
    public URL getJwkUrl() {
        return jwkUrl;
    }

    /**
     * Update the cache timeout.
     * @param cacheTimeout the cache timeout.
     */
    public void setCacheTimeout(Duration cacheTimeout) {
        this.cacheTimeoutInMs = cacheTimeout.to(TimeUnit.MILLISECONDS);
    }

    /**
     * Update the cache time before reload the cache in case of cache miss.
     * @param cacheMissCacheTime the cache miss cache time.
     */
    public void setCacheMissCacheTime(Duration cacheMissCacheTime) {
        this.cacheMissCacheTimeInMs = cacheMissCacheTime.to(TimeUnit.MILLISECONDS);
    }

    /**
     * Update the JWKs URI.
     * @param jwkUrl the jwks uri.
     * @throws FailedToLoadJWKException If the URI has changed and the JWK set cannot be loaded.
     */
    public void setJwkUrl(URL jwkUrl) throws FailedToLoadJWKException {
        Reject.ifNull(jwkUrl);
        URL originalJwkUrl = this.jwkUrl;
        this.jwkUrl = jwkUrl;
        if (!jwkUrl.equals(originalJwkUrl)) {
            reloadJwks();
        }
    }

    private boolean hasJwksCacheTimedOut() {
        return (System.currentTimeMillis() - lastReloadJwksSet) > cacheTimeoutInMs;
    }

    /**
     * When we have a cache miss, we don't refresh the cache straight away. We check first if the cache miss cache
     * time is expired out or not
     * @return true is we  can reload the cache
     */
    private boolean isCacheMissCacheTimeExpired() {
        return (System.currentTimeMillis() - lastReloadJwksSet) >= cacheMissCacheTimeInMs;
    }
}
