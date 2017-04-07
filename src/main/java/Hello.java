import java.sql.*;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;

public class Hello {

    public static void main(String args[]) {
        String host = System.getenv("host");
        String db = System.getenv("db");
        String user = System.getenv("user");
        String password = System.getenv("password");
        String accessKey = System.getenv("accessKey");
        String secretKey = System.getenv("secretKey");
        String region = System.getenv("region");
        String queueName = System.getenv("queueName");

        // call the procedure
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://"+host+":3306/"+db, user, password);
            CallableStatement cs = null;
            System.out.println("Connection done");
            cs = con.prepareCall("{call upc_refresh_upcs}");
            cs.executeQuery();
            con.close();
            System.out.println("Proc ran");
        } catch(Exception e) {
            System.out.println("here " + e.getMessage());
        }

        // sqs
        BasicAWSCredentials credentials = new   BasicAWSCredentials(accessKey, secretKey);
        AmazonSQSClient sqs = new AmazonSQSClient(credentials);
        String endpoint = "sqs." + region + ".amazonaws.com";
        sqs.setEndpoint(endpoint);

        GetQueueUrlRequest request = new GetQueueUrlRequest();
        request.setQueueName(queueName);
        String queueUrl = sqs.getQueueUrl(request).getQueueUrl();
        SendMessageResult messageResult =  sqs.sendMessage(new SendMessageRequest(queueUrl, "test"));
        System.out.println(messageResult.toString());
    }
}
