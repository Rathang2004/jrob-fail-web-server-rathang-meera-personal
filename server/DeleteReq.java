package server;
import java.io.*;
import server.config.MimeTypes;

public class DeleteReq 
{  
    int statusCode;
    String statusPhrase;
    String EOL = "\r\n";
    StringBuilder responseBuilder;

    public String getResponse(String firstLine, WebServer webServer) throws IOException 
    {
        this.responseBuilder = new StringBuilder(); 
        String[] allWords = firstLine.split(" ");
        String httpVersion = allWords[2];
        String filePath = webServer.getDocumentRoot() + allWords[1];
        filePath = webServer.getDocumentRoot().replaceAll("/+$", "") + "/" + allWords[1].replaceAll("^/+",""); // Remove trailing slashes from root and leading from path

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
            if(newFile.delete() == false)
            {
                statusCode = 500;
                statusPhrase = "Error Response";
                filePath = webServer.getDocumentRoot() + "/errorResponse.html";
                newFile = new File(filePath);
                System.out.println("file couldn't be deleted: " + filePath);
            }
            else
            {
                statusCode = 204;
                statusPhrase = "No Content";
                filePath = webServer.getDocumentRoot() + "/noContentResponse.html";
                newFile = new File(filePath);
                System.out.println("file was deleted: " + filePath);
            }
        }

        responseBuilder.append(statusCode).append(" ").append(statusPhrase).append(EOL);
        responseBuilder.append("Content-Length: ").append(newFile.length()).append(EOL);

        String contentType = "text/html"; 
        if (statusCode != 404) 
        {
            String expression = allWords[1];
            String ending = expression.split("\\.")[1];
            MimeTypes mime = MimeTypes.getDefault();
            contentType = mime.getMimeTypeFromExtension(ending);
        }
        responseBuilder.append("Content-Type: " + contentType + EOL);
        java.util.Date date = new java.util.Date();
        responseBuilder.append("Date: " + date + EOL);

        responseBuilder.append(EOL);
        try (BufferedReader br = new BufferedReader(new FileReader(newFile))) 
        {
            String eachLine;
            while ((eachLine = br.readLine()) != null) 
            {
                responseBuilder.append(eachLine).append(EOL);
            }
        }
        return responseBuilder.toString();
    }
}