package jsonpath;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import spark.ResponseTransformer;

public class JsonTransformer implements ResponseTransformer {

    JsonProvider jsonProvider = new GsonJsonProvider();

    @Override
    public String render(Object model) {
        return jsonProvider.toJson(model);
    }
}