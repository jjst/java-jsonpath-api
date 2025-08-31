# Java JSONPath API

A lightweight HTTP API service for executing JSONPath queries against JSON documents. Built with Java and SparkJava framework.

## Overview

This service provides a simple REST API that allows you to query JSON documents using JSONPath expressions. It's particularly useful for:

- Extracting specific data from complex JSON structures
- Testing JSONPath queries
- Integration with other services that need JSON data extraction
- Building data transformation pipelines

## Features

- 🚀 **Fast and Lightweight**: Built with SparkJava for minimal overhead
- 🔍 **JSONPath Support**: Powered by Jayway JSONPath library
- 🌐 **CORS Enabled**: Ready for browser-based applications
- 🐳 **Docker Ready**: Containerized for easy deployment
- ☁️ **Cloud Native**: Configured for DigitalOcean App Platform
- 📊 **Observability**: Includes OpenTelemetry instrumentation

## Quick Start

### Prerequisites

- Java 11 or higher
- Maven 3.6 or higher

### Building the Application

```bash
# Clone the repository
git clone https://github.com/jjst/java-jsonpath-api.git
cd java-jsonpath-api

# Build the application
mvn clean package

# Run the application
java -jar target/java-jsonpath-api-jar-with-dependencies.jar
```

The service will start on port 4567 and be available at `http://localhost:4567`.

## API Documentation

### Endpoint

- **URL**: `/`
- **Method**: `POST`
- **Content-Type**: `application/json`

### Request Format

```json
{
  "json_str": "JSON string to query",
  "queries": ["array", "of", "jsonpath", "expressions"],
  "options": {
    "engine": "JAYWAY"
  }
}
```

#### Parameters

- **json_str** (required): The JSON document as a string that you want to query
- **queries** (required): An array of JSONPath expressions to execute
- **options** (optional): Configuration options
  - **engine** (optional): JSONPath engine to use (currently only "JAYWAY" is supported)

### Response Format

The API returns a JSON object mapping each query to its result:

```json
{
  "$.query1": "result1",
  "$.query2": "result2"
}
```

### Examples

#### Basic Query

```bash
curl -X POST http://localhost:4567/ \
  -H "Content-Type: application/json" \
  -d '{
    "json_str": "{\"name\": \"John\", \"age\": 30, \"city\": \"New York\"}",
    "queries": ["$.name", "$.age"]
  }'
```

**Response:**
```json
{
  "$.name": "\"John\"",
  "$.age": "30"
}
```

#### Array Operations

```bash
curl -X POST http://localhost:4567/ \
  -H "Content-Type: application/json" \
  -d '{
    "json_str": "{\"users\": [{\"name\": \"Alice\", \"age\": 25}, {\"name\": \"Bob\", \"age\": 30}]}",
    "queries": ["$.users[0].name", "$.users[*].age", "$.users.length()"]
  }'
```

**Response:**
```json
{
  "$.users[0].name": "\"Alice\"",
  "$.users[*].age": "[25,30]",
  "$.users.length()": "2"
}
```

#### Complex JSON Structure

```bash
curl -X POST http://localhost:4567/ \
  -H "Content-Type: application/json" \
  -d '{
    "json_str": "{\"store\": {\"book\": [{\"title\": \"Book 1\", \"price\": 10.99}, {\"title\": \"Book 2\", \"price\": 12.99}], \"bicycle\": {\"color\": \"red\", \"price\": 19.95}}}",
    "queries": ["$.store.book[*].title", "$.store..price"]
  }'
```

## JSONPath Quick Reference

| Expression | Description |
|------------|-------------|
| `$` | Root element |
| `$.property` | Direct child property |
| `$['property']` | Bracket notation for property |
| `$.array[0]` | First element of array |
| `$.array[*]` | All elements of array |
| `$..property` | Recursive descent (find property anywhere) |
| `$.array[?(@.prop > 5)]` | Filter array elements |

For more JSONPath syntax, see the [JSONPath documentation](https://github.com/json-path/JsonPath).

## Docker Usage

### Building the Docker Image

```bash
docker build -t java-jsonpath-api .
```

### Running with Docker

```bash
docker run -p 4567:4567 java-jsonpath-api
```

### Using Docker Compose

Create a `docker-compose.yml` file:

```yaml
version: '3.8'
services:
  jsonpath-api:
    build: .
    ports:
      - "4567:4567"
    environment:
      - LS_ACCESS_TOKEN=your_lightstep_token  # Optional: for observability
      - LS_SERVICE_NAME=java-jsonpath-api
```

Run with:
```bash
docker-compose up
```

## Deployment

### DigitalOcean App Platform

This project includes configuration for DigitalOcean App Platform deployment in `deploy/app.yaml`. 

1. Fork this repository
2. Connect your GitHub account to DigitalOcean App Platform
3. Create a new app and select this repository
4. The deployment will use the included `app.yaml` configuration

### Other Cloud Platforms

The Docker image can be deployed on any container platform:

- **Heroku**: Use the Container Registry
- **AWS ECS/Fargate**: Deploy the Docker image
- **Google Cloud Run**: Deploy as a serverless container
- **Azure Container Instances**: Run the container image

## Development

### Project Structure

```
├── src/main/java/jsonpath/
│   ├── App.java              # Main application class
│   └── JsonTransformer.java  # JSON response transformer
├── deploy/
│   └── app.yaml             # DigitalOcean deployment config
├── Dockerfile               # Container configuration
└── pom.xml                  # Maven dependencies
```

### Running in Development

```bash
# Run with Maven
mvn exec:java -Dexec.mainClass="jsonpath.App"

# Or compile and run
mvn compile
java -cp target/classes:$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q) jsonpath.App
```

### Dependencies

- **SparkJava**: Micro web framework
- **Jayway JSONPath**: JSONPath implementation
- **Gson**: JSON serialization
- **SLF4J**: Logging framework
- **Zalando Problem**: RFC 7807 problem details

## Error Handling

The API returns appropriate HTTP status codes and error messages:

- **400 Bad Request**: Invalid JSON, missing required fields, or malformed requests
- **200 OK**: Successful query execution

Example error response:
```json
{
  "title": "Bad Request",
  "status": 400,
  "detail": "Field 'json_str' is mandatory"
}
```

## Performance Considerations

- The service parses JSON for each request - consider caching for frequently queried documents
- Large JSON documents may impact response times
- For high-throughput scenarios, consider running multiple instances behind a load balancer

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Observability

The application includes OpenTelemetry instrumentation for distributed tracing when the `LS_ACCESS_TOKEN` environment variable is set. This is particularly useful in production deployments for monitoring and debugging.

## Support

If you encounter any issues or have questions, please [open an issue](https://github.com/jjst/java-jsonpath-api/issues) on GitHub.