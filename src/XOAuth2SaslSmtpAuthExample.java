/*
 * XOAuth2SaslSmtpAuthExample.java
 *
 * Java implementation of the SASL XOAUTH2 mechanism.  Used by the big guys
 * (Google and Microsoft namely).  I assume you already have an OAuth2 access
 * token with you; this is not an OAuth2 client that will do HTTP stuff for you
 * (I wouldn't know how to write that in Java anyways...).  See:
 * https://developers.google.com/workspace/gmail/imap/xoauth2-protocol#the_sasl_xoauth2_mechanism
 *
 * This file is directly forked from PlainSaslSmtpAuthExample.java.  Its
 * comments are retained below, but just so you know if you've used that file,
 * you can just mentally skip them knowing they're essentially the same....
 *
 * There are a few ways to use this file.  Obviously you will have to replace
 * USER and ACCESS with your own, but depending on how much you want to
 * hard-code stuff, you can:
 *
 *    - Set USER to your email and unset ACCESS (= set to null).
 *      That way, Java will only prompt you for the access token.
 *    - Unset both.  Java will prompt for both.
 *    - Set both.  (But aren't you gonna get tired of renewing the key
 *      and copying it in after a while?)
 *
 * The command-line interface is enabled if at least one argument is passed to
 * this program.  In this case USER and ACCESS are ignored entirely.
 * Instead, the user is taken from the first argument, and the access token is
 * taken from or the second argument, or if absent, it will prompt you for it.
 * A third argument is not allowed and will cause the program to die with an
 * IllegalArgumentException.
 *
 * Note that a JVM console/terminal *must* be available when omitted values
 * are present.  You may have to tinker with your IDE to find out how, or
 * you will have to make sure that values are always supplied some other way.
 */

import java.io.Console;
import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @version 1.1
 */
public class XOAuth2SaslSmtpAuthExample {

    /* These constants are provided in case your IDE does not hook the JVM
     * to a console suitable for I/O, or if your machine uses some weird
     * encoding that converts badly to UTF-8.  (See the constructor below) */

    /**
     * OAuth2 username.  This is generally your email <em>in full</em>.
     * (Do not omit the domain name; Microsoft doesn't just serve one domain!)
     * <p>
     * For Wiscmail specifically: this is your NETID@wisc.edu, not any
     * firstname.lastname aliases you registered in the admin panel at
     * {@literal <https://wiscmail.wisc.edu/account-admin/>}.
     */
    private static final String USER = "meng69@wisc.edu";

    /**
     * Access token.  Without the word "Bearer", and definitely
     * without trailing line feed!  There will be no example
     * placeholder this time: BYOT (bring your own token)!
     */
    private static final String ACCESS = null;

    private final String myUser;
    private final String myToke;

    public static void main(String[] args) throws IOException {
        XOAuth2SaslSmtpAuthExample example;
        if (args.length == 0) {
            example = new XOAuth2SaslSmtpAuthExample(USER, ACCESS);
        } else if (args.length == 1) {
            example = new XOAuth2SaslSmtpAuthExample(args[0], null);
        } else if (args.length == 2) {
            example = new XOAuth2SaslSmtpAuthExample(args[0], args[1]);
        } else {
            throw new IllegalArgumentException(usage());
        }
        example.run();
    }

    /**
     * I format the usage string, a conventional error message displayed
     * when the user passes a bad combination of arguments to a Unix program.
     * @return my usage string
     */
    private static String usage() {
        final String className = XOAuth2SaslSmtpAuthExample.class.getName();
        return String.format("Usage: java %s [user [access_token]]\n", className);
    }

    /**
     * Get terminal or die.
     * @return an I/O console, if available
     * @throws IOException if JVM is not properly attached to a terminal
     */
    private static Console getTty() throws IOException {
        Console tty = System.console();
        if (tty == null) {
            throw new IOException("JVM is not attached to a terminal device.");
        }
        return tty;
    }

    /**
     * Some initialization to ensure that we get the stuff
     * we need to perform SASL XOAUTH2 or die with an error.
     * @param username provided username
     * @param password proivded password
     * @throws IOException if JVM console is unavailable or acting up
     */
    public XOAuth2SaslSmtpAuthExample(String user, String access) throws IOException {
        /*
         * Here is, in fact, a third way to pass the user and access token.
         * It would be harder to set up if you're not on a Unix/Linux computer,
         * so I am commenting here as a somewhat niche alternative.
         * In a Bounrne shell, you can set environment variables of the child
         * process by simply writing NAME=VALUE before the command.  Assuming
         * you have compiled this file and 'XOAuth2SaslSmtpAuthExample.class'
         * is on your classpath, this is how you would pass them in:
         *
         *     OAUTH_USER='meng69@wisc.edu' OAUTH_ACCESS="$(./get_token.py)" \
         *          java XOAuth2SaslSmtpAuthExample
         *
         * C Shell doesn't have the luxury of this syntax.  What you can do
         * there instead is use env(1) to append your environment variables:
         *
         *     env OAUTH_USER='meng69@wisc.edu' OAUTH_ACCESS="$(./get_token.py)" \
         *          java XOAuth2SaslSmtpAuthExample
         *
         * You can set them more permanently using the shell built-in 'export':
         *
         *     export OAUTH_USER='meng69@wisc.edu'
         *     export OAUTH_ACCESS="$(./get_token.py)"
         *     java XOAuth2SaslSmtpAuthExample
         *
         * In C shell, you'd replace 'export' with 'setenv' and drop the '='
         * and use the older backtick capture:
         *
         *     setenv OAUTH_USER 'meng69@wisc.edu'
         *     setenv OAUTH_ACCESS `./get_token.py`
         *     java XOAuth2SaslSmtpAuthExample
         */
        if (user == null) {
            user = System.getenv("OAUTH_USER");
        }
        if (access == null) {
            access = System.getenv("OAUTH_ACCESS");
        }

        if (user != null) {
            myUser = user;
        } else {
            String input = getTty().readLine("User: ");
            if (input == null) {
                throw new EOFException();
            }
            myUser = input;
        }

        if (access != null) {
            myToke = access;
        } else {
            char[] passwordChars = getTty().readPassword("Access Token: ");
            if (passwordChars == null) {
                throw new EOFException();
            }
            myToke = new String(passwordChars);
        }
    }

    public void run() {
        /* "user=" {User} "^Aauth=Bearer " {Access Token} "^A^A" */
        String credentials = "user=" + myUser + "\u0001auth=Bearer " + myToke + "\u0001\u0001";
        byte[] binaryCredentials = credentials.getBytes(StandardCharsets.UTF_8);
        String base64Credentials = Base64.getEncoder().encodeToString(binaryCredentials);
        /* This string will be so disgustingly long it wouldn't even be
         * worth it for me to label it as XOAUTH2 -- you just know it is. */
        System.out.println(base64Credentials);
    }

}
/* vi: set ts=4 sw=4 et: */
