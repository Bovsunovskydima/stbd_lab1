# cafenet

База даних мережі кав'ярень: філії, персонал і зміни, клієнти з картками лояльності,
меню з технологічними картами, склад, закупівлі, продажі, доставка, відгуки.

PostgreSQL 16 + Spring Boot (Java 21), міграції через Liquibase, доступ до даних з коду —
Repository + Unit of Work поверх збережених процедур і розрізів даних.

## Запуск

```
docker compose up -d --build
```

| Сервіс | Адреса |
|---|---|
| API | http://localhost:8090/api/ping |
| Adminer | http://localhost:8091 |
| PostgreSQL | localhost:5434 |

Доступ до БД: база `cafenet`, користувач `cafenet`, пароль `cafenet`.

Зупинити і прибрати дані:

```
docker compose down -v
```

Запити для перевірки роботи — у файлі `requests.http`.

## Структура

```
src/main/resources/db/changelog/
  master.yaml
  changesets/
    01-tables.sql       22 таблиці, ключі, UNIQUE, CHECK
    02-functions.sql    9 користувацьких функцій
    03-triggers.sql     6 тригерних функцій, 15 тригерів
    04-views.sql        25 розрізів даних
    05-routines.sql     51 збережена процедура
    06-indexes.sql      38 індексів, 6 типів
    07-demo-data.sql    дані для перевірки

src/main/java/ua/lab/cafenet/
  persistence/            UnitOfWork, StatementRunner, винятки
  persistence/repository/ 12 репозиторіїв
  domain/                 моделі читання
  service/                сервіси, зокрема CheckoutService
  api/                    REST-контролери, форми запитів

docs/schema.dbml          схема для dbdiagram.io
requests.http             запити до API
```

## Відповідність завданню

**1. Схема.** `docs/schema.dbml` — імпортується в dbdiagram.io через `Import → From DBML`.
Той самий опис у вигляді DDL — `01-tables.sql`: первинні й зовнішні ключі, іменовані
обмеження `UNIQUE` та `CHECK`.

**2. Сутності.** 22 таблиці: `branches`, `positions`, `staff`, `shifts`, `clients`,
`client_addresses`, `loyalty_cards`, `promo_codes`, `menu_sections`, `menu_items`, `products`,
`tech_cards`, `suppliers`, `purchase_orders`, `purchase_lines`, `stock_movements`,
`sales_orders`, `sales_lines`, `couriers`, `deliveries`, `payments`, `reviews`.

**3. Soft delete та аудит.**

Soft delete через `deleted_at` мають 5 сутностей: `staff`, `clients`, `promo_codes`,
`menu_items`, `products`. Працює тригером `BEFORE DELETE`, який замість видалення рядка
проставляє `deleted_at` і повертає `NULL`, тому звичайний `DELETE` фізично нічого не видаляє.

Дату зміни та автора (`modified_at`, `modified_by`) зберігають 6 сутностей: ті самі п'ять і
`sales_orders`. Заповнює тригер `BEFORE INSERT OR UPDATE`, який читає користувача з сесійної
змінної `cafenet.actor`. Змінну виставляє процедура `sp_actor_set`, її викликає `JdbcUnitOfWork`
на початку кожної транзакції, а значення береться із заголовка запиту `X-Staff-Id`.

**4. СУБД.** PostgreSQL 16, розширення `pg_trgm` і `btree_gist`. 43 ченджсети Liquibase.

**5. Процедури, функції, тригери, View.**

| Тип | Кількість |
|---|---|
| Збережені процедури | 51 |
| Користувацькі функції | 9 |
| Тригерні функції | 6 |
| Тригери | 15 |
| Розрізи даних | 25 |

Бізнес-логіка в тригерах:

- `tg_order_total` на `sales_lines` — перераховує суму замовлення з урахуванням промокоду;
- `tg_loyalty_points` на `payments` — нараховує бали на картку й оновлює рівень;
- `tg_shift_overlap` на `shifts` — не дає поставити працівникові зміни, що перетинаються;
- `tg_default_address` на `client_addresses` — лишає лише одну основну адресу в клієнта.

Списання складу робить `sp_order_close`: за технологічними картами рахує потребу в продуктах,
порівнює із залишком і відхиляє закриття, якщо продуктів не вистачає.

**6. Індекси.** 38 індексів, 6 типів:

| Тип | Приклад |
|---|---|
| B-tree | `ix_sales_branch_created (branch_id, created_at DESC) INCLUDE (total)` |
| B-tree частковий | `ix_sales_open ... WHERE status = 'NEW'` |
| UNIQUE частковий | `uq_clients_phone_active ... WHERE deleted_at IS NULL` |
| GIN | `ix_menu_title_trgm USING gin (title gin_trgm_ops)` |
| GiST | `ix_shifts_range_gist USING gist (staff_id, tsrange(starts_at, ends_at))` |
| HASH | `ix_sales_status_hash USING hash (status)` |
| BRIN | `ix_sales_created_brin USING brin (created_at)` |

**7. Repository + Unit of Work.**

`UnitOfWork` — одна одиниця роботи на одне з'єднання з `autoCommit = false`, дає доступ до
12 репозиторіїв і має `commit()`, `rollback()`, `close()` з неявним відкатом незавершеної
транзакції. `UnitOfWorkFactory.open(actor)` одразу викликає `sp_actor_set`, завдяки чому
працює аудит.

`StatementRunner` — єдина точка доступу до JDBC: читання тільки з View та функцій, зміни
тільки через `CALL sp_*`. Прямих `INSERT`, `UPDATE` і `DELETE` у коді немає.

`CheckoutService.checkout` — сценарій на кілька зв'язаних сутностей в одній транзакції:
клієнт → замовлення → позиції → промокод → оплата → списання складу → доставка з призначенням
вільного курєра. Будь-яка помилка відкочує все.
