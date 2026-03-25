# Список найденных багов

**Дата:** 2026-03-25
**Проверяющий:** Чигрин Д.А.

## ▎Bug 1.1
|                        |                                                                                                   |
|:-----------------------|:--------------------------------------------------------------------------------------------------|
| Проект                 | qa-internship.avito.com                                                                           |
| Компонент              | API /api/1/item (POST)                                                                            |
| Версия API             | v1                                                                                                |
| Серьёзность (Severity) | Critical                                                                                          |
| Приоритет (Priority)   | P1                                                                                                |

---

### Описание

При создании объявления с корректной структурой `statistics` (объект с полями `likes`, `viewCount`,
`contacts`) API возвращает ошибку 400 с сообщением "поле likes обязательно", несмотря на то, что поле передано.
Ожидаемое поведение — успешное создание объявления с переданной статистикой.
---

### Шаги воспроизведения

1. Отправить POST запрос:`POST https://qa-internship.avito.com/api/1/item`
2. Установить заголовки Headers:
`Content-Type: application/json`
`Accept: application/json`
3. Отправить тело запроса:
```json
{
  "sellerId": 123456,
  "name": "Test Item",
  "price": 1000,
  "statistics": {
    "likes": 0,
    "viewCount": 0,
    "contacts": 0
  }
}
```
---

### Результаты
|             |                                            |
|:------------|:-------------------------------------------|
| Фактический | 400 Bad Request — "поле likes обязательно" |
| Ожидаемый   | 200 OK — объявление создано со статистикой |

---

### CURL для воспроизведения

curl -X POST 'https://qa-internship.avito.com/api/1/item' \
-H 'Content-Type: application/json' \
-d '{"sellerId":123456,"name":"Test Item","price":1000,"statistics":{"likes":0,"viewCount":0,"contacts":0}}'

---

## ▎Bug 1.2
|                        |                                                                                                   |
|:-----------------------|:--------------------------------------------------------------------------------------------------|
| Проект                 | qa-internship.avito.com                                                                           |
| Компонент              | API /api/1/item (POST)                                                                            |
| Версия API             | v1                                                                                                |
| Серьёзность (Severity) | Critical                                                                                          |
| Приоритет (Priority)   | P1                                                                                                |

---

### Описание

При создании объявления с корректной структурой `statistics` (объект с полями `likes`, `viewCount`,
`contacts`) API возвращает ошибку 400 с сообщением "поле likes обязательно", несмотря на то, что поле передано.
Ожидаемое поведение — успешное создание объявления с переданной статистикой.
---

### Шаги воспроизведения

1. Отправить POST запрос:`POST https://qa-internship.avito.com/api/1/item`
2. Установить заголовки Headers:
   `Content-Type: application/json`
   `Accept: application/json`
3. Отправить тело запроса:
```json
{
  "sellerId": 123456,
  "name": "Test Item",
  "price": 1000,
  "statistics": {
    "likes": 0,
    "viewCount": 0,
    "contacts": 0
  }
}
```
---

### Результаты
|             |                                            |
|:------------|:-------------------------------------------|
| Фактический | 400 Bad Request — "поле likes обязательно" |
| Ожидаемый   | 200 OK — объявление создано со статистикой |

---

### CURL для воспроизведения

curl -X POST 'https://qa-internship.avito.com/api/1/item' \
-H 'Content-Type: application/json' \
-d '{"sellerId":123456,"name":"Test Item","price":1000,"statistics":{"likes":0,"viewCount":0,"contacts":0}}'

---