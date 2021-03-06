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
 * Copyright 2014 ForgeRock AS.
 */
package org.forgerock.openam.shared.security.whitelist;

import com.sun.identity.shared.debug.Debug;
import com.sun.identity.shared.whitelist.URLPatternMatcher;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

/**
 * Validates the provided redirect URL against the list of valid goto URL domains.
 *
 * @param <T> The type of the configuration information that is provided to find the collection of valid domains.
 */
public class RedirectUrlValidator<T> {

    private static final Debug DEBUG = Debug.getInstance("patternMatching");
    private final ValidDomainExtractor<T> domainExtractor;

    public RedirectUrlValidator(final ValidDomainExtractor<T> domainExtractor) {
        this.domainExtractor = domainExtractor;
    }

    /**
     * Validates the provided redirect URL against the collection of valid goto URL domains found based on the
     * configuration info.
     *
     * @param url The URL that needs to be validated. May be null.
     * @param configInfo The necessary information about the configuration to determine the collection of valid goto
     * URL domains. May not be null.
     * @return <code>true</code> if the provided URL is valid, <code>false</code> otherwise.
     */
    public boolean isRedirectUrlValid(final String url, final T configInfo) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        final Collection<String> patterns = domainExtractor.extractValidDomains(configInfo);
        if (DEBUG.messageEnabled()) {
            DEBUG.message("Validating goto URL " + url + " against patterns:\n" + patterns);
        }
        if (patterns == null || patterns.isEmpty()) {
            if (DEBUG.messageEnabled()) {
                DEBUG.message("There are no patterns to validate the URL against, the goto URL is considered valid");
            }
            return true;
        }

        try {
            final URI uri = new URI(url);
            if (!uri.isAbsolute()) {
                if (DEBUG.messageEnabled()) {
                    DEBUG.message(url + " is a relative URI, the goto URL is considered valid");
                }
                return true;
            }
        } catch (final URISyntaxException urise) {
            if (DEBUG.messageEnabled()) {
                DEBUG.message("The goto URL " + url + " is not a valid URI", urise);
            }
            return false;
        }

        final URLPatternMatcher patternMatcher = new URLPatternMatcher();
        try {
            return patternMatcher.match(url, patterns, true);
        } catch (MalformedURLException murle) {
            DEBUG.error("An error occurred while validating goto URL: " + url, murle);
            return false;
        }
    }
}
