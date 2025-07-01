import java.io.EOFException;
import java.io.IOException;

public class PlainSaslSmtpExample {

	/**
	 * Set to null to read from standard input.
	 */
	private static final String USERNAME = "emeng@cs.wisc.edu";

	/**
	 * Set to null to read from standard input.
	 */
	private static final String PASSWORD = null;

	private final String myUsername;
	private final String myPassword;

	public static void main(String[] args) throws IOException {
		new PlainSaslSmtpExample(USERNAME, PASSWORD).run();
	}

	public PlainSaslSmtpExample(String username, String password) throws IOException {
		if (username != null) {
			myUsername = username;
		} else {
			String input = System.console().readLine("Username: ");
			if (input == null) {
				throw new EOFException(
					"Reached end of stream while I was reading your username. "
					+ "Does Java Virtual Machine support console interaction?");
			}
			myUsername = input;
		}

		if (password != null) {
			myPassword = password;
		} else {
			char[] passwordChars = System.console().readPassword("Password: ");
			if (passwordChars == null) {
				throw new EOFException(
					"Reached end of stream while I was reading your password. "
					+ "Does Java Virtual Machine support console interaction?");
			}
			myPassword = new String(passwordChars);
		}
	}

	public void run() {

	}

}
