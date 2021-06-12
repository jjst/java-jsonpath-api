package jsonpath;

import static spark.Spark.get;

public class App {

    public static void main(String[] args) {
        get("/", (req, res) -> {
            return "hello from sparkjava.com";
        });
    }

}
