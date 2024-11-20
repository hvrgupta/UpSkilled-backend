# **Upskilled Backend**
This backend service for UpSkilled is developed using Java and the Spring Framework. It provides APIs to manage courses, assignments, course material, submissions, gradebooks and user interactions.

## Prerequisites
<p>JDK: Version 17 or later</p>
<p>Java: Version 17</p>
<p>Spring Boot: Version 3.3.4</p>
<p>Maven: Version 3.9.9 or later</p>
<p>MySQL: Version 8.0 or later</p>

## Project Structure
<p>src/main/: Contains the main Java source code for the application.</p>
<p>src/test/: Contains unit tests for the application.</p>
<p>src/main/resources: Contains configuration files such as application.properties</p>

## Configuration Setup
<h3>MySQL Database Configuration</h3>
<p>Configure the MySQL database by adding the following properties to the <code>application.properties</code> file:</p>

<pre>
spring.datasource.url=jdbc:mysql://&lt;host&gt;:&lt;port&gt;/&lt;database-name&gt;
spring.datasource.username=&lt;your-database-username&gt;
spring.datasource.password=&lt;your-database-password&gt;
</pre>

<p>Replace <code>&lt;host&gt;</code>, <code>&lt;port&gt;</code>, <code>&lt;database-name&gt;</code>, <code>&lt;your-database-username&gt;</code>, and <code>&lt;your-database-password&gt;</code> with your actual database connection details.</p>

### Configure the JWT secret in the application.properties file:
<ol>
  <li>Generate a 256-bit encrypted secret for JWT.</li>
  <li>Add the following property to your <code>application.properties</code> file:</li>
</ol>
<pre>
jwt.secret=&lt;your-256-bit-encrypted-secret&gt;
</pre>
<h3>AWS S3 Configuration for UpSkilled</h3>
<p>UpSkilled uses AWS S3 to store course-related data, including syllabi, course materials, and assignments. To set this up, follow these steps:</p>
<ol>
  <li>
    <strong>Generate AWS Access and Secret Keys:</strong>
    <ul>
      <li>Log in to your AWS account.</li>
      <li>Navigate to the IAM service to create an access key and secret key for your application.</li>
    </ul>
  </li>
  <li>
    <strong>Create Unique Buckets for Data Storage:</strong>
    <ul>
      <li>Since bucket names must be unique across AWS, create three separate buckets in the <code>us-east-1</code> region for:</li>
      <ul>
        <li>Syllabus</li>
        <li>Course materials</li>
        <li>Assignments</li>
      </ul>
    </ul>
  </li>
  <li>
    <strong>Configure Application Properties:</strong>
    <p>Add the following properties in the <code>application.properties</code> file of the application:</p>
    <pre>
aws.s3.accessKey=&lt;your-AWS-access-key&gt;
aws.s3.secretKey=&lt;your-AWS-secret-key&gt;
aws.s3.bucketName=&lt;syllabus-bucket-name&gt;
aws.s3.course-materials-bucketName=&lt;course-materials-bucket-name&gt;
aws.s3.assignment-bucketName=&lt;assignments-bucket-name&gt;</pre>
  </li>
</ol>

## Building the Project
<p>To build the project and package it into a JAR file, use the following command in the project directory:</p>

```bash
mvn clean package -DskipTests=true
```
The JAR file will be created in the target directory.

## Running the Application
<p>Set up the database:</p>
<p>Ensure MySQL is running.</p>
<p>Update database credentials in src/main/resources/application.properties.</p>
<p>Run the JAR file: After building, execute the following command:</p>

```bash
java -jar target/upskilled-0.0.1-SNAPSHOT.jar
```
Access the application APIs at http://localhost:8080
                                            
## Testing the Application
<p>To run the unit tests, run the following command in the project directory: </p>

```bash
mvn test
```

## Further Help
<p>For additional help and documentation:</p>
<p>Maven documentation: https://maven.apache.org/guides/index.html</p>
<p>Spring Boot documentation: https://spring.io/guides</p>
