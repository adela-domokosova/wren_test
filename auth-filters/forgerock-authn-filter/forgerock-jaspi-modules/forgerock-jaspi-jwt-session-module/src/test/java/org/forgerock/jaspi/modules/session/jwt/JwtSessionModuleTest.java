package org.forgerock.jaspi.modules.session.jwt;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;

import org.forgerock.http.protocol.Cookie;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class JwtSessionModuleTest {

    JwtSessionModule jwtSessionModule;

    @BeforeMethod
    public void setUp() {
        jwtSessionModule = new JwtSessionModule();
        jwtSessionModule.cookieDomains = Collections.singleton("example.com");
    }

    @Test
    public void shouldCreateSessionCookieWithMaxAge() {
        Collection<CookieWrapper> cookies = jwtSessionModule.createCookies("foo", 7, "/");
        assertEquals(cookies.size(), 1);
        Cookie cookie = cookies.iterator().next().getCookie();
        assertEquals(cookie.getMaxAge(), Integer.valueOf(7));
        assertNull(cookie.getExpires());
    }

    @Test
    public void shouldCreateSessionCookieWithoutMaxAge() {
        Collection<CookieWrapper> cookies = jwtSessionModule.createCookies("foo", -1, "/");
        assertEquals(cookies.size(), 1);
        Cookie cookie = cookies.iterator().next().getCookie();
        assertNull(cookie.getMaxAge());
        assertNull(cookie.getExpires());
    }

    @Test
    public void shouldCreateSessionExpiredCookie() {
        Collection<CookieWrapper> cookies = jwtSessionModule.createCookies("foo", 0, "/");
        assertEquals(cookies.size(), 1);
        Cookie cookie = cookies.iterator().next().getCookie();
        assertTrue(cookie.getMaxAge() <= 0);
    }

}
