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
package org.forgerock.security.keystore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements a {@link KeyStoreConfigurationManager} using JSON serialization. The typical
 * usage will be to open a JSON file in conf/keystore.json that contains the configurations
 * for the various keystores the application may use. For example:
 * <pre>
 *
 * {
 *    "default": {
 *      "comment": "Additional fields should be ignored. This is the default keystore implementation",
 *      "keyStoreFile": "keystore.pfx",
 *      "keyStorePasswordFile": "storepass",
 *      "keyStoreType": "PKCS12"
 *    },
 *    "legacy": {
 *      "comment": "Some legacy keystore",
 *      "keyStoreFile": "keystore.jks",
 *      "keyStorePasswordFile": "storepass",
 *      "keyStoreType": "JKS"
 *    },
 *    "opendj": {
 *        "comment": "opendj keystore",
 *         "providerName": "OpenDJ",
 *         "providerArg": "src/test/resources/keystore.conf",
 *         "keyStoreType": "LDAP",
 *         "providerClass": "org.forgerock.opendj.security.OpenDJProvider",
 *         "keyStorePasswordFile": "dj_storepass",
 *         "keyPasswordFile": "dj_storepass"
 *     }
 * }
 * </pre>
 * <p>
 * {@inheritDoc}
 */
public class KeyStoreConfigurationManagerImpl implements KeyStoreConfigurationManager {
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final Map<String, KeyStoreConfiguration> keystores;
    private final static MapType TYPE_REFERENCE = OBJECT_MAPPER.getTypeFactory()
            .constructMapType(HashMap.class, String.class, KeyStoreConfiguration.class);

    /**
     * Construct a KeyStoreConfigurationManager instance.
     *
     * @param fileName JSON file path that holds the keystore configuration
     * @throws IOException If the configuration JSON can not be read
     */
    public KeyStoreConfigurationManagerImpl(final String fileName) throws IOException {
        keystores = OBJECT_MAPPER.readValue(fileName, TYPE_REFERENCE);
    }

    /**
     * Construct a KeyStoreConfigurationManager instance.
     *
     * @param jsonStream InputStream to an json object to be parsed.
     * @throws IOException If the configuration JSON can not be read
     */
    public KeyStoreConfigurationManagerImpl(final InputStream jsonStream) throws IOException {
        keystores =  OBJECT_MAPPER.readValue(jsonStream, TYPE_REFERENCE);
    }

    @Override
    /**
     * {@inheritDoc}
     */
    public KeyStoreConfiguration getKeyStoreConfiguration(final String name) {
        return keystores.get(name);
    }

}
