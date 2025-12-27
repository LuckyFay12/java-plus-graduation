# Explore With Me (EWM)

Проект афиша. Здесь можно предложить какое-либо событие от выставки до похода в кино и собрать компанию для участия в нём.

## Архитектура

Проект реализован на основе микросервисной архитектуры с использованием Spring Cloud.

### Микросервисы:

- **Event Service** — управление событиями
- **User Service** — управление пользователями
- **Request Service** — управление заявками на участие
- **Comment Service** — добавление и модерация комментариев
- **Stats Server** — сбор и анализ статистики обращений

### Инфраструктурные сервисы:

- **Gateway Server** (Spring Cloud Gateway) — единая точка входа в приложение
- **Discovery Server** (Netflix Eureka) — регистрация и обнаружение сервисов
- **Config Server** (Spring Cloud Config) — централизованная конфигурация

### Особенности:

- Каждый сервис — **отдельное Spring Boot приложение**
- Сервисы User, Event, Request, Comment используют **один экземпляр PostgreSQL**, но каждый работает со своей базой данных
- Stats Server использует **отдельный экземпляр PostgreSQL**
- Межсервисная коммуникация через **REST API (OpenFeign)**
- Использование **Circuit Breaker (Resilience4j)** для отказоустойчивости

## Внешнее API

Внешний API приложения предоставляет функциональность через API Gateway на порту **8080**. Все запросы проходят через Gateway, который маршрутизирует их на соответствующие микросервисы.

### Публичные API (доступны всем)

**Категории**:
- `GET /categories` - получение списка категорий
- `GET /categories/{catId}` - получение категории по идентификатору

**События**:
- `GET /events` - получение событий с фильтрацией
- `GET /events/{id}` - получение подробной информации о событии

**Подборки событий**:
- `GET /compilations` - получение подборок событий
- `GET /compilations/{compId}` - получение подборки по идентификатору

### Закрытые API (требуют аутентификации)

**Пользователи**:
- `GET /users/{userId}` - получение информации о пользователе
- `POST /users` - регистрация пользователя

**События**:
- `POST /users/{userId}/events` - создание события
- `GET /users/{userId}/events` - получение событий пользователя
- `PATCH /users/{userId}/events/{eventId}` - редактирование события

**Запросы на участие**:
- `POST /users/{userId}/requests?eventId={eventId}` - создание запроса на участие
- `GET /users/{userId}/requests` - получение запросов пользователя
- `PATCH /users/{userId}/requests/{requestId}/cancel` - отмена запроса

**Комментарии**:
- `POST /users/{userId}/events/{eventId}/comments` - создание комментария
- `GET /users/{userId}/events/{eventId}/comments` - получение комментариев события

### Административные API

**Пользователи**:
- `GET /admin/users` - получение всех пользователей
- `POST /admin/users` - создание пользователя
- `DELETE /admin/users/{userId}` - удаление пользователя

**Категории**:
- `POST /admin/categories` - создание категории
- `PATCH /admin/categories/{catId}` - редактирование категории
- `DELETE /admin/categories/{catId}` - удаление категории

**События**:
- `GET /admin/events` - получение всех событий
- `PATCH /admin/events/{eventId}` - редактирование события (публикация/отклонение)

**Подборки**:
- `POST /admin/compilations` - создание подборки
- `PATCH /admin/compilations/{compId}` - редактирование подборки
- `DELETE /admin/compilations/{compId}` - удаление подборки


### Технологический стек:
- Java 21, Spring Boot 3.x
- Spring Cloud: Gateway, Config, Netflix Eureka
- Базы данных: PostgreSQL
- Сборка: Maven 3.8+

## Запуск приложения

### 1.Подготовка базы данных (Docker)

**Запустите контейнеры с базами данных**:
   ```bash
   # Из корня проекта
   docker-compose up -d
```

### 2. Запуск инфраструктурных сервисов

1. **Config Server** (порт 8888)
2. **Discovery Server** (порт 8761)
3. **Gateway Server** (порт 8080)

### 3. Запуск бизнес-сервисов
   