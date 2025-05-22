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


/**
 * Defines an interface to obtain KeyStoreConfiguration objects. The idea
 * is to insulate applications from the specific details of how a keystore
 * configuration is obtained, what type of keystore it is, etc.. Applications ask for a keystore by name:
 *
 * <pre>
 *     KeyStoreConfigurationManager configManager;
 *
 *     KeyStoreConfiguration ksc = configManager.getKeyStoreConfiguration("default");
 *     KeyStore keyStore = ksc.loadKeyStore();
 *     // use keystore
 * </pre>
 *
 * @see KeyStoreConfigurationManagerImpl
 */
public interface KeyStoreConfigurationManager {

    /**
     * Return the KeyStore configuration for the given symbolic name.
     * @param name The name of the keystore configuration. example: default, saml2, hsm, test-keystore
     * @return KeyStoreConfiguration for this name, or null if no such configuration exists
     */
    KeyStoreConfiguration getKeyStoreConfiguration(String name);


}
