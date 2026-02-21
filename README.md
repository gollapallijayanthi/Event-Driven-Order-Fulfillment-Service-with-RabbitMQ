#  Event-Driven Order Fulfillment Service



A \*\*production-grade backend microservice\*\* built using \*\*Spring Boot, RabbitMQ, and MySQL\*\*, designed to demonstrate \*\*event-driven architecture (EDA)\*\* principles such as \*\*asynchronous processing, idempotency, retries, DLQs, and fault tolerance\*\*.



This service consumes `OrderPlacedEvent`s, processes orders asynchronously, updates order state in a database, and publishes `OrderProcessedEvent`s—fully decoupled and resilient by design.



---



\## 📌 Key Features



\* ✅ Event-driven, asynchronous processing

\* ✅ RabbitMQ message consumption \& publishing

\* ✅ Manual ACK / NACK handling

\* ✅ Retry logic with Dead Letter Queue (DLQ)

\* ✅ Idempotent order processing

\* ✅ Atomic database transactions

\* ✅ Structured logging with context

\* ✅ Health endpoint for monitoring

\* ✅ Docker \& Docker Compose support

\* ✅ Unit tests + Integration tests (Testcontainers)



---



\## 🏗️ Architecture Overview



```

OrderPlacedEvent

&nbsp;       │

&nbsp;       ▼

RabbitMQ (order.events exchange)

&nbsp;       │

&nbsp;       ▼

Order Fulfillment Service

&nbsp;       │

&nbsp;       ├── MySQL (orders table)

&nbsp;       │

&nbsp;       └── RabbitMQ (OrderProcessedEvent)

```



\*\*Why this architecture?\*\*



\* Loose coupling between services

\* No synchronous dependencies

\* High resilience to failures

\* Safe retries without duplicate side effects



---



\## 📂 Project Structure



```

.

├── src/

│   ├── main/

│   │   ├── java/com/example/orderprocessor/

│   │   │   ├── OrderProcessorApplication.java

│   │   │   ├── config/          # RabbitMQ \& DB config

│   │   │   ├── controller/      # /health endpoint

│   │   │   ├── service/         # Business logic \& listeners

│   │   │   ├── model/           # JPA entities \& events

│   │   │   └── repository/      # JPA repositories

│   │   └── resources/

│   │       └── application.yml

│   └── test/

│       ├── service/             # Unit tests

│       └── integration/         # Integration tests (Testcontainers)

├── db\_init/

│   └── init.sql                 # DB schema \& seed data

├── Dockerfile

├── docker-compose.yml

├── .env.example

├── pom.xml

└── README.md

```



---



\## 📬 Event Schemas



\### 🔹 OrderPlacedEvent (Incoming)



```json

{

&nbsp; "orderId": "string",

&nbsp; "productId": "string",

&nbsp; "quantity": 2,

&nbsp; "customerId": "string",

&nbsp; "timestamp": "2023-10-27T10:00:00Z"

}

```



\### 🔹 OrderProcessedEvent (Outgoing)



```json

{

&nbsp; "orderId": "string",

&nbsp; "status": "PROCESSED",

&nbsp; "processedAt": "2023-10-27T10:05:00Z"

}

```



---



\## 🧠 Idempotency Strategy



\* Orders are uniquely identified by `orderId`

\* If an order is already in `PROCESSED` state:



&nbsp; \* The event is safely ignored

&nbsp; \* No duplicate DB writes

&nbsp; \* No duplicate outgoing events



\*\*Proof (runtime log):\*\*



```

Duplicate event ignored | orderId=order-test-1

```



---



\## ❌ Error Handling \& Retries



| Scenario             | Behavior               |

| -------------------- | ---------------------- |

| Transient failure    | NACK + requeue         |

| Retry count exceeded | Reject → DLQ           |

| Permanent failure    | Reject without requeue |

| Crash safety         | Message not ACKed      |



Retry count is tracked via RabbitMQ headers.



---



\## 🗄️ Database Schema



```sql

CREATE TABLE orders (

&nbsp;   id VARCHAR(255) PRIMARY KEY,

&nbsp;   product\_id VARCHAR(255) NOT NULL,

&nbsp;   customer\_id VARCHAR(255) NOT NULL,

&nbsp;   quantity INT NOT NULL,

&nbsp;   status ENUM('PENDING','PROCESSING','PROCESSED','FAILED') NOT NULL,

&nbsp;   created\_at TIMESTAMP DEFAULT CURRENT\_TIMESTAMP,

&nbsp;   updated\_at TIMESTAMP DEFAULT CURRENT\_TIMESTAMP ON UPDATE CURRENT\_TIMESTAMP

);

```



Seed data is provided in `db\_init/init.sql`.



---



\## 🐳 Docker \& Docker Compose



\### Prerequisites



\* Docker

\* Docker Compose



\### Start Everything



```bash

docker-compose up -d

```



This starts:



\* Order Processor Service (port `8080`)

\* RabbitMQ (ports `5672`, `15672`)

\* MySQL (port `3306`)



---



\##  Health Check



```bash

curl http://localhost:8080/health

```



Response:



```json

{"status":"UP"}

```



---



\## 🧪 Testing Guide



\### ✅ Unit Tests (Default)



Unit tests run \*\*without Docker\*\*.



```bash

mvn test

```



Result:



```

BUILD SUCCESS

Tests run: 3, Failures: 0, Errors: 0

```



---



\### 🔬 Integration Tests (Optional)



Integration tests use \*\*Testcontainers\*\* (require Docker).



\#### Enable Integration Profile



```bash

mvn test -Pintegration

```



What it tests:



\* Publishes `OrderPlacedEvent`

\* Consumes message

\* Updates MySQL

\* Publishes `OrderProcessedEvent`



---



\##  RabbitMQ Manual Testing



1\. Open UI:

&nbsp;  👉 \[http://localhost:15672](http://localhost:15672)

&nbsp;  (user: `guest`, password: `guest`)



2\. Publish message:



&nbsp;  \* Exchange: `order.events`

&nbsp;  \* Routing key: `order.placed`

&nbsp;  \* Payload: `OrderPlacedEvent`



3\. Observe:



&nbsp;  \* Message consumed

&nbsp;  \* DB updated

&nbsp;  \* Logs printed

&nbsp;  \* ACK sent



---



\##  Logs You Should See



```

Received OrderPlacedEvent | orderId=order-test-1

Order processed successfully | orderId=order-test-1

Published OrderProcessedEvent | orderId=order-test-1

Duplicate event ignored | orderId=order-test-1

```



---



\##  Configuration \& Security



\* No hardcoded secrets

\* All credentials via environment variables

\* `.env.example` included for reference



---







\##  Final Notes



This project demonstrates \*\*real-world backend engineering skills\*\* required for \*\*scalable distributed systems\*\*, including:



\* Message-driven microservices

\* Fault tolerance

\* Exactly-once semantics

\* Cloud-native deployment readiness



