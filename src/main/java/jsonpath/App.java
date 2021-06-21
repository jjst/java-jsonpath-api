package jsonpath;

import com.google.gson.Gson;
import com.jayway.jsonpath.*;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import lombok.Data;
import org.eclipse.jetty.http.HttpHeader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import spark.ResponseTransformer;

import static spark.Spark.*;

enum JsonPathEngine {
    JAYWAY
}

class ConfigurationDefaults implements Configuration.Defaults {
    private final JsonProvider jsonProvider = new GsonJsonProvider();
    private final MappingProvider mappingProvider = new GsonMappingProvider();

    @Override
    public JsonProvider jsonProvider() {
        return jsonProvider;
    }

    @Override
    public MappingProvider mappingProvider() {
        return mappingProvider;
    }

    @Override
    public Set<Option> options() {
        return EnumSet.noneOf(Option.class);
    }
}

public class App {

    private static String APPLICATION_JSON = "application/json";

    private static Logger logger = LoggerFactory.getLogger(App.class);

    private static ResponseTransformer responseTransformer = new JsonTransformer();

    public static void main(String[] args) {
        Configuration.setDefaults(new ConfigurationDefaults());
        // CORS handling
        options("/*",
                (request, response) -> {

                    String accessControlRequestHeaders = request
                            .headers("Access-Control-Request-Headers");
                    if (accessControlRequestHeaders != null) {
                        response.header("Access-Control-Allow-Headers",
                                accessControlRequestHeaders);
                    }

                    String accessControlRequestMethod = request
                            .headers("Access-Control-Request-Method");
                    if (accessControlRequestMethod != null) {
                        response.header("Access-Control-Allow-Methods",
                                accessControlRequestMethod);
                    }

                    return "OK";
                });
        before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));

        post("/", (req, res) -> {
            res.type(APPLICATION_JSON);
            if (!req.contentType().equals(APPLICATION_JSON)) {
                res.status(Status.BAD_REQUEST.getStatusCode());
                return Problem.valueOf(Status.BAD_REQUEST, "Only 'application/json' is supported");
            }
            // FIXME: ok parsing json manually gets annoying quickly, maybe i do want
            // some automated mapping thingy or at least to read into a record class
            JSONObject json = null;
            try {
                json = new JSONObject(req.body());
            } catch (JSONException e) {
                res.status(Status.BAD_REQUEST.getStatusCode());
                return Problem.valueOf(Status.BAD_REQUEST, "Malformed JSON");
            }
            if(!json.has("json_str")) {
                res.status(Status.BAD_REQUEST.getStatusCode());
                return Problem.valueOf(Status.BAD_REQUEST, "Field 'json_str' is mandatory");
            }
            String jsonStr = null;
            try {
                jsonStr = json.getString("json_str");
            } catch (JSONException e) {
                logger.error("Error parsing json from request", e);
                halt(400, "Malformed json: json_str field should be of type string");
            }
            var engine = JsonPathEngine.JAYWAY;
            if(json.has("options")) {
                var options = json.getJSONObject("options");
                if (options.has("engine")) {
                   var engineStr = options.getString("engine");
                   try {
                       engine = JsonPathEngine.valueOf(engineStr.toUpperCase(Locale.ROOT));
                   } catch(IllegalArgumentException e) {
                       halt(400, "Invalid json engine: " + engineStr);
                   }
                }
            }
            var queries= extractQueries(json);
            logger.info("json_str = " + jsonStr);
            JsonProvider jsonProvider = Configuration.defaultConfiguration().jsonProvider();
            Object document = jsonProvider.parse(jsonStr);
            Map<String, String> queryToResult = new HashMap<>();
            for (String queryStr: queries) {
                queryToResult.put(queryStr, extractJson(document, queryStr, jsonProvider));
            }
            return jsonProvider.toJson(queryToResult);
        });

    }

    static List<String> extractQueries(JSONObject json) {
        var jsonPathQueries = new ArrayList<String>();
        if(!json.has("queries")) {
            halt(400, "Field 'queries' is mandatory");
        }
        try {
            JSONArray queries = json.getJSONArray("queries");
            for(Object query: queries) {
                try {
                    jsonPathQueries.add((String) query);
                } catch(ClassCastException e) {
                    halt(400, "Malformed json");
                }
            }
        } catch (JSONException e) {
            halt(400, "Malformed json");
        }
        return jsonPathQueries;
    }

    static String extractJson(Object document, String jsonPathQuery, JsonProvider jsonProvider) {
        logger.info("query = " + jsonPathQuery);
        try {
            Object result = JsonPath.read(document, jsonPathQuery);
            return jsonProvider.toJson(result);
        } catch(ClassCastException e) {
            Object[] result = JsonPath.read(document, jsonPathQuery);
            return jsonProvider.toJson(result);
        } catch(JsonPathException e) {
            return "";
        }
    }
}
