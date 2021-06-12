package jsonpath;

import static spark.Spark.*;

public class App {

    public static void main(String[] args) {
        post("/", (req, res) -> {
            if (req.contentType() == "application/json") {
                halt(400, "Only 'application/json' is supported");
            }
            System.out.println(req.body());
            return "hello from sparkjava.com";
        });
    }

}
