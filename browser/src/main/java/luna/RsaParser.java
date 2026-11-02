package luna;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

/**
 * Reads the shared public login key using browser-supported Java streams.
 *
 * @author lare96
 */
public final class RsaParser {
    private static BigInteger modulus;
    private static BigInteger exponent;

    private RsaParser() {
    }

    public static void parse() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (InputStream input = ClientResources.open("rsa/rsapub.toml")) {
            byte[] buffer = new byte[1024];
            for (int count; (count = input.read(buffer)) != -1;) {
                bytes.write(buffer, 0, count);
                if (bytes.size() > 8192) throw new IOException("Public key resource is oversized");
            }
        }
        for (String line : new String(bytes.toByteArray(), StandardCharsets.UTF_8).split("\\r?\\n")) {
            String token = line.trim();
            if (token.startsWith("modulus =")) modulus = parseValue(token);
            else if (token.startsWith("exponent =")) exponent = parseValue(token);
        }
        if (modulus == null || exponent == null) throw new IOException("Public key resource is incomplete");
    }

    private static BigInteger parseValue(String line) throws IOException {
        int start = line.indexOf('"');
        int end = line.lastIndexOf('"');
        if (start < 0 || end <= start) throw new IOException("Invalid public key value");
        try {
            return new BigInteger(line.substring(start + 1, end));
        } catch (NumberFormatException exception) {
            throw new IOException("Invalid public key number", exception);
        }
    }

    public static BigInteger getModulus() {
        if (modulus == null) throw new IllegalStateException("RSA modulus value was not parsed.");
        return modulus;
    }

    public static BigInteger getExponent() {
        if (exponent == null) throw new IllegalStateException("RSA exponent value was not parsed.");
        return exponent;
    }
}
