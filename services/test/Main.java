import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

import java.security.interfaces.RSAPublicKey;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class Main {
    public static void main(String[] args) {
        String jsonStr = "{\n" +
                "    \"keys\": [\n" +
                "        {\n" +
                "            \"kid\": \"6719678351a5faedc2e70274bbea62da2a8c4a12\",\n" +
                "            \"n\": \"oAP5OnSzKfkEV2QMm2XCuu4G8VGRBOyhKg-4H04WzYzPqM_Tmqi60Vod96JTo7SfM0OoGeNnlkWNjjBWkSS66alNLrvTNLi0A-KGeBsZiIFmrbsP6HHJfFzPd0Mci7-e11fNKecZgbC1me9PtRXFZb9JprZGFOvBiMwU0rRvh0GWYmTFj1HFjOIMAwTGOKOVGNuPjv0b3V0YaAkUNklzi4MM6qgzUb0tE0so1Ii7kBe7roMScS2USPeeJkeoPjLEbQcrT8MxOSxH-JgPLfq-zOnEJ6ERW3mtXdZCNzqmVLn5yjX5lKr5E2vgkPAHx9NLZ09fo_L9woeX_5epl6cIkQ\",\n" +
                "            \"kty\": \"RSA\",\n" +
                "            \"use\": \"sig\",\n" +
                "            \"e\": \"AQAB\",\n" +
                "            \"alg\": \"RS256\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"use\": \"sig\",\n" +
                "            \"alg\": \"RS256\",\n" +
                "            \"e\": \"AQAB\",\n" +
                "            \"kty\": \"RSA\",\n" +
                "            \"n\": \"w-l_VE4KNa22n4nsMwcabujowm924YoQQnwOz_dPYHmDI1O-r2bqw6mHmByXwii7aaeIMHJZWpmT5SkR3OYIu5RbSgiU-8JrQoplW_vZY2IqG1y5-frPC_9gnz_0qKKjtjqglCP-1AlfOdu7r5kOpkOACs5mWn4tm1K9R1EPjk2T_MMO7FkteZd8woh1fwUUuvbhPyDxBzx9EUsnGWbpTndOYc7W-EUk1jMtWBk3buLeaypVaOLWranK_XFrX-xx03BohrfinOqmftYgc0z94sxix7X1G36JZeh8-jpUhwyBPinBxOZOE_5kQn4CYM66Ygxwiws0ZJ-klG2qTi239w\",\n" +
                "            \"kid\": \"674dbba8faee69acae1bc1be190453678f472803\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";
		try {
			JSONObject data = new JSONObject(jsonStr);
			JSONArray keys = data.getJSONArray("keys");

			for (int i = 0; i < keys.length(); i++) {
				JSONObject key = keys.getJSONObject(i);
				String nStr = key.getString("n");
				String eStr = key.getString("e");

				BigInteger n = new BigInteger(1, base64UrlDecode(nStr));
				BigInteger e = new BigInteger(1, base64UrlDecode(eStr));

				RSAPublicKeySpec spec = new RSAPublicKeySpec(n, e);
				KeyFactory factory = KeyFactory.getInstance("RSA");
				RSAPublicKey pubKey = (RSAPublicKey) factory.generatePublic(spec);

				String pemKey = getPem(pubKey);
				System.out.println(pemKey);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
    }

    private static byte[] base64UrlDecode(String input) {
        input = input.replace('-', '+').replace('_', '/');
        int padding = 4 - (input.length() % 4);
        if (padding != 4) {
            input += "====".substring(0, padding);
        }
        return Base64.getDecoder().decode(input);
    }

    private static String getPem(RSAPublicKey pubKey) {
        StringBuilder sb = new StringBuilder();
        sb.append("-----BEGIN PUBLIC KEY-----\n");
        sb.append(Base64.getEncoder().encodeToString(pubKey.getEncoded()));
        sb.append("\n-----END PUBLIC KEY-----");
        return sb.toString();
    }
}
