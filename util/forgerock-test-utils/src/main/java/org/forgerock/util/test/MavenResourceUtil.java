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
 * Copyright 2018 Wren Security. All rights reserved.
 */
package org.forgerock.util.test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility methods for retrieving test resource files from the Maven project path.
 */
public final class MavenResourceUtil {
    /**
     * Private constructor for singleton static class.
     */
    private MavenResourceUtil() { }

    /**
     * Get a file representation of the specified relative file resource path.
     *
     * @param relativePath
     *   The path to the file resource, relative to the root of the current project being compiled
     *   (i.e. relative to the {@code basedir} system property.
     *
     * @return
     *   An absolute {@code File} reference that can be used to access the resource, regardless of
     *   the current working directory.
     */
    public static File getFileForPath(final String relativePath) {
        return getPath(relativePath).toFile();
    }

    /**
     * Get the appropriate path to the file having the specified relative path.
     *
     * @param relativePath
     *   The path to the file resource, relative to the root of the current project being compiled
     *   (i.e. relative to the {@code basedir} system property.
     *
     * @return
     *   An absolute {@code Path} that can be used to reference the resource, regardless of the
     *   current working directory.
     */
    public static Path getPath(final String relativePath) {
        String baseDir = System.getProperty("basedir");

        if (baseDir == null) {
            // Fall back to current working directory; to support tests run directly
            // (e.g. through an IDE)
            baseDir = System.getProperty("user.dir");
        }

        return Paths.get(baseDir, relativePath).toAbsolutePath();
    }
}
