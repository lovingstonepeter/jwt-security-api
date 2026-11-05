# JWT Security API

A Spring Boot 3 REST API with **stateless JWT authentication** and **role-based access control** (USER / ADMIN).

## Features
- Register and log in; login returns a signed JWT (HS256, 24h expiry)
- Passwords hashed with BCrypt; responses use DTOs so passwords are never exposed
- Server-assigned roles: public registration always creates a `ROLE_USER` (clients cannot choose their own role)
- Role-based rules: any logged-in user can read players, only `ADMIN` can create/update/delete them
- Consistent JSON errors for every case (400, 401, 403, 404, 409, 500), including errors raised inside the security filter
- Bean validation on all request bodies
- Integration tests with MockMvc

## Tech stack
Java 17, Spring Boot 3.3, Spring Security 6, Spring Data JPA, JJWT 0.12, H2 (in-memory), Lombok, Maven.

## Run it
The app needs two secrets as environment variables (there are deliberately no defaults in the repo).

**Windows PowerShell**
```powershell
$bytes = New-Object byte[] 32
[Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
$env:ADMIN_PASSWORD = "choose-a-strong-password"
mvn spring-boot:run
```
**macOS / Linux / Git Bash**
```bash
export JWT_SECRET=$(openssl rand -base64 32)
export ADMIN_PASSWORD="choose-a-strong-password"
mvn spring-boot:run
```
In IntelliJ, put the same variables in Run > Edit Configurations > Environment variables.
See `.env.example` for the full list. The API starts on `http://localhost:8080` with an in-memory H2 database (data resets on restart). An admin account is created on startup from `ADMIN_USERNAME` / `ADMIN_PASSWORD`.

## Test with Postman
Import `postman/JWT-Security-API.postman_collection.json`. In the **Login as admin** request body, use the `ADMIN_PASSWORD` you set. Run it first: its test script stores the token and every other request sends it automatically.

## Endpoints
| Method | Path | Access | Purpose |
|---|---|---|---|
| POST | `/api/auth/register` | public | Create an account (role USER) |
| POST | `/api/auth/login` | public | Get a JWT |
| GET | `/api/users/me` | logged in | Own profile |
| PUT | `/api/users/me/password` | logged in | Change own password |
| GET | `/api/users` | ADMIN | List users |
| DELETE | `/api/users/{id}` | ADMIN | Delete a user |
| GET | `/api/players`, `/api/players/{id}` | logged in | Read players |
| POST | `/api/players` | ADMIN | Create player |
| PUT | `/api/players/{id}` | ADMIN | Update player |
| DELETE | `/api/players/{id}` | ADMIN | Delete player |

Send the token as `Authorization: Bearer <token>`.

### Example
```bash
curl -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"<your ADMIN_PASSWORD>"}'
```

## Configuration
| Env variable | Required | Purpose |
|---|---|---|
| `JWT_SECRET` | yes | Base64 HS256 key, at least 32 bytes |
| `ADMIN_PASSWORD` | yes | Password of the seeded admin |
| `ADMIN_USERNAME` | no (default `admin`) | Seeded admin username |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | no | Use a real database instead of H2 |

Tests use their own values from `src/test/resources/application.properties`, so `mvn test` needs no setup.

## Tests
```bash
mvn test
```
![Uploading image.png…]()



## Design notes
- Stateless sessions, CSRF disabled (Bearer tokens, no cookies)
- `JwtFilter` is registered only inside the Spring Security chain
- Custom `AuthenticationEntryPoint` / `AccessDeniedHandler` return JSON instead of empty 401/403 responses

## Possible next steps
Refresh tokens, Swagger/OpenAPI docs, PostgreSQL + Flyway migrations, Docker, rate limiting on login.
