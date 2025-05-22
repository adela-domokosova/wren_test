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
 * Copyright 2016-2017 ForgeRock AS.
 */
package org.forgerock.security.keystore;


import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;
import java.util.Map;

import org.forgerock.opendj.security.OpenDJProvider;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.DestroyFailedException;


/**
 * Test the KeyStoreConfigurationManager API
 */
public class KeyStoreConfigurationManagerTest {
    // for testing dynamic class loading
    static final String OPENDJ_CLASSNAME = OpenDJProvider.class.getName();

    @DataProvider
    private Object[][] defaultConfig() {
        return new Object[][]{
                {"default",                     "PKCS12"},
                {"legacy",                      "JKS"},
                {"legacy-jceks",                "JCEKS"},
                {"password-file-with-newline",  "PKCS12"},
        };
    }

    @Test(dataProvider = "defaultConfig")
    public void shouldInitialize(final String configName, final String keyStoreType) throws Exception {
        String fileName = "/keystore_config1.json";

        String dir = getPathPrefix();

        try (final InputStream configFile = getClass().getResourceAsStream(fileName)) {
            assertThat(configFile).isNotNull();
            KeyStoreConfigurationManager cf = new KeyStoreConfigurationManagerImpl(configFile);
            assertThat(cf).isNotNull();
            KeyStoreConfiguration ksc = cf.getKeyStoreConfiguration(configName);
            assertThat(ksc).isNotNull();
            assertThat(ksc.getKeyStoreType()).isEqualTo(keyStoreType);

            KeyStore keyStore = ksc.loadKeyStore(dir);

            final KeyStoreManager keyStoreManager = new KeyStoreManager(keyStore);

            // when
            final X509Certificate certificate = keyStoreManager.getX509Certificate("key");

            // then
            assertThat(certificate).isNotNull();
        }
    }

    /**
     * Test using OpenDJ has a Keystore. This exercises dynamic loading of the OpenDJ KeyStore provider
     * <p>
     * This is an integration test that requires OpenDJ to be running, and configured
     * with an ou=keystore,dc=example,dc=com.   See the DJ parameters in resources/keystore.conf.
     * <p>
     * Because it is an integration test, it has been disabled by default
     *
     * @throws Exception
     */
    @Test(enabled = false)
    public void shouldLoadOpenDJProvider() throws Exception {
        String configFile = "/keystore_opendj.json";

        final InputStream in = getClass().getResourceAsStream(configFile);
        assertThat(in).isNotNull();
        String dir = getPathPrefix();
        KeyStoreConfigurationManager cf = new KeyStoreConfigurationManagerImpl(in);
        KeyStoreConfiguration ksc = cf.getKeyStoreConfiguration("default");
        KeyStore keyStore = ksc.loadKeyStore(dir);

        assertThat(keyStore.containsAlias("not-there")).isFalse();

        setSecretKeyEntry(keyStore, "foo", "secret-password");
    }

    // Test dynamic class loading.
    @Test()
    public void shouldNotThrowExceptionForDefaultDynamicClassLoader() throws  Exception {
        KeyStoreBuilder builder = new KeyStoreBuilder();
        builder = builder.withProviderClass(OPENDJ_CLASSNAME);
    }


    // Test dynamic class loading using the current context loader
    @Test()
    public void shouldNotThrowExceptionForDynamicClassLoader() throws  Exception {
        KeyStoreBuilder builder = new KeyStoreBuilder();
        builder = builder.withProviderClass(OPENDJ_CLASSNAME, Thread.currentThread().getContextClassLoader());
    }

    @Test()
    public void testMapParameters() throws Exception {
        KeyStoreConfiguration ksc = getKeyStoreConfigWithMapParams("/keystore_opendj_with_map_params.json");

        Map<String, Object> p = ksc.getParameters();
        assertThat(p).isNotNull();
        assertThat(p.get("org.forgerock.opendj.security.port")).isEqualTo(1389);
    }

    // enable this integration test once https://bugster.forgerock.org/jira/browse/OPENDJ-3790 is complete
    @Test(enabled = false)
    public void testOpenDJwithMapParameters() throws Exception {
        KeyStoreConfiguration ksc = getKeyStoreConfigWithMapParams("/keystore_opendj_with_map_params.json");

        Map<String, Object> p = ksc.getParameters();
        assertThat(p).isNotNull();
        assertThat(p.get("org.forgerock.opendj.security.port")).isEqualTo(1389);
        String dir = getPathPrefix();

        KeyStore keyStore = ksc.loadKeyStore(dir);
    }

    private KeyStoreConfiguration getKeyStoreConfigWithMapParams(String configFile) throws Exception {
        final InputStream in = getClass().getResourceAsStream(configFile);
        assertThat(in).isNotNull();
        String dir = getPathPrefix();
        KeyStoreConfigurationManager cf = new KeyStoreConfigurationManagerImpl(in);
        KeyStoreConfiguration ksc = cf.getKeyStoreConfiguration("default");
        return ksc;
    }


    // test method to add a secret to the keystore
    private void setSecretKeyEntry(KeyStore ks, String alias, String password) throws KeyStoreException {
        SecretKeySpec keyspec = new SecretKeySpec(password.getBytes(UTF_8), "RAW");
        KeyStore.PasswordProtection keyStorePP = new KeyStore.PasswordProtection("changeit".toCharArray());

        try {
            if (ks.containsAlias(alias)) {
                ks.deleteEntry(alias);
            }
            KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(keyspec);
            ks.setEntry(alias, entry, keyStorePP);
        } finally {
            try {
                keyStorePP.destroy();
            } catch (DestroyFailedException e) {
                // It's OK to swallow this  - means that we could not wipe the password, but this is never
                // guaranteed to work anyway.
            }
        }
    }

    private static String pathPrefix;

    /**
     * Calculate the parent directory of the keystore / storepass files
     * This is needed to get the absolute path to the test configuration files.
     *
     * @return
     */
    private String getPathPrefix() throws URISyntaxException {
        if (pathPrefix != null) {
            return pathPrefix;
        }

        String fileName = "/keystore_config1.json";

        File f = Paths.get(getClass().getResource(fileName).toURI()).toFile();
        pathPrefix = f.getParent();
        return pathPrefix;
    }
}
