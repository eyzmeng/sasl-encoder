/*
 * PlainSaslSmtpAuthExample.java
 *
 * Java implementation of RFC 4616 (SASL PLAIN mechanism) encoded over Base64.
 * There are a few ways to use this file.  Obviously you will have to replace
 * USERNAME and PASSWORD with your own, but depending on how much you want to
 * hard-code stuff, you can:
 *
 *    - Set USERNAME to your email and unset PASSWORD (= set to null).
 *      That way, Java will only prompt you for the password.
 *    - Unset both.  Java will prompt for both.
 *    - Set both.  (NOT recommended unless you keep this file safe!)
 *
 * The command-line interface is enabled if at least one argument is passed to
 * this program.  In this case USERNAME and PASSWORD are ignored entirely.
 * Instead, the username is taken from the first argument, and the password is
 * taken from or the second argument, or if absent, it will prompt you for it.
 * A third argument is not allowed and will cause the program to die with an
 * IllegalArgumentException.
 */

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class PlainSaslSmtpAuthExample {

    /* These constants are provided in case your IDE does not hook the JVM
     * to a console suitable for I/O, or if your machine uses some weird
     * encoding that converts badly to UTF-8.  (See the constructor below) */

    /**
     * SMTP username.  Corresponds to RFC 4422 authcid.
     * Set to null to read from standard input.
     */
    private static final String USERNAME = "emeng@cs.wisc.edu";

    /**
     * SMTP password.
     * Set to null to read from standard input.
     */
    private static final String PASSWORD = "awwwtysm";

    private final String myUsername;
    private final String myPassword;

    public static void main(String[] args) throws IOException {
        PlainSaslSmtpAuthExample example;
        if (args.length == 0) {
            example = new PlainSaslSmtpAuthExample(USERNAME, PASSWORD);
        } else if (args.length == 1) {
            example = new PlainSaslSmtpAuthExample(args[0], null);
        } else if (args.length == 2) {
            example = new PlainSaslSmtpAuthExample(args[0], args[1]);
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
        final String className = PlainSaslSmtpAuthExample.class.getName();
        return String.format("Usage: java %s [username [password]]\n", className);
    }

    /**
     * Some initialization to ensure that we get the stuff
     * we need to perform SASL PLAIN or die with an error.
     * @param username provided username
     * @param password proivded password
     * @throws IOException if JVM console is acting up
     */
    public PlainSaslSmtpAuthExample(String username, String password) throws IOException {
        /*
         * Here is, in fact, a third way to pass the username and password.
         * It would be harder to set up if you're not on a Unix/Linux computer,
         * so I am commenting here as a somewhat niche alternative.
         * In a Bounrne shell, you can set environment variables of the child
         * process by simply writing NAME=VALUE before the command.  Assuming
         * you have compiled this file and 'PlainSaslSmtpAuthExample.class'
         * is on your classpath, this is how you would pass them in:
         *
         *     SASL_USERNAME='emeng@cs.wisc.edu' SASL_PASSWORD='awwwtysm' \
         *          java PlainSaslSmtpAuthExample
         *
         * C Shell doesn't have the luxury of this syntax.  What you can do
         * there instead is use env(1) to append your environment variables:
         *
         *     env SASL_USERNAME='emeng@cs.wisc.edu' SASL_PASSWORD='awwwtysm' \
         *          java PlainSaslSmtpAuthExample
         *
         * You can set them more permanently using the shell built-in 'export':
         *
         *     export SASL_USERNAME='emeng@cs.wisc.edu'
         *     export SASL_PASSWORD='awwwtysm'
         *     java PlainSaslSmtpAuthExample
         *
         * In C shell, you'd replace 'export' with 'setenv' and drop the '=':
         *
         *     setenv SASL_USERNAME 'emeng@cs.wisc.edu'
         *     setenv SASL_PASSWORD 'awwwtysm'
         *     java PlainSaslSmtpAuthExample
         */
        if (username == null) {
            username = System.getenv("SASL_USERNAME");
        }
        if (password == null) {
            password = System.getenv("SASL_PASSWORD");
        }

        if (username != null) {
            myUsername = username;
        } else {
            String input = System.console().readLine("Username: ");
            if (input == null) {
                throw new EOFException();
            }
            myUsername = input;
        }

        if (password != null) {
            myPassword = password;
        } else {
            char[] passwordChars = System.console().readPassword("Password: ");
            if (passwordChars == null) {
                throw new EOFException();
            }
            myPassword = new String(passwordChars);
        }
    }

    public void run() {
        String credentials = "\0" + myUsername + "\0" + myPassword;
        byte[] binaryCredentials = credentials.getBytes(StandardCharsets.UTF_8);
        String base64Credentials = Base64.getEncoder().encodeToString(binaryCredentials);
        System.out.println("AUTH PLAIN " + base64Credentials);
    }

}
