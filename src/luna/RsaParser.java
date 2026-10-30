package luna;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class RsaParser {

    private static BigInteger modulus;
    private static BigInteger exponent;

    public static void parse() throws IOException {
        try (InputStream stream = ClientResources.open("rsa/rsapub.toml");
             Scanner reader = new Scanner(stream, StandardCharsets.UTF_8)) {
            reader.nextLine();
            modulus = parseValue(reader.nextLine());
            exponent = parseValue(reader.nextLine());
        }
    }

    private static BigInteger parseValue(String token) {
        int start = token.indexOf('"') + 1;
        int finish = token.lastIndexOf('"');
        String value = token.substring(start, finish);
        return new BigInteger(value);
    }

    public static BigInteger getModulus() {
        if (modulus == null) {
            throw new NullPointerException("RSA modulus value was not parsed.");
        }
        return modulus;
    }

    public static BigInteger getExponent() {
        if (exponent == null) {
            throw new NullPointerException("RSA exponent value was not parsed.");
        }
        return exponent;
    }
}
