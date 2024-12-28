package server;

import java.io.*;
import java.net.Socket;
import java.util.Base64;

public class MultiThreading implements Runnable 
{
    private final Socket socket;
    private final WebServer webServer;

    public MultiThreading(Socket socket, WebServer webServer) 
    {
        this.socket = socket;
        this.webServer = webServer;
    }

    public void run() {
        try (
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream));
        ) {
            StringBuilder httpRequestBuilder = new StringBuilder();
            String httpRequest;

            // Read HTTP request lines
            while ((httpRequest = buffer.readLine()) != null && !httpRequest.isEmpty()) {
                httpRequestBuilder.append(httpRequest).append("\n");
            }

            String fullHttpRequest = httpRequestBuilder.toString();
            System.out.println("HttpRequest: " + fullHttpRequest);

            String[] wordsArray = fullHttpRequest.split(" ");
            if (wordsArray.length < 3) {
                sendErrorResponse(outputStream, 400, "Bad Request: Malformed HTTP request.");
                return;
            }

            String requestType = wordsArray[0];
            String resource = wordsArray[1].startsWith("/") ? wordsArray[1].substring(1) : wordsArray[1];

            File file = new File(webServer.getDocumentRoot(), resource);
            File passwordFile = new File(file.getParent(), ".password");

            String authorizationHeader = getAuthorizationHeader(fullHttpRequest);

            // Authorization logic
            if (passwordFile.exists()) {
                if (authorizationHeader == null) {
                    sendUnauthorizedResponse(outputStream);
                    return;
                } else {
                    if (!isAuthorized(authorizationHeader, passwordFile)) {
                        sendErrorResponse(outputStream, 403, "Forbidden: Invalid credentials.");
                        return;
                    }
                }
            }

            // Process request type
            String response = "";
            switch (requestType) {
                case "GET":
                    GetReq getReq = new GetReq(outputStream);
                    response = getReq.getResponse(wordsArray[0] + " " + resource + " " + wordsArray[2], webServer);
                    break;
                case "HEAD":
                    HeadReq headReq = new HeadReq();
                    response = headReq.getResponse(wordsArray[0] + " " + resource + " " + wordsArray[2], webServer);
                    break;
                case "PUT":
                    PutReq putReq = new PutReq();
                    response = putReq.getResponse(wordsArray[0] + " " + resource + " " + wordsArray[2], webServer, socket);
                    break;
                case "DELETE":
                    DeleteReq deleteReq = new DeleteReq();
                    response = deleteReq.getResponse(wordsArray[0] + " " + resource + " " + wordsArray[2], webServer);
                    break;
                default:
                    sendErrorResponse(outputStream, 501, "Not Implemented: The method is not supported.");
                    return;
            }

            // Send response
            outputStream.write(response.getBytes());
            outputStream.flush();
            System.out.println("Response: " + response);

        } catch (IOException e) {
            System.err.println("Error processing request: " + e.getMessage());
        } finally {
            try {
                socket.close(); 
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    private String getAuthorizationHeader(String httpRequest) {
        for (String line : httpRequest.split("\n")) {
            if (line.startsWith("Authorization:")) {
                return line.substring("Authorization: ".length()).trim();
            }
        }
        return null;
    }

    private boolean isAuthorized(String authorizationHeader, File passwordFile) {
        String encodedCredentials = authorizationHeader.replace("Basic ", "").trim();
        String decodedCredentials = new String(Base64.getDecoder().decode(encodedCredentials));

        try (BufferedReader reader = new BufferedReader(new FileReader(passwordFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().equals(decodedCredentials)) {
                    return true; 
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading .password file: " + e.getMessage());
        }
        return false; 
    }

    private void sendUnauthorizedResponse(OutputStream outputStream) {
        String response = "HTTP/1.1 401 Unauthorized\r\n" +
                          "WWW-Authenticate: Basic realm=\"667 Server\"\r\n" +
                          "Content-Length: 0\r\n\r\n";
        try {
            outputStream.write(response.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            System.err.println("Error sending 401 response: " + e.getMessage());
        }
    }

    private void sendErrorResponse(OutputStream outputStream, int statusCode, String message) {
        String response = "HTTP/1.1 " + "404" + " " + "Bad Request" + "\r\n" +
                          "Content-Type: text/html\r\n" +
                          "Content-Length: " + getResponse().length() + "\r\n" +
                          "\r\n" + getResponse();
        try 
        {
            outputStream.write(response.getBytes());
            outputStream.flush();
        } 
        catch (IOException e) 
        {
            System.err.println("Error sending error response: " + e.getMessage());
        }
    }

    public String getResponse()
    {
        return "<!DOCTYPE html>" +
               "<html>" + "<head>" +
               "<title>404</title>" +
               "</head>" +
               "<body>" + "<h1>Error</h1>" +
               "<p1>" + "404 Not Found" + "</p1>" +
               "</body>" +
               "</html>";
    }
}

