package server;

import java.io.*;
import server.config.MimeTypes;
import java.util.Date;

public class PutReq<Socket> {

    private int statusCode;
    private String statusPhrase;
    private final String EOF = "\r\n";
    private StringBuilder responseBuilder;

    public String getResponse(String firstLine, WebServer webServer, Socket socket) throws IOException {
        this.responseBuilder = new StringBuilder();
        String[] allWords = firstLine.split(" ");
        String httpVersion = allWords[2];
        String filePath = webServer.getDocumentRoot() + "/" + allWords[1]; 

        File newFile = new File(filePath);
        responseBuilder.append(httpVersion).append(" ");

        if (!newFile.exists()) {
            if (!newFile.createNewFile()) {
                statusCode = 500;
                statusPhrase = "Error Response";
                filePath = webServer.getDocumentRoot() + "/errorResponse.html";
                newFile = new File(filePath);
            } else {
                statusCode = 201;
                statusPhrase = "Created Response";
            }
        } else {
            statusCode = 201;
            statusPhrase = "Modified Response";
        }

        responseBuilder.append(statusCode).append(" ").append(statusPhrase).append(EOF);


        InputStream inputStream = ((java.net.Socket) socket).getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String headerLine;
        int contentLength = -1;


        while (!(headerLine = reader.readLine()).isEmpty()) {
            if (headerLine.startsWith("Content-Length:")) {
                contentLength = Integer.parseInt(headerLine.split(":")[1].trim());
            }
        }

        if (contentLength > 0) {
            byte[] buffer = new byte[4096];
            int totalBytesRead = 0;
            try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(newFile))) {
                int bytesRead;
                while (totalBytesRead < contentLength && (bytesRead = inputStream.read(buffer)) != -1) {
                    bufferedOutputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                }
                bufferedOutputStream.flush();
            }
            System.out.println("Total bytes written to file: " + totalBytesRead);
        } else {
            System.out.println("Invalid or missing Content-Length header");
            statusCode = 411; 
            statusPhrase = "Length Required";
        }

        responseBuilder.append("Content-Length: ").append(newFile.length()).append(EOF);

        String contentType = " "; 
        if (statusCode != 404) {
            String expression = allWords[1];
            String ending = expression.split("\\.")[1];
            MimeTypes mime = MimeTypes.getDefault();
            contentType = mime.getMimeTypeFromExtension(ending);
        }
        responseBuilder.append("Content-Type: ").append(contentType).append(EOF);
        responseBuilder.append("Date: ").append(new Date()).append(EOF);

        responseBuilder.append(EOF);
        return responseBuilder.toString();
    }
}