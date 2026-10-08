package handler;

/**
 * The server handler classes serve as a translator between HTTP and Java.
 * Your handlers will convert an HTTP request into Java usable objects & data.
 * The handler then calls the appropriate service.
 * When the service responds, the handler converts the response object back to JSON and sends the HTTP response.
 * This could include converting thrown exception types into the appropriate HTTP status codes if necessary.
 */
public class Handler {
    // Method that takes an HTTP request (a JSON object)
    // this method turns the JSON object into usable objects and data
    // this method calls UserService, GameService, or AuthService as needed

    // Method that takes a JSON object
    // this method turns that JSON object back into JSON
    // this method returns the HTTP response and JSON object
}
