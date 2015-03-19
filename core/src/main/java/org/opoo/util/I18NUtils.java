/*
 * Copyright 2013-2015 Alex Lin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opoo.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author Alex Lin
 */
public class I18NUtils {
    private static final Object[] NO_ARGS = new Object[0];

    public static String getString(String bundleName, Locale locale, String key) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale);
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return null;
        }
    }

    public static String format(String bundleName,
                                Locale locale,
                                String key,
                                Object arg1) {
        return format(bundleName, locale, key, new Object[]{arg1});
    }


    public static String format(String bundleName,
                                Locale locale,
                                String key,
                                Object arg1,
                                Object arg2) {
        return format(bundleName, locale, key, new Object[]{arg1, arg2});
    }

    /**
     * Looks up the value for <code>key</code> in the
     * <code>ResourceBundle</code> referenced by
     * <code>bundleName</code>, then formats that value for the
     * specified <code>Locale</code> using <code>args</code>.
     *
     * @return Localized, formatted text identified by
     * <code>key</code>.
     */
    public static String format(String bundleName, Locale locale, String key, Object[] args) {
        if (locale == null) {
            // When formatting Date objects and such, MessageFormat
            // cannot have a null Locale.
            locale = Locale.getDefault();
        }

        String value = getString(bundleName, locale, key);
        if (args == null) {
            args = NO_ARGS;
        }

        // FIXME: after switching to JDK 1.4, it will be possible to clean
        // this up by providing the Locale along with the string in the
        // constructor to MessageFormat.  Until 1.4, the following workaround
        // is required for constructing the format with the appropriate locale:
        MessageFormat messageFormat = new MessageFormat("");
        messageFormat.setLocale(locale);
        messageFormat.applyPattern(value);

        return messageFormat.format(args);
    }
}
