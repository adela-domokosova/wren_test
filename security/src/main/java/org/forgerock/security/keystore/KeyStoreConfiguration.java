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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Holds the configuration required for initializing a Keystore. This includes things
 * like the keystore type (JKS, JCEKS), paths to keystore files, the Keystore provider class, etc.
 * <p>
 * This class is usually de-serialized from json using Jackson. It is immutable once created.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KeyStoreConfiguration {
    private String keyStorePasswordFile;
    private String keyPasswordFile;
    private String keyStoreType;
    private String keyStoreFile;
    private String providerClass;
    private String providerArg;
    private String providerName;
    private Map<String, Object> parameters;

    private static final String[] NEWLINE_TYPES = {
        "\r\n",
        "\n",
    };

    // Private no arg constructor is provided for Jackson
    private KeyStoreConfiguration() {
    }

    /**
     * Create an Immutable KeyStoreConfiguration that holds keystore configuration parameters.
     * Note that depending on the KeyStoreProvider, some or all of these arguments may be
     * optional, in which case you can pass null.
     * <p>
     * Creating instances via json de-serialization is the recommended approach.
     *
     * @param keyStorePasswordFile path name of the .storepass file
     * @param keyPasswordFile      path name of the .keypass file
     * @param keyStoreType         The type of keystore (JKS, JCEKS, etc.)
     * @param keyStoreFile         The path name of the keystore file ( keystore.jceks)
     * @param providerClass        The name of the KeyStoreProvider class (org.acme.CustomKeystoreProvider)
     * @param providerArg          Optional argument used to instantiate a KeyStoreProvider. The interpretation
     *                             is left to the provider
     * @param providerName         The name of the registered keystore provider instance ("LDAP", "JKS", etc.)
     * @param parameters           optional key/value map used to create loadstore parameters for keystore
     *                             initialization
     */
    public KeyStoreConfiguration(String keyStorePasswordFile,
                                 String keyPasswordFile,
                                 String keyStoreType,
                                 String keyStoreFile,
                                 String providerClass,
                                 String providerArg,
                                 String providerName,
                                 Map<String, Object> parameters) {
        this.keyStorePasswordFile = keyStorePasswordFile;
        this.keyPasswordFile = keyPasswordFile;
        this.keyStoreType = keyStoreType;
        this.keyStoreFile = keyStoreFile;
        this.providerClass = providerClass;
        this.providerArg = providerArg;
        this.providerName = providerName;
        this.parameters = parameters;
    }

    /**
     * The provider string name (LDAP, JKS, etc.).
     *
     * @return The provider name as a string
     */
    public String getProviderName() {
        return providerName;
    }

    /**
     * Get the path to the file that contains the password/pin used to unlock the keystore.
     *
     * @return Path to file that holds the password to unlock the keystore
     */
    public String getKeyStorePasswordFile() {
        return keyStorePasswordFile;
    }

    /**
     * Get the path to file that holds the password to unlock individual keys. Frequently not used.
     *
     * @return path to file that contains password to unlock individual key entries
     */
    public String getKeyPasswordFile() {
        return keyPasswordFile;
    }

    /**
     * Get the keystore type.
     *
     * @return the keystore type (JKS, JCEKS , etc. )
     */
    public String getKeyStoreType() {
        return keyStoreType;
    }

    /**
     * Get the path to the keystore.
     *
     * @return The keystore file (example: /tmp/keystore.jceks )
     */
    public String getKeyStoreFile() {
        return keyStoreFile;
    }

    /**
     * Get the provider class name string. This is optional for providers.
     *
     * @return The provider class
     */
    public String getProviderClass() {
        return providerClass;
    }

    /**
     * Get the provider generic argument as a string. The Java keystore SPI provides
     * a single string argument that the provider interprets as it wishes.
     *
     * @return optional provider argument
     */
    public String getProviderArg() {
        return providerArg;
    }

    /**
     * Get the optional parameter map used to initialize a keystore. This is
     * for providers that support a LoadStoreParameter argument.
     *
     * @return parameter map used to configure the keystore.
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * Get the keystore password. This results in the store password file being opened and read into
     * memory.
     *
     * <p>If the keystore password file ends with a new line, the new line is automatically stripped
     * from the password before it is returned. This ensures that a password file edited with a
     * POSIX-compliant editor continues to function as it did before editing. Note that only the
     * last, platform-dependent newline is stripped from the password; whitespace or multiple
     * newlines at the end of the password file are left intact.
     *
     * @param pathPrefix
     *   The path prefix where files will be opened relative to. This can be null or "" in which
     *   case the current directory is assumed. This will not be applied to any files that start
     *   with a file separator.
     *
     * @return The keystore password as a char array
     *
     * @throws IOException
     *   If the keystore password file cannot be opened
     */
    @JsonIgnore
    public char[] getKeyStorePassword(final String pathPrefix) throws IOException {
        final String fileName        = prefix(pathPrefix, getKeyStorePasswordFile()),
                     fileContents    = new String(Files.readAllBytes(Paths.get(fileName)), UTF_8);
        String       trimmedContents = fileContents;

        for (String newLine : NEWLINE_TYPES) {
            trimmedContents = trimFromEnd(trimmedContents, newLine);

            if (!trimmedContents.equals(fileContents)) {
                // Only replace a single newline
                break;
            }
        }

        return trimmedContents.toCharArray();
    }

    /**
     * Get the key password used to unlock individual key entries.The results in the
     * file being opened and read into memory.
     *
     * @param pathPrefix The path prefix where files will be opened relative to. This can be null or ""
     *                   in which case the current directory is assumed. This will not be applied
     *                   to any files that start with a file separator.
     * @return The key password as a char array
     * @throws IOException If the key password file can not be opened
     */

    @JsonIgnore
    public char[] getKeyPassword(final String pathPrefix) throws IOException {
        String fileName = prefix(pathPrefix, getKeyPasswordFile());
        return new String(Files.readAllBytes(Paths.get(fileName)), UTF_8).toCharArray();
    }

    /**
     * Initialize and load the keystore described by this configuration
     *
     * There are a number of possible exceptions that can be generated - they are consolidated
     * to a single KeyStoreException and the underlying exception is wrapped. If dynamic
     * classloading is required to load the KeyStoreProvider, the current ClassLoader is used.
     *
     * @param pathPrefix The path prefix where files will be opened relative to. This can be null or ""
     *                   in which case the current directory is assumed. This will not be applied
     *                   to any files that start with a file separator.
     * @return the opened KeyStore
     * @throws KeyStoreException if the keystore can not be opened or initialized.
     */
    public KeyStore loadKeyStore(final String pathPrefix) throws KeyStoreException {
        return loadKeyStore(pathPrefix, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Initialize and load the keystore described by this configuration
     *
     * There are a number of possible exceptions that can be generated - they are consolidated
     * to a single KeyStoreException and the underlying exception is wrapped.
     *
     * @param pathPrefix The path prefix where files will be opened relative to. This can be null or ""
     *                   in which case the current directory is assumed. This will not be applied
     *                   to any files that start with a file separator.
     * @param classLoader  The classloader to use for dynamic classloading of the KeyStore Provider
     * @return the opened KeyStore
     * @throws KeyStoreException if the keystore can not be opened or initialized.
     */
    public KeyStore loadKeyStore(final String pathPrefix,  final ClassLoader classLoader) throws KeyStoreException {
        KeyStore ks = null;
        try {
            KeyStoreBuilder builder = new KeyStoreBuilder();
            String file = getKeyStoreFile();

            // If keystore file is supplied, this is a keystore on disk type of keystore...
            if (file != null) {
                builder = builder.withKeyStoreFile(prefix(pathPrefix, file))
                        .withKeyStoreType(getKeyStoreType())
                        .withPassword(getKeyStorePassword(pathPrefix));
            } else if (getProviderClass() != null) {
                builder = builder
                        .withProviderClass(getProviderClass(), classLoader)
                        .withKeyStoreType(getKeyStoreType());
                if (getProviderArg() != null) {
                    builder = builder.withProviderArgument(getProviderArg());
                }
                if (getParameters() != null) {
                    final MapKeyStoreParameters params = new MapKeyStoreParameters(getParameters());
                    builder = builder.withLoadStoreParameter(params);
                }
            } else {
                throw new KeyStoreException("Could not initialize keystore - the configuration file is incomplete");
            }

            ks = builder.build();
        } catch (IOException e) {
            throw new KeyStoreException("Could not initialize the Keystore. Cause: " + e.getMessage(), e);
        }
        return ks;
    }

    /**
     * Apply the prefix to the string.
     *
     * @param pathPrefix The path prefix where files will be opened relative to.
     * @param file name string
     * @return the file name prefixed by that path prefix. If the file name starts with /, it is not modified
     */
    private static String prefix(final String pathPrefix, final String file) {
        if (pathPrefix == null || pathPrefix.equals("") || file.startsWith(File.separator)) {
            return file;
        }
        return pathPrefix + (pathPrefix.endsWith(File.separator) ? "" : File.separator) + file;
    }

    /**
     * Trims the specified pattern from the end of the provided value.
     *
     * <p>If the provided value does not end with the specified pattern, the value is returned
     * as-is.
     *
     * @param value
     *   The value that needs trimming.
     * @param pattern
     *   The pattern to remove from the end of the value, if it exists.
     *
     * @return
     *   The trimmed string (if the pattern was present); or the original string (if the pattern was
     *   not present).
     */
    private static String trimFromEnd(final String value, final String pattern) {
        final String trimmedValue;

        if (value.endsWith(pattern)) {
            final int   valueLength   = value.length(),
                        patternLength = pattern.length(),
                        trimmedLength = valueLength - patternLength;

            trimmedValue = value.substring(0, trimmedLength);
        } else {
            trimmedValue = value;
        }

        return trimmedValue;
    }
}
