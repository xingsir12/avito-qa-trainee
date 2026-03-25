# Баг-репорты

**Сервис:** `https://qa-internship.avito.com`
**Дата:** 2026-03-24
**Окружение:** Windows 11, Java 17, Maven 4.0.0

---

## BUG-001: Нулевая статистика возвращает 400

**Краткое описание:**
При создании объявления со статистикой, где все значения равны 0, API возвращает
ошибку 400 с сообщением "поле likes обязательно", хотя 0 является валидным числовым значением.

**Шаги воспроизведения:**
1. Запустить тест: `mvn test -Dtest=CreateItemTest#createItem_zeroStatistics_shouldReturn200_butReturns400`
2. Либо ввести запрос вручную:
```bash
curl -X POST https://qa-internship.avito.com/api/1/item \
   -H "Content-Type: application/json" \
   -d '{
   "sellerID": 123456,
   "name": "Test Item",
   "price": 1000,
   "statistics": { "likes": 0, "viewCount": 0, "contacts": 0 }
   }'
```
   

**Фактический результат:**
```json
{
  "result": {
    "message": "поле likes обязательно",
    "messages": {}
  },
  "status": "400"
}
```
HTTP статус: `400 Bad Request`
Сервер интерпретирует `0` как отсутствие значения.

**Ожидаемый результат:** `200 OK` — `0` является валидным числовым значением

**Серьёзность** 
High

**Окружение**
URL: https://qa-internship.avito.com
Метод: POST /api/1/item
Версия API: v1

---

## BUG-002: Отрицательная цена принимается

**Краткое описание:**
API позволяет создать объявление с отрицательной ценой, что противоречит бизнес-логике 
(цена не может быть отрицательной).

**Шаги воспроизведения:**
1. Запустить тест: `mvn test -Dtest=CreateItemTest#createItem_negativePrice_returns400`
2. Либо ввести запрос вручную:
```bash
curl -X POST https://qa-internship.avito.com/api/1/item \
  -H "Content-Type: application/json" \
  -d '{
    "sellerID": 123456,
    "name": "Test Item",
    "price": -100,
    "statistics": { "likes": 1, "viewCount": 1, "contacts": 1 }
  }'
```

**Фактический результат:**
```json
{
  "status": "Сохранили объявление - 0a66838e-84eb-4511-ba77-fde37467c7a6"
}
```
HTTP статус: `200 OK`
Объявление создано с ценой -100

**Ожидаемый результат:** 
HTTP статус: `400 Bad Request`
Сообщение об ошибке: цена не может быть отрицательной

**Серьёзность**
High

**Окружение**
URL: https://qa-internship.avito.com
Метод: POST /api/1/item
Версия API: v1

## BUG-003: Отрицательная статистика принимается

**Краткое описание:**
API позволяет создать объявление с отрицательными значениями статистики (likes, viewCount, contacts),
что противоречит бизнес-логике (количество просмотров, лайков и контактов не может быть отрицательным).

**Шаги воспроизведения:**
1. Запустить тест: `mvn test -Dtest=CreateItemTest#createItem_negativeStatistics_returns400`
2. Либо ввести запрос вручную:
```bash
curl -X POST https://qa-internship.avito.com/api/1/item \
  -H "Content-Type: application/json" \
  -d '{
    "sellerID": 123456,
    "name": "Test Item",
    "price": 1000,
    "statistics": { "likes": -1, "viewCount": -5, "contacts": -2 }
  }'
```

**Фактический результат:**
```json
{
  "status": "Сохранили объявление - 424bdc32-aa8c-41fe-8058-ef368333683c"
}
```
HTTP статус: `200 OK`
Объявление создано с отрицательной статистикой

**Ожидаемый результат:**
HTTP статус: `400 Bad Request`
Сообщение об ошибке: статистика не может быть отрицательной

**Серьёзность**
High

**Окружение**
URL: https://qa-internship.avito.com
Метод: POST /api/1/item
Версия API: v1

---

## BUG-004: В ответе POST отсутствует поле createdAt

**Краткое описание:**
При создании объявления API возвращает только строку статуса `{"status":"Сохранили объявление - <uuid>"}`,
не возвращая поле createdAt, которое необходимо для проверки даты создания объявления.

**Шаги воспроизведения:**
1. Запустить тест: `mvn test -Dtest=E2EAndNonFunctionalTest#nonFunc_createdAtIsISO8601`
2. Либо ввести запрос вручную:
```bash
curl -X POST https://qa-internship.avito.com/api/1/item \
  -H "Content-Type: application/json" \
  -d '{
    "sellerID": 123456,
    "name": "Test Item",
    "price": 1000,
    "statistics": { "likes": 1, "viewCount": 1, "contacts": 1 }
  }'
```

**Фактический результат:**
```json
{
  "status": "Сохранили объявление - d7809920-fa9f-42cb-8c9e-101b24040739"
}
```
Поле createdAt отсутствует

**Ожидаемый результат:**
Тело ответа содержит поле createdAt в формате ISO 8601
Пример: `"createdAt": "2026-03-25T10:30:00.000Z"`

**Серьёзность**
Medium

**Окружение**
URL: https://qa-internship.avito.com
Метод: POST /api/1/item
Версия API: v1

---

## BUG-005: OPTIONS метод возвращает 405

**Краткое описание:**
OPTIONS запрос на /api/1/item возвращает 405 Method Not Allowed, браузерные клиенты
не смогут выполнять запросы из-за CORS preflight failure.

**Шаги воспроизведения:**
1. Запустить тест: `mvn test -Dtest=E2EAndNonFunctionalTest#nonFunc_corsHeadersPresent`
2. Либо ввести запрос вручную:
```bash
curl -X OPTIONS https://qa-internship.avito.com/api/1/item \
  -H "Origin: https://example.com"
```

**Фактический результат:**
HTTP статус: `405 Method Not Allowed`

**Ожидаемый результат:**
HTTP статус: `200 OK`
Заголовки: `Access-Control-Allow-Origin`, `Access-Control-Allow-Methods`

**Серьёзность**
Minor

**Окружение**
URL: https://qa-internship.avito.com
Метод: OPTIONS /api/1/item
Версия API: v1
---

## BUG-006: Список объявлений не сортируется по createdAt

**Краткое описание:**
`GET /api/1/{sellerID}/item` возвращает объявления в неопределенном порядке.
Ожидается сортировка по убыванию createdAt (новые первые). Порядок элементов не соответствует
ни одному из ожидаемых (ASC/DESC)

**Шаги воспроизведения:**
1. Запустить тест: `mvn test -Dtest=GetSellerItemsTest#getSellerItems_sortedByCreatedAtDesc`

**Фактический результат:**
Последнее созданное объявление не всегда находится первым в списке
Порядок возврата не гарантирован

**Ожидаемый результат:**
Объявления отсортированы по убыванию createdAt
Последнее созданное объявление должно быть первым в списке

**Серьёзность**
Medium

**Окружение**
URL: https://qa-internship.avito.com
Метод: POST /api/1/item
Версия API: v1

---

## BUG-007: name длиной 255 символов возвращает 400

**Краткое описание:**
При создании объявления с name длиной 255 символов API возвращает 400 Bad Request
с сообщением "не передан объект - объявление", хотя объявление передано.
**Шаги воспроизведения:**
1. Запустить тест: `mvn test -Dtest=CreateItemTest#createItem_maxLengthName_returns200`
2. Либо ввести запрос вручную:
```bash
curl -X POST https://qa-internship.avito.com/api/1/item \
  -H "Content-Type: application/json" \
  -d '{
    "sellerID": 123456,
    "name": "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
    "price": 1000,
    "statistics": { "likes": 1, "viewCount": 1, "contacts": 1 }
  }'
```

**Фактический результат:**
```json
{"result":{"message":"","messages":{}},"status":"не передан объект - объявление"}
```
HTTP статус: 400 Bad Request

**Ожидаемый результат:**
HTTP статус: `200 OK`
Объявление создано с name длиной 255 символов

**Серьёзность**
Medium

**Окружение**
URL: https://qa-internship.avito.com
Метод: POST /api/1/item
Версия API: v1

---