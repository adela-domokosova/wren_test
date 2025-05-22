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
 * Copyright 2016 ForgeRock AS.
 * Portions Copyright 2018 Wren Security.
 */

package org.forgerock.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.testng.annotations.Test;

/**
 * Tests using Javascript with API Descriptor's Builder API.
 */
public class JavascriptTest {

    @Test
    public void testSimpleExample() throws Exception {
        final Object result = executeScript("SimpleExample", "/com/forgerock/api/SimpleExample.js");

        assertThat(result).isEqualTo(
            "{\"id\":\"frapi:test\",\"paths\":{\"/testPath\":{\"1"
            + ".0\":{\"actions\":[{\"name\":\"action1\",\"response\":{\"boolean\":false,"
            + "\"collection\":false,\"list\":false,\"map\":true,\"notNull\":true,"
            + "\"null\":false,\"number\":false,\"pointer\":{\"empty\":true},"
            + "\"string\":false}}],\"mvccSupported\":true},\"2"
            + ".0\":{\"actions\":[{\"name\":\"action1\",\"response\":{\"boolean\":false,"
            + "\"collection\":false,\"list\":false,\"map\":true,\"notNull\":true,"
            + "\"null\":false,\"number\":false,\"pointer\":{\"empty\":true},"
            + "\"string\":false}},{\"name\":\"action2\",\"response\":{\"boolean\":false,"
            + "\"collection\":false,\"list\":false,\"map\":true,\"notNull\":true,"
            + "\"null\":false,\"number\":false,\"pointer\":{\"empty\":true},"
            + "\"string\":false}}],\"mvccSupported\":true}}},\"version\":\"1.0\"}"
        );
    }

    private Object executeScript(String name, final String resourcePath)
    throws Exception {
        final InputStream stream = JavascriptTest.class.getResourceAsStream(resourcePath);

        Context context = Context.enter();

        try {
            final Scriptable scope = context.initStandardObjects();

            final Object result = context.evaluateReader(
                scope,
                new InputStreamReader(stream),
                name,
                1,
                null
            );

            if (result instanceof NativeJavaObject) {
                return ((NativeJavaObject)result).unwrap();
            } else {
                return result;
            }
        } catch (JavaScriptException e) {
            String message = e.sourceName() + ":" + e.lineNumber();

            if (e.getValue() instanceof NativeObject) {
                message += ": " + ((NativeObject) e.getValue()).get("message").toString();
            }

            throw new Exception(message);
        } finally {
            Context.exit();
        }
    }
}
