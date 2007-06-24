/*
 * Messages
 */

package org.iatrix.util;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 *
 * Description of Messages
 *
 * @author danlutz@watz.ch
 */
public class IatrixMessages {
    public static final String EMPTY = "(Empty)";
    public static final String CHANGE = "CHANGE";
    
    private static final String BUNDLE_NAME
            = "org.iatrix.util.IatrixMessages"; //$NON-NLS-1$

    private static ResourceBundle bundle = null;

    // hide constructor
    private IatrixMessages() {
    }

    public static String getString(String key) {
        try {
            if (bundle == null) {
                bundle = ResourceBundle.getBundle(BUNDLE_NAME);
            }
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            Debug.log("Key " + key + " not found.");
            return "!" + key + "!";
        } catch (Exception e) {
            Debug.log("Resource bundle not found.");
            return "!" + key + "!";
        }
    }
}