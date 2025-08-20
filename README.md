# DM111 — Plataforma Vale Food (Microservices)

Projeto da disciplina DM111 (Desenvolvimento de Web Services com segurança em Java no Google App Engine) com uma arquitetura baseada em **microserviços** para um marketplace de restaurantes e promoções.

## Módulos
- `vale-food-auth` — Autenticação/JWT.
- `vale-food-user-management` — CRUD de usuários e preferências.
- `vale-food-restaurant-management` — CRUD de restaurantes e produtos.
- `vale-food-promotion-management` — Busca e CRUD de promoções.

## Tecnologias
- Java 17, Spring Boot 3, Maven
- Validação (Jakarta Validation)
- JWT (assinatura RSA)
- (Opcional) Firebase/Firestore para persistência
- Perfis Spring: `local`/`test` (implementações `Firebase*RepositoryImpl` ou `Memory*RepositoryImpl`)

## Estrutura (alto nível)
```
DM111/
  vale-food-auth/
  vale-food-user-management/
  vale-food-restaurant-management/
  vale-food-promotion-management/
```

## Pré-requisitos
- JDK 17+ e Maven 3.9+
- (Opcional p/ Firestore) Credenciais de serviço e variável `GOOGLE_APPLICATION_CREDENTIALS`

## Como executar (local)
1) Clonar e compilar tudo:
```bash
mvn -q -DskipTests clean install
```
2) Subir cada serviço (em terminais separados), por exemplo:
```bash
cd vale-food-user-management && mvn spring-boot:run
cd vale-food-restaurant-management && mvn spring-boot:run
cd vale-food-promotion-management && mvn spring-boot:run
cd vale-food-auth && mvn spring-boot:run
```

> Dica: ative o perfil que quiser com `-Dspring-boot.run.profiles=local` (ou `test`).  
> `test` usa repositórios em memória; `local` usa Firestore (onde disponível).

## Configuração básica
**application.properties** (exemplo mínimo):
```
server.port=8080
spring.profiles.active=dev

# JWT (auth)
vale-food.jwt.custom.issuer=vale-food
vale-food.jwt.public-key-path=classpath:jwt/public.pem
vale-food.jwt.private-key-path=classpath:jwt/private.pem

# Firestore (quando usar)
spring.cloud.gcp.firestore.project-id=<seu-project-id>
# ou setar GOOGLE_APPLICATION_CREDENTIALS apontando para o JSON da conta de serviço
```
> Gere um par RSA para o JWT (ex.: `openssl genrsa -out private.pem 2048` e `openssl rsa -in private.pem -pubout -out public.pem`).

## Endpoints principais (resumo)
### Auth (`vale-food-auth`)
- `POST /auth/authenticate` → recebe `{ email, password }` e retorna token JWT.

### Usuários (`vale-food-user-management`)
- `GET /valefood/users`
- `GET /valefood/users/{userId}`
- `POST /valefood/users`
- `PUT /valefood/users/{userId}`
- `DELETE /valefood/users/{userId}`

#### Exemplo de payload — criar usuário
```json
{
  "name": "Edilson",
  "email": "edilson@email.br",
  "password": "admin",
  "type": "REGULAR",
  "preferredCategories": ["japonesa", "pizza"]
}
```

### Restaurantes (`vale-food-restaurant-management`)
- `GET /valefood/restaurants`
- `GET /valefood/restaurants/{restaurantId}`
- `POST /valefood/restaurants`
- `PUT /valefood/restaurants/{restaurantId}`
- `DELETE /valefood/restaurants/{restaurantId}`

#### Exemplo de payload — criar restaurante
```json
{
  "name": "Sushi do Vale",
  "address": "Av. Central, 1000",
  "userId": "<id-do-dono>",
  "categories": ["japonesa"],
  "products": [
    {"name": "Combo 1", "description": "8 peças", "price": 29.9}
  ]
}
```

### Promoções (`vale-food-promotion-management`)
- `GET /valefood/promotions` (pode aceitar filtros como `restaurantId`)
- `POST /valefood/promotions`
- `DELETE /valefood/promotions/{promotionId}`

#### Exemplo de payload — criar promoção
```json
{
  "restaurantId": "<id-restaurante>",
  "title": "Combo Sushi",
  "description": "12 peças por preço especial",
  "category": "japonesa",
  "price": 39.90
}
```

## Perfis e persistência
- **test**: repositórios em memória (`Memory*RepositoryImpl`) para desenvolvimento rápido.
- **local**: Firestore (`Firebase*RepositoryImpl`) quando configurado.

## Testes rápidos (Insomnia/Postman)
1. Autenticar em `/auth/authenticate` e capturar o JWT.
2. Enviar o token no header (ex.: `token: <token>`) para os demais serviços, se configurado.
3. Exercitar os CRUDs de usuários, restaurantes e promoções com os exemplos acima.

## Notas
- Ports podem variar conforme `server.port` em cada módulo.
- Alguns serviços podem validar o JWT usando um `HandlerInterceptor`.

