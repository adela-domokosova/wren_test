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
 * information: "Portions Copyrighted [year] [name of copyright owner]".
 *
 * Copyright 2013-2017 ForgeRock AS.
 */

package org.forgerock.json.jose.jwk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.forgerock.json.JsonException;
import org.forgerock.json.JsonValue;
import org.forgerock.json.jose.jwt.Algorithm;
import org.forgerock.json.jose.jwt.JWObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Holds a Set of JWKs.
 */
public class JWKSet extends JWObject {

    private static final Logger logger = LoggerFactory.getLogger(JWKSet.class);

    /**
     * Constructs an empty JWKSet.
     */
    public JWKSet() {
        put("keys", Collections.EMPTY_LIST);
    }

    /**
     * Construct a JWKSet from a single JWK.
     * @param jwk the jwk to construct the set from
     */
    public JWKSet(JWK jwk) {
        if (jwk == null) {
            throw new JsonException("JWK must not be null");
        }
        put("keys", Collections.singletonList(jwk.toJsonValue().asMap()));
    }

    /**
     * Construct a JWKSet from a single JWK.
     * @param jwks contains a list of json web keys
     */
    public JWKSet(JsonValue jwks) {
        if (jwks == null) {
            throw new JsonException("JWK set must not be null");
        }
        put("keys", jwks.expect(List.class));
    }

    /**
     * Construct a JWKSet from a List of JWKs.
     * @param jwkList a list of jwks
     */
    public JWKSet(List<JWK> jwkList) {
        if (jwkList == null) {
            throw new JsonException("The list cannot be null");
        }
        //transform to json, as it's our current way of storing jwks
        List<Map<String, Object>> jwkListAsJson = new ArrayList<>();
        for (JWK jwk : jwkList) {
            jwkListAsJson.add(jwk.toJsonValue().asMap());
        }
        put("keys", jwkListAsJson);
    }

    /**
     * Get the JWKs in the set.
     * @return a list of JWKs
     */
    public List<JWK> getJWKsAsList() {
        List<JWK> listOfJWKs = new LinkedList<>();
        JsonValue jwks = get("keys");
        Iterator<JsonValue> i = jwks.iterator();
        while (i.hasNext()) {
            listOfJWKs.add(JWK.parse(i.next()));
        }
        return listOfJWKs;
    }

    /**
     * Get the JWKs in the set.
     * @return a list of JWKs as JsonValues
     */
    public JsonValue getJWKsAsJsonValue() {
        return get("keys");
    }

    /**
     * Converts a json string to a jsonValue.
     * @param json a json jwk set object string
     * @return a json value of the son string
     * @throws JsonException if unable to parse
     */
    protected static JsonValue toJsonValue(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return new JsonValue(mapper.readValue(json, Map.class));
        } catch (IOException e) {
            throw new JsonException("Failed to parse json", e);
        }
    }

    /**
     * Parses a JWKSet object from a string json object.
     * @param json string json object
     * @return a JWKSet
     */
    public static JWKSet parse(String json) {
        JsonValue jwkSet = new JsonValue(toJsonValue(json));
        return parse(jwkSet);
    }

    /**
     * Parses a JWKSet object from a jsonValue object.
     * @param json an JsonValue object
     * @return a JWKSet
     */
    public static JWKSet parse(JsonValue json) {
        if (json == null) {
            throw new JsonException("Cant parse JWKSet. No json data.");
        }
        return new JWKSet(json.get("keys"));
    }

    /**
     * Prints the JWK Set as a json string.
     * @return A String representing JWK
     */
    public String toJsonString() {
        return super.toString();
    }

    /**
     * Search for a JWK that matches the algorithm and the key usage.
     *
     * @param algorithm the algorithm needed
     * @param keyUse the key usage. If null, only the algorithm will be used as a search criteria.
     * @return A jwk that matches the search criteria. If no JWK found for the key usage, then it searches for a JWK
     * without key usage defined. If still no JWK found, then returns null.
     */
    public JWK findJwk(Algorithm algorithm, KeyUse keyUse) {
        //First, we try to find a JWK that matches the keyUse
        for (JWK jwk : getJWKsAsList()) {
            try {
                if (algorithm.getJwaAlgorithmName().equalsIgnoreCase(jwk.getAlgorithm()) && (keyUse == jwk.getUse())) {
                    return jwk;
                }
            } catch (IllegalArgumentException e) {
                // We raise a warning as the JWKs could be the client one, with some non-compliant JWK.
                logger.warn("Can't load JWK with kid'" + jwk.getKeyId() + "'", e);
            }
        }

        //At this point, no jwk was found. We can try to find a JWK without a keyUse now
        return keyUse != null ? findJwk(algorithm, null) :  null;
    }

    /**
     * Search for a JWK that matches the kid.
     *
     * @param kid Key ID
     * @return A jwk that matches the kid. If no JWK found, returns null
     */
    public JWK findJwk(String kid) {
        for (JWK jwk : getJWKsAsList()) {
            if (kid.equals(jwk.getKeyId())) {
                return jwk;
            }
        }
        return null;
    }
}
