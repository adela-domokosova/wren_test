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
 * Copyright 2013-2017 ForgeRock AS.
 */

package org.forgerock.json.jose.jwk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.forgerock.json.JsonValue.field;
import static org.forgerock.json.JsonValue.json;
import static org.forgerock.json.JsonValue.object;

import java.lang.reflect.Method;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.forgerock.json.JsonValue;
import org.forgerock.json.jose.jwe.JweAlgorithm;
import org.forgerock.json.jose.jws.JwsAlgorithm;
import org.forgerock.json.jose.jwt.Algorithm;
import org.testng.ITest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class JWKSetTest  implements ITest {

    private String mTestCaseName;
    private String jsonAsString;
    private JsonValue jwkSetJson;
    private Map<String, JWK> jwksMapByKid = new HashMap<>();

    @BeforeClass
    public void setup() throws NoSuchAlgorithmException {
        jwksMapByKid.clear();

        //Generate some RSA JWKs
        List<JsonValue> listOfKeys = new LinkedList<>();
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        for (int i = 0; i < 10; i++) {
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            RsaJWK rsaJwk = new RsaJWK((RSAPublicKey) keyPair.getPublic(), KeyUse.SIG, JwsAlgorithm.RS256.name(),
                    "rsaJwk" + i, null, null, null);
            jwksMapByKid.put(rsaJwk.getKeyId(), rsaJwk);
            listOfKeys.add(rsaJwk.toJsonValue());
        }

        KeyPairGenerator ecKeyPairGenerator = KeyPairGenerator.getInstance("EC");
        ecKeyPairGenerator.initialize(256);
        for (int i = 0; i < 10; i++) {
            KeyPair keyPair = ecKeyPairGenerator.generateKeyPair();
            EcJWK ecJwk = new EcJWK((ECPublicKey) keyPair.getPublic(), KeyUse.SIG, "ecJwk" + i);
            jwksMapByKid.put(ecJwk.getKeyId(), ecJwk);
            listOfKeys.add(ecJwk.toJsonValue());
        }

        jwkSetJson = json(object(field("keys", listOfKeys)));
        jsonAsString = jwkSetJson.toString();
    }

    @Test
    public void testJWKSetConstructorFromAString() {
        JWKSet jwkSet = JWKSet.parse(jsonAsString);
        assertCopiedJwksIsEqualToOriginal(jwkSet.getJWKsAsList());
    }

    @Test
    public void testJWKSetConstructorFromAJsonValue() {
        JWKSet jwkSet = JWKSet.parse(jwkSetJson);
        assertCopiedJwksIsEqualToOriginal(jwkSet.getJWKsAsList());
    }

    @Test
    public void testJWKSetConstructorFromJWKList() {
        JWKSet jwkSet = new JWKSet(new ArrayList<>(jwksMapByKid.values()));
        assertCopiedJwksIsEqualToOriginal(jwkSet.getJWKsAsList());
    }

    /** Cannot use equals() on the two lists because JWK does not implement equals() and hashcode(). */
    private void assertCopiedJwksIsEqualToOriginal(List<JWK> jwks) {
        for (JWK jwk : jwks) {
            assertThat(jwk.getKeyId()).isNotNull();
            JWK jwkExpected = jwksMapByKid.get(jwk.getKeyId());
            assertThat(jwkExpected).isNotNull();
            assertThat(jwk.getKeyType()).isEqualTo(jwkExpected.getKeyType());
            assertThat(jwk.getAlgorithm()).isEqualTo(jwkExpected.getAlgorithm());
        }
    }

    @Test
    public void testFindJWKByKid() throws NoSuchAlgorithmException {
        //Given
        List<JWK> jwks = new ArrayList<>();

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        for (int i = 0; i < 10; i++) {
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            RsaJWK rsaJwk = new RsaJWK((RSAPublicKey) keyPair.getPublic(), KeyUse.SIG,
                    JwsAlgorithm.RS256.getJwaAlgorithmName(), "rsaJwk" + i, null, null, null);
            jwks.add(rsaJwk);
        }

        KeyPairGenerator ecKeyPairGenerator = KeyPairGenerator.getInstance("EC");
        ecKeyPairGenerator.initialize(256);
        for (int i = 0; i < 10; i++) {
            KeyPair keyPair = ecKeyPairGenerator.generateKeyPair();
            EcJWK ecJwk = new EcJWK((ECPublicKey) keyPair.getPublic(), KeyUse.SIG, "ecJwk" + i);
            jwks.add(ecJwk);
        }

        JWKSet jwkSet = new JWKSet(jwks);

        //We check that we can find all the jwks we created previously
        for (JWK jwkExpected : jwks) {
            //When
            JWK jwkFound = jwkSet.findJwk(jwkExpected.getKeyId());

            //Then
            assertThat(jwkFound).isNotNull();
            assertThat(jwkFound.getKeyId()).isEqualTo(jwkExpected.getKeyId());
        }
    }

    @DataProvider
    private Object[][] jwks() throws NoSuchAlgorithmException {
        final List<Object[]> results = new ArrayList<>();
        //case 1:
        String testName = "[{RSA_256(SIGN), RSA_OAEP_256(ENC)}]";

        List<JWK> jwks = new ArrayList<>();

        //Generate the keys
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);

        //A RSA key for signing
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        RsaJWK rsaSignJwk = new RsaJWK((RSAPublicKey) keyPair.getPublic(), KeyUse.SIG,
                JwsAlgorithm.RS256.getJwaAlgorithmName(),
                "rsaSignJwk", null, null, null);
        jwks.add(rsaSignJwk);

        //A RSA key for encrypting
        keyPair = keyPairGenerator.generateKeyPair();
        RsaJWK rsaEncJwk = new RsaJWK((RSAPublicKey) keyPair.getPublic(), KeyUse.ENC,
                JweAlgorithm.RSA_OAEP_256.getJwaAlgorithmName(), "rsaEncJwk", null, null, null);
        jwks.add(rsaEncJwk);

        JWKSet jwkSet = new JWKSet(jwks);

        //Test(s)
        results.add(new Object[]{ new TestParameters(testName + ", search signing key", jwkSet,
                JwsAlgorithm.RS256, KeyUse.SIG, rsaSignJwk)
        });
        results.add(new Object[]{ new TestParameters(testName + ", search encryption key", jwkSet,
                JweAlgorithm.RSA_OAEP_256, KeyUse.ENC, rsaEncJwk)
        });

        //case 2:
        testName = "[{RSA_256(), RSA_OAEP_256(ENC)}]";

        jwks = new ArrayList<>();

        //Generate the keys
        keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);

        //A RSA key for signing but without keyUse
        keyPair = keyPairGenerator.generateKeyPair();
        RsaJWK rsaSignJwkWithoutKeyUse = new RsaJWK((RSAPublicKey) keyPair.getPublic(), null,
                JwsAlgorithm.RS256.getJwaAlgorithmName(), "rsaSignJwk", null, null, null);
        jwks.add(rsaSignJwkWithoutKeyUse);

        //A RSA key for encrypting
        keyPair = keyPairGenerator.generateKeyPair();
        rsaEncJwk = new RsaJWK((RSAPublicKey) keyPair.getPublic(), KeyUse.ENC,
                JweAlgorithm.RSA_OAEP_256.getJwaAlgorithmName(), "rsaEncJwk", null, null, null);
        jwks.add(rsaEncJwk);

        //Test(s)
        jwkSet = new JWKSet(jwks);
        results.add(new Object[]{ new TestParameters(testName + ", search signing key",
                jwkSet, JwsAlgorithm.RS256, KeyUse.SIG, rsaSignJwkWithoutKeyUse)
        });

        //case 3:
        testName = "[{RSA_256(SIGN), RSA_256(), RSA_OAEP_256(ENC)}]";

        jwks = new ArrayList<>();

        //Generate the keys
        keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);

        //A RSA key for signing
        keyPair = keyPairGenerator.generateKeyPair();
        rsaSignJwk = new RsaJWK((RSAPublicKey) keyPair.getPublic(),
                KeyUse.SIG, JwsAlgorithm.RS256.getJwaAlgorithmName(), "rsaSignJwk", null, null,
                null);
        jwks.add(rsaSignJwk);

        //RSA key for signing but without keyuse
        keyPair = keyPairGenerator.generateKeyPair();
        rsaSignJwkWithoutKeyUse = new RsaJWK((RSAPublicKey) keyPair.getPublic(), null,
                JwsAlgorithm.RS256.getJwaAlgorithmName(), "rsaSignJwk", null, null, null);
        jwks.add(rsaSignJwkWithoutKeyUse);

        //A RSA key for encrypting
        keyPair = keyPairGenerator.generateKeyPair();
        rsaEncJwk = new RsaJWK((RSAPublicKey) keyPair.getPublic(), KeyUse.ENC,
                JweAlgorithm.RSA_OAEP_256.getJwaAlgorithmName(), "rsaEncJwk", null, null, null);
        jwks.add(rsaEncJwk);

        //test(s)
        jwkSet = new JWKSet(jwks);
        results.add(new Object[]{ new TestParameters(testName + ", search signing key",
                jwkSet, JwsAlgorithm.RS256, KeyUse.SIG, rsaSignJwk)
        });

        //case 4:
        testName = "[{RSA_256(SIGN), EC_256(SIGN), EC_384(SIGN), EC_521(SIGN)}]";

        jwks = new ArrayList<>();

        //Generate the keys
        keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);

        //A RSA key for signing
        keyPair = keyPairGenerator.generateKeyPair();
        rsaSignJwk = new RsaJWK((RSAPublicKey) keyPair.getPublic(), KeyUse.SIG,
                JwsAlgorithm.RS256.getJwaAlgorithmName(), "rsaSignJwk", null, null, null);
        jwks.add(rsaSignJwk);

        KeyPairGenerator ecKeyPairGenerator = KeyPairGenerator.getInstance("EC");

        //EC key 256 for signing
        ecKeyPairGenerator.initialize(256);
        keyPair = ecKeyPairGenerator.generateKeyPair();
        EcJWK ecJWK256 = new EcJWK((ECPublicKey) keyPair.getPublic(), KeyUse.SIG, "ecJWK256");
        jwks.add(ecJWK256);

        //EC key 384 for signing
        ecKeyPairGenerator.initialize(384);
        keyPair = ecKeyPairGenerator.generateKeyPair();
        EcJWK ecJWK384 = new EcJWK((ECPublicKey) keyPair.getPublic(), KeyUse.SIG, "ecJWK384");
        jwks.add(ecJWK384);

        //EC key 521 for signing
        ecKeyPairGenerator.initialize(521);
        keyPair = ecKeyPairGenerator.generateKeyPair();
        EcJWK ecJWK521 = new EcJWK((ECPublicKey) keyPair.getPublic(), KeyUse.SIG, "ecJWK521");
        jwks.add(ecJWK521);

        //test(s)
        jwkSet = new JWKSet(jwks);
        results.add(new Object[]{ new TestParameters(testName + ", search RSA signing key",
                jwkSet, JwsAlgorithm.RS256, KeyUse.SIG, rsaSignJwk)
        });

        jwkSet = new JWKSet(jwks);
        results.add(new Object[]{ new TestParameters(testName + ", search EC256 signing key",
                jwkSet, JwsAlgorithm.ES256, KeyUse.SIG, ecJWK256)
        });

        jwkSet = new JWKSet(jwks);
        results.add(new Object[]{new TestParameters(testName + ", search EC384 signing key",
                jwkSet, JwsAlgorithm.ES384, KeyUse.SIG, ecJWK384)
        });

        jwkSet = new JWKSet(jwks);
        results.add(new Object[]{new TestParameters(testName + ", search EC521 signing key",
                jwkSet, JwsAlgorithm.ES512, KeyUse.SIG, ecJWK521)
        });
        return results.toArray(new Object[0][]);
    }

    @Test(dataProvider = "jwks")
    public void testFindJWKByAlgo(TestParameters testParameters) throws NoSuchAlgorithmException {
        //When
        JWK jwkFound = testParameters.jwkSet.findJwk(testParameters.algorithm, testParameters.use);

        //Then
        assertThat(jwkFound).isNotNull();
        assertThat(jwkFound.getKeyId()).isEqualTo(testParameters.jwkExpected.getKeyId());
    }

    //Used to name the tests with a user friendly name
    public class TestParameters {
        public String testName = null;
        public JWKSet jwkSet;
        public Algorithm algorithm;
        public KeyUse use;
        public JWK jwkExpected;

        public TestParameters(String name, JWKSet jwkSet, Algorithm algorithm, KeyUse use, JWK jwkExpected) {
            this.testName = name;
            this.jwkSet = jwkSet;
            this.algorithm = algorithm;
            this.use = use;
            this.jwkExpected = jwkExpected;
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void testData(Method method, Object[] testData) {
        String testCase = "";
        if (testData != null && testData.length > 0) {
            TestParameters testParams = null;
            //Check if test method has actually received required parameters
            for (Object testParameter : testData) {
                if (testParameter instanceof TestParameters) {
                    testParams = (TestParameters) testParameter;
                    break;
                }
            }
            if (testParams != null) {
                testCase = testParams.testName;
            }
        }
        this.mTestCaseName = String.format("%s(%s)", method.getName(), testCase);
    }

    @Override
    public String getTestName() {
        return this.mTestCaseName;
    }
}
