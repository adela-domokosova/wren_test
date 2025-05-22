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

import java.security.KeyStore;
import java.util.Map;

/**
 * Configures a keystore based on a key/value map.
 *
 * @see  KeyStoreBuilder#withLoadStoreParameter(KeyStore.LoadStoreParameter)
 *
 */
public class MapKeyStoreParameters implements KeyStore.LoadStoreParameter {
    private final Map<String, Object> properties;

    /**
     * Create a MapKeyStoreParameters based on the supplied properties.
     *
     * @param properties Key/Value map of properties that will be used to initalize the keystore provider
     */
    public MapKeyStoreParameters(final Map<String, Object> properties) {
        this.properties = properties;
    }

    /**
     * Return the properties used to configure the keystore.
     *
     * @return the properties used to configure the keystore.
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * We do not expect this method to be called.
     *
     * @throws IllegalStateException if this method is called.
     */
    @Override
    public KeyStore.ProtectionParameter getProtectionParameter() {
        throw new IllegalStateException("getProtectionParameter() is not supported for this class");
    }
}
