import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.json.simple.JSONObject;

import static spark.Spark.get;

public class Main {
    static String secret = "secret";
    public static void main(String[] arg) {
        DB db = new DB();
        get("/register/:username/:password", (request, response) -> {
            JSONObject resp = new JSONObject();
            resp.put("status", db.registerUser(request.params(":username"), request.params(":password"))?"Ok":"Fail");
            return resp.toJSONString();
        });
        get("/login/:username/:password", (request, response) -> {
            JSONObject resp = new JSONObject();
            if (db.loginCheck(request.params(":username"), request.params(":password")))
            {
                resp.put("status", "Ok");
                final String token = JWT
                        .create()
                        .withClaim("username", request.params(":username")).sign(Algorithm.HMAC256(secret));
                resp.put("token", token);
            }
            else {
                resp.put("status", "Fail");
            }
            return resp.toJSONString();
        });
        get("/addrecord/:token/:game/:record", (request, response) -> {
            JSONObject resp = new JSONObject();
            boolean status=false;
            try {
                JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                        .build();
                DecodedJWT jwt = verifier.verify(request.params(":token"));
                status = db.addRecord(jwt.getClaim("username").asString(), request.params(":game"), Integer.parseInt(request.params(":record")));
            }
            catch (JWTVerificationException exception){
                resp.put("status", "Fail");
            }
            resp.put("status", status?"Ok":"Fail");
            return resp.toJSONString();
        });
        get("/getuserrecords/:user", (request, response) -> {
            JSONObject resp = new JSONObject();
            resp.put("results", db.showRecordsForUser(request.params(":user")));
            return resp.toJSONString();
        });
        get("/getgamerecords/:game", (request, response) -> {
            JSONObject resp = new JSONObject();
            resp.put("results", db.showRecordsForGame(request.params(":game")));
            return resp.toJSONString();
        });
        get("/getallrecords", (request, response) -> {
            JSONObject resp = new JSONObject();
            resp.put("results", db.showAllRecords());
            return resp.toJSONString();
        });
    }
}
