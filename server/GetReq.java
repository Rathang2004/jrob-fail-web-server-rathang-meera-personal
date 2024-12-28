package server;

import java.io.*;
import server.config.MimeTypes;

public class GetReq {
    private static final String EOL = "\r\n";
    private OutputStream outputStream;

    public GetReq(OutputStream outputStream) 
    {
        this.outputStream = outputStream; 
    }
    public String getResponse(String firstLine, WebServer webServer) throws IOException {
        String[] allWords = firstLine.split(" ");
        
        if (allWords.length < 3) 
        {
            return "HTTP/1.1 400 Bad Request\r\n\r\n";
        }
    
        String httpVersion = allWords[2];
        String file = allWords[1].startsWith("/") ? allWords[1].substring(1) : allWords[1];
        String filePath = webServer.getDocumentRoot() + file; 
        File newFile = new File(filePath);
    
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append(httpVersion).append(" ");
    
        if (!newFile.exists()) 
        {
            responseBuilder.append("404 Not Found" + EOL);
            filePath = "/Users/rathangpandit/Desktop/jrob-fail-web-server-rathang-meera/server/webRoot/notFound.html";
            newFile = new File(filePath);
        } else 
        {
            responseBuilder.append("200 OK").append(EOL);
        }
    
        long contentLength = newFile.length();
        responseBuilder.append("Content-Length: " + contentLength + EOL);
    
        String contentType = "";
        String[] extensionArray  = file.split("\\.");
        String extension = null;
        if(extensionArray.length > 1)
        {
            extension = extensionArray[1];
        }
        if (!extension.isEmpty()) 
        {
            MimeTypes mime = MimeTypes.getDefault();
            contentType = mime.getMimeTypeFromExtension(extension);
        } else
        {
            contentType = "application/octet-stream"; 
        }
        
        responseBuilder.append("Content-Type: ").append(contentType).append(EOL);
        responseBuilder.append("Date: ").append(new java.util.Date()).append(EOL);
        responseBuilder.append(EOL);
    
        outputStream.write(responseBuilder.toString().getBytes());
    
        try (FileInputStream fileInputStream = new FileInputStream(newFile);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream)) {
             
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                bufferedOutputStream.write(buffer, 0, bytesRead);
            }
            bufferedOutputStream.flush();
        }
    
        return responseBuilder.toString();
    }
}
