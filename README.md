# Explore With Me: Stage 2 Microservices

## Архитектура

Проект разделён на инфраструктурный слой и прикладные сервисы.

- `infra/config-server` — централизованная конфигурация из каталога `config/`.
- `infra/discovery-server` — реестр сервисов Eureka.
- `infra/gateway-server` — единая точка входа на порту `8080`.
- `stats-service/stats-server` — сервис статистики просмотров.
- `core/event-service` — управление событиями, категориями и подборками.
- `core/request-service` — управление заявками на участие.
- `core/user-service` — администрирование пользователей.
- `core/rating-service` — дополнительная функциональность: рейтинги событий.
- `core/common` — общие DTO, обработка ошибок, утилиты и Feign-клиенты.
- `core/main-service` — облегчённая оболочка, оставленная как переходный модуль без бизнес-логики.

## Конфигурация

Конфигурации сервисов вынесены в `config/`:

- `config/application.yml` — общие настройки Eureka, actuator и Feign timeouts.
- `config/event-service.yml` — БД и логирование `event-service`.
- `config/request-service.yml` — БД и логирование `request-service`.
- `config/user-service.yml` — БД и логирование `user-service`.
- `config/rating-service.yml` — БД и логирование `rating-service`.
- `config/stats-server.yml` — конфигурация сервиса статистики.
- `config/gateway-server.yml` — маршруты Gateway.
- `config/main-service.yml` — настройки переходного `main-service`.

Все прикладные сервисы получают конфигурацию через Config Server и регистрируются в Eureka.

## Маршрутизация через Gateway

Все внешние запросы должны идти через `gateway-server` на порт `8080`.

- `/admin/users/**` -> `user-service`
- `/users/*/requests/**` -> `request-service`
- `/users/*/events/*/requests` -> `request-service`
- `/users/*/events/*/rating` -> `rating-service`
- `/events/*/rating` -> `rating-service`
- `/admin/categories/**`, `/categories/**`, `/admin/compilations/**`, `/compilations/**`, `/admin/events/**`, `/users/*/events/**`, `/events/**` -> `event-service`
- `/hit`, `/stats` -> `stats-server`

## Внутренний API

Межсервисное взаимодействие реализовано через OpenFeign и Eureka.

### `user-service`

- `GET /internal/users/{userId}/short`
  Возвращает `UserShortDto` для проверки существования пользователя и сохранения инициатора в `event-service`.

### `event-service`

- `GET /internal/events/{eventId}`
  Возвращает `EventInternalDto` со статусом события, инициатором, лимитом участников и текущим числом подтверждённых заявок.
- `PATCH /internal/events/{eventId}/confirmed-requests?delta={n}`
  Меняет счётчик подтверждённых заявок на событии.

### `request-service`

- `GET /internal/users/{userId}/events/{eventId}/requests`
  Возвращает список заявок для события инициатора.
- `PATCH /internal/users/{userId}/events/{eventId}/requests`
  Подтверждает или отклоняет заявки для события инициатора.

### `rating-service`

- `POST /internal/ratings/summary`
  Принимает список `eventId` и возвращает агрегированные рейтинги пачкой без N+1 запросов.

## Границы сервисов

### `event-service`

- Публичный и приватный API событий.
- Админский API событий.
- Категории.
- Подборки.
- Обогащение событий просмотрами из `stats-service`.
- Обогащение событий рейтингом из `rating-service` с fallback в `0`, если сервис рейтинга недоступен.
- Хранение снапшота инициатора (`user_id`, `initiator_name`), чтобы публичные запросы событий продолжали работать даже без `user-service`.

### `request-service`

- Создание заявки текущим пользователем.
- Получение/отмена собственных заявок.
- Модерация заявок инициатором события.
- Синхронизация количества подтверждённых заявок через внутренний API `event-service`.

### `user-service`

- Создание, получение и удаление пользователей по админскому API.
- Внутренний API для остальных сервисов.

### `rating-service`

- Постановка/удаление реакции.
- Получение сводки рейтинга по событию.
- Пакетное получение рейтингов для списков событий.

## Устойчивость к сбоям

`event-service` не завязан критически на остальные прикладные сервисы для публичного чтения:

- если `stats-service` недоступен, просмотры возвращаются как `0`;
- если `rating-service` недоступен, рейтинг возвращается как `0`;
- инициатор события берётся из сохранённого снапшота, поэтому публичные ответы событий не требуют `user-service`.

Это позволяет оставлять рабочими публичные сценарии событий даже при остановке соседних сервисов.

## Сборка

```bash
mvn compile
mvn test
```

## Внешний API

- Основной API: [ewm-main-service-spec.json](./ewm-main-service-spec.json)
- API статистики: [ewm-stats-service-spec.json](./ewm-stats-service-spec.json)

## Postman

Для проверки можно использовать:

- основной набор тестов микросервисного этапа;
- `postman/feature.json` для дополнительной функциональности рейтингов;
- коллекции, переданные преподавателем для `main-service` и `stats-service`.
