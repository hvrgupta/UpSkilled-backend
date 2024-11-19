# **Upskilled Backend**
This backend service for UpSkilled is developed using Java and the Spring Framework. It provides APIs to manage courses, assignments, course material, submissions, gradebooks and user interactions.

## Prerequisites
<p>JDK: Version 17 or later</p>
<p>Java: Version 17</p>
<p>Spring Boot: Version 3.3.4</p>
<p>Maven: Version 3.9.9 or later</p>
<p>MySQL: Version 8.0 or later</p>

## Project Structure
<p>src/main/java: Contains the main Java source code for the application.</p>
<p>src/main/resources: Contains configuration files such as application.properties</p>

## Building the Project
<p>To build the project and package it into a JAR file, use the following command in the project directory:</p>

```bash
mvn package -DskipTests=true
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
