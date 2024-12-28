package server;

import java.io.*;
import server.config.MimeTypes;
import java.util.Date;

public class HeadReq 
{
    int statusCode;
    String statusPhrase;
    String EOF = "\r\n";
    StringBuilder responseBuilder;

    public String getResponse(String firstLine, WebServer webServer) throws IOException 
    {
        this.responseBuilder = new StringBuilder(); 
        String[] allWords = firstLine.split(" ");
        String httpVersion = allWords[2];
        String filePath = webServer.getDocumentRoot() + allWords[1]; 

        File newFile = new File(filePath);
        
        responseBuilder.append(httpVersion + " ");


        if (!newFile.exists()) 
        {
            statusCode = 404;
            statusPhrase = "Not Found";
            filePath = webServer.getDocumentRoot() + "/notFound.html"; 
            newFile = new File(filePath);
            System.out.println("Not found file path: " + filePath);
        } 
        else 
        {
            statusCode = 200;
            statusPhrase = "OK";
        }

        responseBuilder.append(statusCode + " " + statusPhrase + EOF);
        
        long contentLength = newFile.length();
        responseBuilder.append("Content-Length: " + contentLength + EOF);
        

        String contentType = " "; 
        if (statusCode == 200) 
        {
            String ending = allWords[1].substring(allWords[1].lastIndexOf('.') + 1);
            MimeTypes mime = MimeTypes.getDefault();
            contentType = mime.getMimeTypeFromExtension(ending);
        }
        
        responseBuilder.append("Content-Type: " + contentType + EOF);
        
        Date date = new Date();
        responseBuilder.append("Date: ").append(date.toString()).append(EOF);
        
        responseBuilder.append(EOF);
        
        return responseBuilder.toString();
    }
}
