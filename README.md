# Payment Gateway Service

## PREREQUISITES
- Docker
- Java 17+ (Jika tidak menggunakan Docker)
- Maven 3.8+ (Jika tidak menggunakan Docker)
- PostgreSQL (Jika tidak menggunakan Docker)

## PROJECT SETUP
1. Clone repository
    ```bash
    git clone https://github.com/fardanaljihad/payment-gateway-cip.git
    cd payment-gateway-cip
    ```

2. Salin konfigurasi
    ```bash
    cp src/main/resources/application.yaml.example src/main/resources/application.yaml
    cp Dockerfile.example Dockerfile
    cp docker-compose.yaml.example docker-compose.yaml
    ```

3. Pastikan docker sudah berjalan lalu buat docker network dengan perintah berikut.
    ```bash
    docker network create payment-gateway-cip
    ```

## RUN INSTRUCTIONS
1. Jalankan project menggunakan Docker Compose
    ```bash
    docker compose up -d
    ```

2. Cek status container
    ```bash
    docker ps
    ```

    Pastikan container dengan nama berikut ada dan berjalan tanpa error.
    - `payment-gateway-cip` port: (8080:8080)
    - `keycloak` port: (8443:8443)
    - `payment-gateway-db` port: (5432:5432)
    - `kafka` port: (9092:9092)
    - `kafka-zookeeper` port: (2181:2181)

3. Setup Keycloak
    - Akses halaman admin keycloak `http://localhost:8443` melalui browser. Jika tidak merespons, tunggu beberapa saat lalu refresh.
    - Login menggunakan `username: admin` dan `password: admin`.
    - Klik tab `Manage realms` lalu pilih `Create realm`.
    - Upload file `realm-export.json`, pastikan kolom Realm name berisi `oauth2-api-gateway`, dan pastikan juga Enabled `ON`.

4. Clone repository external services untuk simulasi komunikasi dengan Core Banking System dan Biller Aggregator.
    ```bash
    cd ..
    git clone https://github.com/fardanaljihad/external-services.git
    cd external-services
    ```

    Jalankan project external services dengan perintah.
    ```bash
    docker compose up -d
    ```

    Pastikan container dengan nama berikut ada dan berjalan tanpa error.
    - `core-banking` port: (8081:8081)
    - `biller` port: (8082:8082)

5. Dapatkan akses token.
    ```bash
    docker exec -it payment-gateway-cip curl -X POST "http://keycloak:8443/realms/oauth2-api-gateway/protocol/openid-connect/token" \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -d "grant_type=password" \
        -d "client_id=payment-gateway" \
        -d "username=testuser" \
        -d "password=secret123"
    ```
    > Catatan: akses token hanya valid selama **300s**.

6. Buka dokumentasi API
    ```bash
    http://localhost:8080/swagger-ui/index.html
    ```

7. Inputkan akses token pada bagian `Authorize` lalu klik `Authorize`

8. Jalankan API POST /api/payments.

    Request body:
    ```json
    {
        "orderId": "INV-12345",
        "channel": "MOBILE_BANKING",
        "amount": 25000,
        "account": "1234567890",
        "currency": "IDR",
        "paymentMethod": "VIRTUAL_ACCOUNT"
    }
    ```

    Response Body Success:
    ```json
    {
        "transactionId": "9d6d09c2-40e6-4bd1-ac16-64a489bc99ed",
        "orderId": "INV-12345",
        "status": "SUCCESS",
        "corebankReference": "CB1762420369535",
        "billerReference": "BILLER1762420369722",
        "message": null
    }
    ```

    ---

    Response Body Failed (Biller down):
    ```json
    {
        "transactionId": "cb2d2231-627c-4047-9d58-21b31609bf02",
        "orderId": "INV-99999",
        "status": "FAILED",
        "corebankReference": null,
        "billerReference": null,
        "message": "Biller is currently unavailable."
    }
    ```
    Response di atas akan didapatkan jika container `biller` distop (simulasi jika server Biller down).

    ---

    Response Body Failed (Insufficient balance):
    ```json
    {
        "transactionId": "b1769744-09a6-4f8d-be2a-32d9d53ccd1f",
        "orderId": "INV-12346",
        "status": "FAILED",
        "corebankReference": null,
        "billerReference": null,
        "message": "Insufficient balance"
    }
    ```
    Response di atas akan didapatkan jika nilai `amount: 125000` karena untuk simulasi pada Core Banking System saldo yang tersedia hanya `100000` dan saldo tersebut bersifat statis.

    ---

    Response Body Failed (Order ID kosong)
    ```json
    {
        "transactionId": null,
        "orderId": null,
        "status": "FAILED",
        "corebankReference": null,
        "billerReference": null,
        "message": "orderId: Order ID is required"
    }
    ```

    ---

    Response Body Failed (Channel tidak sesuai):
    ```json
    {
        "transactionId": null,
        "orderId": null,
        "status": "FAILED",
        "corebankReference": null,
        "billerReference": null,
        "message": "Invalid channel value: MBANKING. Accepted values: [MOBILE_BANKING, INTERNET_BANKING, ATM]"
    }
    ```

    ---

    Response Body Failed (Amount tidak valid <= 0)
    ```json
    {
        "transactionId": null,
        "orderId": null,
        "status": "FAILED",
        "corebankReference": null,
        "billerReference": null,
        "message": "amount: Amount must be greater than 0"
    }
    ```

    ---

9. Jalankan API GET /api/payments/{id}

    Parameters:
    - id: UUID

    Response Body Success:
    ```json
    {
        "transactionId": "ff2037e9-9829-437f-a766-fff740cce91e",
        "orderId": "INV-98765",
        "status": "SUCCESS",
        "corebankReference": "CB1762422059968",
        "billerReference": "BILLER1762422060100",
        "message": null
    }
    ```

    Response Body Failed (Transaction Not Found):
    ```json
    {
        "transactionId": null,
        "orderId": null,
        "status": null,
        "corebankReference": null,
        "billerReference": null,
        "message": "Transaction not found"
    }
    ```
