package com.derek.example.authdemo;

import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.GenerateDataKeyRequest;
import com.amazonaws.services.kms.model.GenerateDataKeyResult;
import org.apache.log4j.Logger;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.List;

public class Main {

    private static Logger logger = Logger.getLogger(Main.class.getName());

    public static void performKMS(String keyId, String keySpec){

        AWSKMS kmsClient = AWSKMSClientBuilder.standard()
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .withRegion(Regions.AP_NORTHEAST_1)
                .build();
        // Generate a data key

        GenerateDataKeyRequest dataKeyRequest = new GenerateDataKeyRequest();
        dataKeyRequest.setKeyId(keyId);
        dataKeyRequest.withKeySpec(keySpec);
        //dataKeyRequest.setKeySpec(keySpec);

        GenerateDataKeyResult dataKeyResult = kmsClient.generateDataKey(dataKeyRequest);

        ByteBuffer plaintextKey = dataKeyResult.getPlaintext();

        ByteBuffer encryptedKey = dataKeyResult.getCiphertextBlob();

        System.out.printf(
                "Successfully generated an encrypted data key: %s%n",
                Base64.getEncoder().encodeToString(encryptedKey.array())
        );

        System.out.printf(
                "Successfully generated an plantext data key: %s%n",
                Base64.getEncoder().encodeToString(plaintextKey.array())
        );

        DecryptRequest req = new DecryptRequest().withCiphertextBlob(encryptedKey);
        ByteBuffer plainText = kmsClient.decrypt(req).getPlaintext();

        System.out.printf(
                "Successfully decrypt data key with kms: %s%n",
                Base64.getEncoder().encodeToString(plainText.array())
        );

    }

    public static void performS3(){
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .withRegion(Regions.AP_NORTHEAST_1)
                .build();

        try {
            System.out.println("Try to list all bucket.");

            List<Bucket> buckets = s3Client.listBuckets();
            for (Bucket b : buckets) {
                System.out.println(b.getName());
            }
            System.out.println("Bucket list finished.");
        }catch(AmazonS3Exception e){
            System.out.println(e.getErrorMessage());
        }
    }

    public static void performEC2(){

        AmazonEC2 ec2Client = AmazonEC2ClientBuilder.standard()
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .withRegion(Regions.AP_NORTHEAST_1)
                .build();

        try{
            System.out.println("Try to list all EC2.");

            boolean done = false;

            DescribeInstancesRequest request = new DescribeInstancesRequest();
            while(!done) {
                DescribeInstancesResult response = ec2Client.describeInstances(request);

                for (Reservation reservation : response.getReservations()) {
                    for (Instance instance : reservation.getInstances()) {
                        System.out.printf(
                                "Found instance with id %s, " +
                                        "AMI %s, " +
                                        "type %s, " +
                                        "state %s " +
                                        "and monitoring state %s",
                                instance.getInstanceId(),
                                instance.getImageId(),
                                instance.getInstanceType(),
                                instance.getState().getName(),
                                instance.getMonitoring().getState());
                        System.out.println();
                    }
                }

                request.setNextToken(response.getNextToken());

                if (response.getNextToken() == null) {
                    done = true;
                }
            }

            System.out.println("EC2 list finished.");

        }catch(AmazonEC2Exception e){
            System.out.println(e.getErrorMessage());
        }
    }
    public static void main(String[] args){
        logger.info("This is message in logger");

        System.out.println("This is demonstration to show how to use java in containers.");

        performS3();
        performEC2();
        performKMS("df506d6f-b87b-41eb-9c59-1fb8dd0e52a7","AES_256");

    }

}
