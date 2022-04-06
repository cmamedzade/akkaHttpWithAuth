# akkaHttpWithAuth


This project is for the old fashion REST service. Which is providing REST API and doing CRUD operations. 
The service has no session. It uses a JWT token for auth/auth mechanism. It is not a microservice, just a REST service.
The project is written with Akka HTTP, Akka streams, Alpakka JDBC, JWT, Postgres, Scalatest, Mockito and etc. technologies.
Run run.sh file on terminal and all neccassry container will be created and ready to accept request. <br>
http://127.0.0.1:8080/getToken           ---> get jwt token for next aoperations. Token is stroed in header. Take token and put Authorization header <br>
http://0.0.0.0:8080/create-transaction   ---> creates transaction <br>
http://0.0.0.0:8080/getBalance/1         ---> gets balance  <br>


Good luck
