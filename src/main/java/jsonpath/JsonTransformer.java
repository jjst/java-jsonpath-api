package jsonpath;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.zalando.problem.gson.ProblemAdapterFactory;
import spark.ResponseTransformer;

public class JsonTransformer implements ResponseTransformer {

    Gson gson =
        new GsonBuilder()
                .registerTypeAdapterFactory(new ProblemAdapterFactory())
                .create();

    @Override
    public String render(Object model) {
        return gson.toJson(model);
    }
}