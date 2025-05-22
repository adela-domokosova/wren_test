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
 * Copyright 2011 ForgeRock AS
 * Portions Copyright 2022 Wren Security.
 */
package org.forgerock.i18n.maven;

import java.io.File;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Goal which generates message source files from a one or more property files.
 */
@Mojo(name="generate-test-messages", defaultPhase=LifecyclePhase.GENERATE_TEST_SOURCES, threadSafe=true)
public final class GenerateTestMessagesMojo extends
        AbstractGenerateMessagesMojo {

    /**
     * The target directory in which the source files should be generated.
     */
    @Parameter(defaultValue="${project.build.directory}/generated-test-sources/messages", required=true)
    private File targetDirectory;

    /**
     * The resource directory containing the message files.
     */
    @Parameter(defaultValue="${basedir}/src/test/resources", required=true)
    private File resourceDirectory;

    /**
     * {@inheritDoc}
     */
    @Override
    void addNewSourceDirectory(final File targetDirectory) {
        getMavenProject().addTestCompileSourceRoot(
                targetDirectory.getAbsolutePath());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    File getResourceDirectory() {
        return resourceDirectory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    File getTargetDirectory() {
        return targetDirectory;
    }

}
