# Kanban-Board

Сервис представляет собой Kanban-доску для управления задачами с поддержкой приоритезации и временного планирования.

---
## Технические детали

- **Язык**: Java 21
- **Архитектура**: Кастомная реализация HTTP-сервера (com.sun.net.httpserver)
- **JSON-сериализация**: Gson с кастомными адаптерами
- **Менеджер задач**: `InMemoryTaskManager`
- **Обработка ошибок**: Кастомная система обработки и возврата ошибок

---
## Основные компоненты

### `BaseHttpHandler`
Базовый обработчик HTTP-запросов, реализующий интерфейс `HttpHandler`. Обрабатывает основные HTTP-методы:

```java
public class BaseHttpHandler implements HttpHandler {
    protected final TaskManager taskManager;
    private final Gson gson;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Обработка GET, POST, DELETE запросов
    }

    protected void handleGet(HttpExchange exchange) { ... }
    protected void handlePost(HttpExchange exchange) { ... }
    protected void handleDelete(HttpExchange exchange) { ... }

    protected void writeResponse(Object body, HttpExchange exchange, int code) { ... }
}
```

### Специализированные обработчики
 - `TaskHttpHandler` - обработчик задач
 - `SubtaskHttpHandler` - обработчик подзадач
 - `EpicHttpHandler` - обработчик эпиков

Кастомные `TypeAdapter`'ы\
Для корректной сериализации/десериализации временных типов:

```java
Gson gson = new GsonBuilder()
.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
.registerTypeAdapter(Duration.class, new DurationTypeAdapter())
.create();
```
---
## Особенности

 - Поддержка задач, подзадач (в составе эпиков) и эпиков
 - Контроль пересечений по времени выполнения задач
 - Автоматический расчет статуса эпиков на основе подзадач
 - История просмотра задач
 - Приоритезированный список задач
 - Кастомная реализация CRUD без использования Spring Framework

---
## API Endpoints

Базовый URL: `/TaskManager`

| Endpoint         | Описание                  |
|------------------|---------------------------|
| `/tasks`         | Управление задачами       |
| `/subtasks`      | Управление подзадачами    |
| `/epics`         | Управление эпиками        |
| `/history`       | История просмотра задач   |
| `/prioritized`   | Приоритезированный список |

---
## Модели данных

### `Task` (Базовая задача)
```java
public class Task {
    private Integer id;
    private String name;
    private String description;
    private Status status;  // NEW, IN_PROGRESS, DONE
    private Duration duration;
    private LocalDateTime startTime;
}
```
### `Subtask` (Подзадача)
Наследуется от `Task`, добавляет связь с эпиком:
```java
public class Subtask extends Task {
    private final Integer epicId;  // ID родительского эпика
}
```
### `Epic` (Эпик)
Наследуется от `Task`, содержит подзадачи:
```java
public class Epic extends Task {
    private final HashMap<Integer, Subtask> subtasks;

    // Автоматически рассчитывается на основе подзадач:
    // - duration (сумма длительностей подзадач)
    // - startTime (время начала самой ранней подзадачи)
    // - endTime (время завершения самой поздней подзадачи)
    // - status (NEW если все подзадачи NEW, DONE если все DONE, иначе IN_PROGRESS)
}
```
---
## CRUD Операции
### Создание задач
**Создание обычной задачи**

`POST /TaskManager/tasks`
```json
{
  "name": "задача",
  "description": "решить алгоритмическую задачу",
  "duration": 60,
  "startTime": "2025-06-29T14:00"
}
```
Ответ (201 Created):
```json
{
  "id": 3,
  "name": "задача",
  "description": "решить алгоритмическую задачу",
  "status": "NEW",
  "duration": 60,
  "startTime": "2025-06-29T14:00"
}
```
Ошибки:
   - 409 - Конфликт по времени с существующей задачей
   - 500 - Что-то пошло не так

**Создание эпика**

`POST /TaskManager/epics`
```json
{
  "name": "дача",
  "description": "дачные дела"
}
```
**Создание подзадачи**

`POST /TaskManager/subtasks`
```json
{
  "name": "земляника",
  "description": "собрать землянику",
  "epicId": 1,
  "duration": 10,
  "startTime": "2025-07-29T13:00"
}
```
---
### Получение задач
**Получение задачи по ID**

`GET /TaskManager/tasks/{id}`
Ответ (200 OK):
```json
{
    "id": 3,
    "name": "задача",
    "description": "решить алгоритмическую задачу",
    "status": "NEW",
    "duration": 60,
    "startTime": "2025-06-29T14:00"
}
```
Ошибки:
   - 404 - Задача не найдена

Аналогично для `/subtasks/{id}` и `/epics/{id}`

---
### Обновление задач
`POST /TaskManager/{endpoint}/{id}`

Доступные для обновления поля:

Для `Task`/`Subtask`: `name`, `description`, `duration`, `startTime`\
Для `Epic`: только `name` и `description`

Ошибки:
   - 400 - Некорректные данные
   - 404 - Задача не найдена
   - 409 - Конфликт по времени (для `Task`/`Subtask`)
---
### Удаление задач
`DELETE /TaskManager/{endpoint}/{id}`

Особенности:
   - При удалении эпика каскадно удаляются все его подзадачи
   - При удалении подзадачи эпик пересчитывает свои временные характеристики и статус

---
## Дополнительные возможности
### История просмотров
`GET /TaskManager/history` - возвращает список последних просмотренных задач

### Приоритезированный список
`GET /TaskManager/prioritized` - возвращает все задачи, отсортированные по `startTime`

---
## Ограничения
   - **Временные конфликты** - система не позволяет создать/изменить задачу, если ее временной интервал пересекается с существующей задачей
   - Для **эпиков** временные параметры рассчитываются автоматически на основе подзадач
   - **Статус эпика** автоматически пересчитывается при изменении статуса любой из его подзадач
