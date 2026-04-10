# Explore With Me

Актуальное состояние репозитория: многомодульный Maven-проект на Java 21, Spring Boot 3.3.4 и Spring Cloud 2023.0.3. Проект разделён на инфраструктурный слой, прикладные сервисы и статистический контур на gRPC + Kafka.

## Модули

### `infra`

- `infra/config-server` — централизованная конфигурация из каталога `config/`
- `infra/discovery-server` — реестр сервисов Eureka
- `infra/gateway-server` — единая точка входа

### `core`

- `core/common` — общие DTO, Feign-клиенты, ошибки и утилиты
- `core/event-service` — публичный, приватный и админский API событий, категорий и подборок
- `core/request-service` — заявки на участие
- `core/user-service` — админский CRUD пользователей
- `core/rating-service` — реакции и агрегированный рейтинг событий
- `core/main-service` — переходный модуль без самостоятельной бизнес-логики

### `stats-service`

- `stats-service/stats-client` — клиент для вызовов `collector` и `analyzer`
- `stats-service/stats-dto` — общие Avro / protobuf модели
- `stats-service/collector` — приём действий пользователей по gRPC и отправка их в Kafka
- `stats-service/aggregator` — расчёт similarity между событиями
- `stats-service/analyzer` — рекомендации, похожие события и interaction counts по gRPC

Каталог `stats-service/stats-server` в репозитории присутствует, но в текущий [`stats-service/pom.xml`](./stats-service/pom.xml) не включён и в стандартную верхнеуровневую сборку не входит.

## Инфраструктура и конфигурация

`docker-compose.yml` поднимает:

- `ewm-db` — PostgreSQL для прикладных сервисов, порт `5432`
- `stats-db` — PostgreSQL для статистического контура, порт `5433`
- `kafka` — брокер Kafka, порт `9092`
- `zookeeper` — Zookeeper, порт `2181`

Актуальные конфиги в `config/`:

- `config/application.yml` — общие настройки Eureka, Kafka и базовые свойства
- `config/gateway-server.yml`
- `config/event-service.yml`
- `config/request-service.yml`
- `config/user-service.yml`
- `config/rating-service.yml`
- `config/main-service.yml`
- `config/collector.yml`
- `config/aggregator.yml`
- `config/analyzer.yml`

`config/stats-server.yml` сохранён в репозитории для старого `stats-server`, но не используется текущим статистическим контуром.

## Как устроена статистика

1. `event-service` и `request-service` отправляют действия пользователей в `collector` по gRPC через `stats-client`.
2. `collector` пишет действия в Kafka-топик `stats.user-actions.v1`.
3. `aggregator` читает действия, пересчитывает схожесть событий и публикует результаты в `stats.events-similarity.v1`.
4. `analyzer` читает оба Kafka-топика, хранит агрегаты в `stats-db` и отдаёт по gRPC:
   - рекомендации для пользователя
   - похожие события
   - количество взаимодействий по списку событий

## Маршрутизация через Gateway

Сейчас в [`config/gateway-server.yml`](./config/gateway-server.yml) настроены только следующие внешние маршруты:

- `/admin/users/**` -> `USER-SERVICE`
- `/users/*/requests/**` -> `REQUEST-SERVICE`
- `/users/*/events/*/requests` -> `EVENT-SERVICE`
- `/admin/categories/**`, `/categories/**`, `/admin/compilations/**`, `/compilations/**`, `/admin/events/**`, `/users/*/events/**`, `/events/**` -> `EVENT-SERVICE`

Важно: в текущем gateway-конфиге нет маршрутов для `rating-service` и старого `/hit`, `/stats` API.

## Внутренние интеграции

### `user-service`

- `GET /internal/users/{userId}/short`

### `event-service`

- `GET /internal/events/{eventId}`
- `POST /internal/events/{eventId}/confirmed-requests?delta={n}`

### `request-service`

- `GET /internal/users/{userId}/events/{eventId}/requests`
- `POST /internal/users/{userId}/events/{eventId}/requests`
- `GET /internal/users/{userId}/events/{eventId}/requests/exists`

### `rating-service`

- `POST /internal/ratings/summary`

## Что делает каждый сервис

### `event-service`

- управляет событиями, категориями и подборками
- обогащает события interaction count и рекомендациями через `analyzer`
- запоминает снапшот инициатора в событии, чтобы публичное чтение не зависело жёстко от `user-service`

### `request-service`

- создаёт, показывает и отменяет заявки
- модерирует заявки инициатором события
- отправляет регистрации в `collector`

### `user-service`

- предоставляет админский CRUD пользователей
- отдаёт внутренний `UserShortDto` для остальных сервисов

### `rating-service`

- принимает и удаляет реакции пользователей
- считает агрегированный рейтинг события
- отдаёт пачку summary для `event-service`

### `collector`

- принимает пользовательские действия по gRPC
- сериализует их и пишет в Kafka

### `aggregator`

- пересчитывает similarity между событиями по поступающим действиям
- публикует только актуальные значения similarity в Kafka

### `analyzer`

- хранит агрегаты действий и similarity
- отдаёт рекомендации, похожие события и interaction counts по gRPC

## Локальный запуск

Требования:

- Java 21+
- Maven
- Docker / Docker Compose

Инфраструктура:

```bash
docker compose up -d
```

Сборка:

```bash
mvn clean package
```

Рекомендуемый порядок запуска сервисов:

1. `infra/discovery-server`
2. `infra/config-server`
3. `stats-service/collector`
4. `stats-service/aggregator`
5. `stats-service/analyzer`
6. `core/user-service`
7. `core/request-service`
8. `core/rating-service`
9. `core/event-service`
10. `infra/gateway-server`

Для ручного запуска через Maven можно использовать:

```bash
mvn -q -pl <module> spring-boot:run
```

Полезные порты:

- `8761` — Eureka
- `8080` — Gateway
- `5432` — `ewm-db`
- `5433` — `stats-db`
- `9092` — Kafka

## Сборка и проверка

Полная сборка:

```bash
mvn clean package
```

Все тесты:

```bash
mvn test
```

Точечная проверка `aggregator`:

```bash
mvn -q -pl stats-service/aggregator -am test
```

## Актуальные tester-отчёты

В корне репозитория лежат рабочие отчёты без ошибок:

- `tester-report-collection.txt`
- `tester-report-aggregation-fixed.txt`
- `tester-report-smoke.txt`

## Спецификации и Postman

- Основной API: [ewm-main-service-spec.json](./ewm-main-service-spec.json)
- API статистики: [ewm-stats-service-spec.json](./ewm-stats-service-spec.json)
- Дополнительная Postman-коллекция: `postman/feature.json`
