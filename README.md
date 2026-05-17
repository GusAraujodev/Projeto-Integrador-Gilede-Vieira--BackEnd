# Livraria Gilede Vieira — Backend

Backend completo em **Java 21 + Spring Boot 3.2 + PostgreSQL** para o frontend React/Vite da Livraria Gilede Vieira.

---

## Estrutura de Pacotes

```
src/main/java/com/gilede/livraria/
├── LivrariaApplication.java
├── config/
│   └── DataSeeder.java              ← Cria usuários e livros padrão na inicialização
├── controller/
│   ├── AuthController.java
│   ├── BookController.java
│   ├── OrderController.java
│   ├── NotificationController.java
│   ├── FavoriteController.java
│   └── AdminController.java
├── domain/
│   ├── entity/
│   │   ├── User.java
│   │   ├── Book.java
│   │   ├── Review.java
│   │   ├── Order.java
│   │   ├── OrderItem.java
│   │   ├── Notification.java
│   │   └── Favorite.java
│   └── enums/
│       ├── Role.java
│       ├── OrderStatus.java
│       └── PaymentMethod.java
├── dto/
│   ├── auth/AuthDTOs.java
│   ├── book/BookDTOs.java
│   ├── order/OrderDTOs.java
│   ├── notification/NotificationDTOs.java
│   ├── favorite/FavoriteDTOs.java
│   └── admin/DashboardDTOs.java
├── exception/
│   └── GlobalExceptionHandler.java
├── mapper/
│   ├── BookMapper.java
│   └── OrderMapper.java
├── repository/
│   ├── UserRepository.java
│   ├── BookRepository.java
│   ├── OrderRepository.java
│   ├── NotificationRepository.java
│   └── FavoriteRepository.java
├── security/
│   ├── JwtService.java
│   ├── JwtAuthenticationFilter.java
│   └── SecurityConfig.java
└── service/
    ├── AuthService.java
    ├── BookService.java
    ├── OrderService.java
    ├── NotificationService.java
    ├── FavoriteService.java
    └── DashboardService.java
```

---

## Pré-requisitos

- Java 21+
- Maven 3.9+
- PostgreSQL 15+

---

## Setup

### 1. Criar banco de dados

```sql
CREATE DATABASE livraria_gilede;
```

### 2. Configurar credenciais

Edite `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/livraria_gilede
    username: SEU_USUARIO
    password: SUA_SENHA
```

### 3. Rodar

```bash
mvn spring-boot:run
```

O `DataSeeder` criará automaticamente:
- **Admin:** `livrariagiledevieira@gmail.com` / `admin123`
- **Cliente:** `giovani.vieira@email.com` / `cliente123`
- 4 livros de exemplo

---

## Endpoints

### Auth
| Método | Endpoint     | Acesso  |
|--------|-------------|---------|
| POST   | /auth/login | Público |
| POST   | /auth/logout| Público |
| GET    | /auth/me    | JWT     |

**Login response:**
```json
{
  "token": "eyJ...",
  "user": { "id": "uuid", "name": "...", "email": "...", "role": "admin" }
}
```

### Books
| Método | Endpoint                  | Acesso  |
|--------|--------------------------|---------|
| GET    | /books?search=termo      | Público |
| GET    | /books/{id}              | Público |
| GET    | /books/category/{cat}    | Público |
| POST   | /books                   | ADMIN   |
| PUT    | /books/{id}              | ADMIN   |
| DELETE | /books/{id}              | ADMIN   |
| PATCH  | /books/{id}/status       | ADMIN   |
| PATCH  | /books/{id}/stock        | ADMIN   |
| POST   | /books/sync-ml           | ADMIN   |
| GET    | /admin/books             | ADMIN   |

### Orders
| Método | Endpoint                  | Acesso      |
|--------|--------------------------|-------------|
| GET    | /orders                  | ADMIN       |
| GET    | /orders/{id}             | Autenticado |
| GET    | /orders/user/{userId}    | Autenticado |
| POST   | /orders                  | Autenticado |
| PATCH  | /orders/{id}/status      | ADMIN       |

### Notifications
| Método | Endpoint                        | Acesso      |
|--------|---------------------------------|-------------|
| GET    | /notifications/user/{userId}   | Autenticado |
| PATCH  | /notifications/{id}/read       | Autenticado |
| PATCH  | /notifications/read-all/{uid}  | Autenticado |

### Favorites
| Método | Endpoint                      | Acesso      |
|--------|------------------------------|-------------|
| GET    | /favorites/user/{userId}     | Autenticado |
| POST   | /favorites                   | Autenticado |
| DELETE | /favorites/{userId}/{bookId} | Autenticado |

### Admin Dashboard
| Método | Endpoint                              | Acesso |
|--------|--------------------------------------|--------|
| GET    | /admin/dashboard/stats               | ADMIN  |
| GET    | /admin/dashboard/recent-orders       | ADMIN  |
| GET    | /admin/dashboard/best-selling-books  | ADMIN  |
| GET    | /admin/dashboard/alerts              | ADMIN  |

---

## Autenticação no Frontend

Após o login, armazene o token e envie em todas as requisições protegidas:

```
Authorization: Bearer <token>
```

---

## Carrinho

O carrinho **permanece no frontend** (localStorage / contexto React).  
Ele só vai ao backend no momento do `POST /orders` (checkout).  
Isso é intencional: evita complexidade de sessão e é compatível com o contexto atual.

---

## Notas de Produção

- Troque `ddl-auto: update` por `validate` e use **Flyway** para migrações controladas.
- Gere um `app.jwt.secret` de no mínimo 256 bits aleatórios.
- Configure variáveis de ambiente para credenciais sensíveis.
- Adicione o domínio de produção da Vercel em `app.cors.allowed-origins`.
