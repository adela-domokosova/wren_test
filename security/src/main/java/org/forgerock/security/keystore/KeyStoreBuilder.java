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

import static org.forgerock.util.Reject.checkNotNull;
import static org.forgerock.util.Utils.closeSilently;
import static org.forgerock.util.Utils.isBlank;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertificateException;
import java.util.Locale;

import org.forgerock.util.Reject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder class for loading key stores.
 */
public final class KeyStoreBuilder {
    private static final Logger logger = LoggerFactory.getLogger(KeyStoreBuilder.class);
    private static final String NONE = "none";
    private String type = "JKS";
    private KeyStore.LoadStoreParameter loadStoreParameter;
    private InputStream inputStream;
    private Provider provider;
    private char[] password;
    private Class<?> providerClass;
    private String providerArg;

    /**
     * Specifies the input stream to load the keystore from. Defaults to {@code null} to create a fresh keystore.
     * <p>
     * Note: the input stream will be closed automatically after the keystore is loaded.
     *
     * @param inputStream the input stream to load the keystore from.
     * @return the same builder instance.
     */
    public KeyStoreBuilder withInputStream(final InputStream inputStream) {
        this.inputStream = inputStream;
        return this;
    }

    /**
     * Specifies the file to load the keystore from.
     *
     * @param keyStoreFile the keystore file to load.
     * @return the same builder instance.
     * @throws FileNotFoundException if the file does not exist, is not a file, or cannot be read.
     */
    public KeyStoreBuilder withKeyStoreFile(final File keyStoreFile) throws FileNotFoundException {
        Reject.ifNull(keyStoreFile);
        return withInputStream(new FileInputStream(keyStoreFile));
    }

    /**
     * Specifies the file to load the keystore from. If the file name is "NONE" (case-insensitive), empty, or null
     * the keystore will be loaded with a null {@link InputStream}.
     *
     * @param keyStoreFile the name of keystore file to load.
     * @return the same builder instance.
     * @throws FileNotFoundException if the file does not exist, is not a file, or cannot be read.
     */
    public KeyStoreBuilder withKeyStoreFile(final String keyStoreFile) throws FileNotFoundException {
        if (isBlank(keyStoreFile) || NONE.equals(keyStoreFile.toLowerCase(Locale.ROOT))) {
            return withInputStream(null);
        } else {
            return withInputStream(new FileInputStream(keyStoreFile));
        }
    }

    /**
     * Specifies the type of keystore to load. Defaults to JKS.
     *
     * @deprecated Use withKeyStoreType(String) instead.
     *
     * Use of the KeyStoreType enum is deprecated as it restricts the keystore type to those specified in the
     * enum. Library consumers may want to specify the keystore type at runtime.
     *
     * @param type the type of keystore to load. May not be null.
     * @return the same builder instance.
     */
    @Deprecated()
    public KeyStoreBuilder withKeyStoreType(final KeyStoreType type) {
        this.type = checkNotNull(type).toString();
        return this;
    }

    /**
     * Specifies the type of keystore to load. Defaults to JKS.
     *
     * @param type the type of keystore to load. May not be null.
     * @return the same builder instance.
     */
    public KeyStoreBuilder withKeyStoreType(final String type) {
        this.type = checkNotNull(type);
        return this;
    }

    /**
     * Specifies the password to unlock the keystore. Defaults to no password. The password will be cleared after the
     * keystore has been loaded.
     *
     * @param password the password to unlock the keystore.
     * @return the same builder instance.
     */
    public KeyStoreBuilder withPassword(final char[] password) {
        this.password = password;
        return this;
    }

    /**
     * Specifies the password to unlock the keystore.
     *
     * @param password the password to use. May not be null.
     * @return the same builder instance.
     * @see #withPassword(char[])
     */
    public KeyStoreBuilder withPassword(final String password) {
        return withPassword(password.toCharArray());
    }

    /**
     * Specifies the security provider to use for the keystore.
     *
     * @param provider the security provider. May not be null.
     * @return the same builder instance.
     */
    public KeyStoreBuilder withProvider(final Provider provider) {
        this.provider = checkNotNull(provider);
        return this;
    }

    /**
     * Specifies the security provider to use for the keystore.
     *
     * @param providerName the name of the provider to use.
     * @return the same builder instance.
     * @throws IllegalArgumentException if no such provider exists.
     */
    public KeyStoreBuilder withProvider(final String providerName) {
        if (isBlank(providerName)) {
            return this;
        }
        Provider provider = Security.getProvider(providerName);
        if (provider == null) {
            throw new IllegalArgumentException("No such provider: " + providerName);
        }
        return withProvider(provider);
    }

    /**
     * Specifies the {@link KeyStore.LoadStoreParameter} to use to load the {@link KeyStore}.
     *
     * @param loadStoreParameter the {@link KeyStore.LoadStoreParameter}.
     * @return the same builder instance.
     */
    public KeyStoreBuilder withLoadStoreParameter(final KeyStore.LoadStoreParameter loadStoreParameter) {
        Reject.ifNull(loadStoreParameter);
        this.loadStoreParameter = loadStoreParameter;
        return this;
    }

    /**
     * Specifies the java class name of a keystore provider. The class will be loaded via reflection
     * using the default class loader.
     *
     * @param className Java class name of a KeyStoreProvider - specififed as a string
     * @return the same builder instance.
     */
    public KeyStoreBuilder withProviderClass(final String className)  {
        return withProviderClass(className, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Specifies the java class name of a keystore provider. The class will be loaded via reflection
     * using the supplied Class Loader
     *
     * @param className Java class name of a KeyStoreProvider - specififed as a string
     * @param classLoader - The Java Class Loader to use.
     * @return the same builder instance.
     */
    public KeyStoreBuilder withProviderClass(final String className, final ClassLoader classLoader)  {
        try {
            providerClass  = Class.forName(className, true, classLoader);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Can not dynamically load new keystore class " + className, e);
        }
        return this;
    }


    /**
     * Specifies the argument to the Java Keystore Provider. This is used when loading the provider
     * through reflection. The interpretation of the argument is specific to the KeyStore Provider.
     *
     * @param arg The string argument to the provider.
     * @return the same builder instance.
     */
    public KeyStoreBuilder withProviderArgument(String arg) {
        this.providerArg = checkNotNull(arg);
        return this;
    }


    /**
     * Builds and loads the keystore using the provided parameters. If a password was provided, then it is blanked
     * after the keystore has been loaded.
     *
     * @return the configured keystore.
     */
    public KeyStore build() {
        try {
            if (providerClass != null) {
                this.provider = loadClass();
            }

            final KeyStore keyStore = provider != null
                    ? KeyStore.getInstance(type, provider)
                    : KeyStore.getInstance(type);

            if (inputStream != null && loadStoreParameter != null) {
                throw new IllegalStateException("Can not specify a load store parameter and an input stream");
            } else if (loadStoreParameter != null) {
                keyStore.load(loadStoreParameter);
            } else if (inputStream == null) { // this works for PKCS11 and LDAP
                keyStore.load(null, password);
            } else {
                keyStore.load(inputStream, password);
            }
            return keyStore;
        } catch (CertificateException | NoSuchAlgorithmException | IOException | KeyStoreException e) {
            logger.error("Error loading keystore", e);
            throw new IllegalStateException("Unable to load keystore", e);
        } finally {
            closeSilently(inputStream);
        }
    }

    // Dynamically loads the keystore provider class
    // This assume the provider constructor takes a single String argument, which can be null
    // This is the case for the known dynamic keystore providers
    private Provider loadClass() {
        try {
            Constructor<?> ctor = providerClass.getConstructor(String.class);
            Provider provider = (Provider) ctor.newInstance(providerArg);
            // This is not required unless you want other classes to be able to load the provider
            // by the type name. This builder class explicitly loads the provider
            // The insertProvider call may fail if the Java Security provider blocks it
            // This is left here for documentation purposes.
            //Security.insertProviderAt(provider, 1);
            return provider;
        } catch (Exception e) { // there a bunch of reasons reflection can fail. None can be fixed
            throw new IllegalStateException("Can not load provider class using reflection", e);
        }
    }
}
