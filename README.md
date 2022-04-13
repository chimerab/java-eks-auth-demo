# java-eks-auth-demo

This project demostrate how to use service account with java in EKS. 

Before you get started, Please read the aws java sdk doc to understanding how java sdk to perform the authticate 
https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html
https://docs.aws.amazon.com/zh_cn/eks/latest/userguide/iam-roles-for-service-accounts-minimum-sdk.html

Make sure you have configure your EKS cluster and service account ready. If not, please follow below link
https://www.eksworkshop.com/beginner/110_irsa/
https://docs.aws.amazon.com/AmazonECR/latest/userguide/docker-push-ecr-image.html

1. clone the project to your local directory.
2. Edit pom.xml, change <repository>xxxxx.dkr.ecr.ap-northeast-1.amazonaws.com/javaexample</repository> to your own.
3. aws ecr get-login-password --region <region> | docker login --username AWS --password-stdin <aws_account_id>.dkr.ecr.<region>.amazonaws.com
4. mvn clean package dockerfile:push
5. Edit autodemojob.yaml , replace the image with the new one.
6. kubectl apply -f autodemojob.yaml
7. pods=$(kubectl get pods --selector=job-name=auth-demo --output=jsonpath='{.items[*].metadata.name}')
8. kubectl logs $pods

Note:  
  In case you are using private EKS cluster, you might need add below annotation to autodemojob.yaml to make sure the sts request goto regional endpoint.  
  
  > eks.amazonaws.com/sts-regional-endpoints: "true"
  
  
From the debug output if you see below similar output means the java apps have authticate with service account successful. 

DEBUG com.amazonaws.request -  Sending Request: POST https://sts.amazonaws.com / Parameters: ({"Action":["AssumeRoleWithWebIdentity"],"Version":["2011-06-15"],"RoleArn":["arn:aws:iam::xxxxxxx:role/eksctl-addon-iamserviceaccount"]
...
  
DEBUG com.amazonaws.auth.AWSCredentialsProviderChain -  Loading credentials from WebIdentityTokenCredentialsProvider

From the CloudTrail->Event history, search "AssumeRoleWithWebIdentity" as Event Name you should find the entry with 
 "system:serviceaccount:default:[service-account-name]" as User name
