<h1 align="center">Cloud
<h3 align="center">Облачное хранилище файлов (Java IO, NIO, Netty, StreamAPI + Java FX)</h3>

<a href="https://shev-81.github.io/Cloud-netty/"><img src="https://img.shields.io/badge/Cloud-JavaDoc-green"/>  </a>


## 0. Предисловие 
Проект написан, в рамках изучения мной сетевого фреймворка `Netty`, я решил создать облачную систему хранения файлов с `Netty` в качестве ядра. Мной рассматривалось два варианта реализации прикладного протокола взаимодействия между сервером и клиентом. Первый вариант, это работа с передаваемыми байтами через `ByteBuffer Netty`, и второй это работать с настроенными `PipeLine фильтром` (который, к слову все так же работает с `ByteBuffer`, ведь все, что летит по сети - это Байты! ), в котором использовать фильтры сериализации исходящих сообщений и десериализации входящих. Я выбрал второй вариант - работать с объектами, хотя протокол написанный мной в рамках другого проекта с байтами показал большую эффективность.

> <b>Netty</b> — это инфраструктура клиент/сервер, которая использует расширенные сетевые возможности Java, скрывая за собой сложность и предоставляя простой в использовании API.

> <b>В рамках проекта реализованно:</b>

- <b>Сервер Netty</b> запускается и подключает клиентов, пересылает служебные сообщения от клиентов (котороые так же могут нести в себе данные из файла), отвечает за авторизацию пользователей, использует `Data Base SQLite`, для хранения данных пользователей (работа с Data Base через `JDBC`), распределяет и контроллирует выделяемое под пользователей дисковое пространство, по запросу от клиентов проводит действия на закрепленном  за пользователем дисковом пространстве. 
- <b>Клиент Java FX</b> запускаясь предлагает пользователю авторизоваться и при успехе предоставляет доступ к основному графическому интерфейсу `(GUI)`. `GUI` показывает состояние хранилища для пользователя: наличие файлов и их свойства, общую наполненность до `10 Gb` (это размер дискогвого пространства, выделяемого каждому пользователю на сервере). Так же `GUI` содержит элементы управления позволяющие выполнять следующие действия.
   - Принять и записать на дисковое пространство пользователя файлы от клиента.
   - Передать файлы клиенту. 
   - Удалить файлы у себя с дискового пространства (закрепленного за пользователем). 

> <b>Техническая часть</b>
 - `IDE: IntelliJ IDEA 2021.3.3`
 - `Версия JDK: 1.8.0_121 + 16 на стороне клиента.`
 - `Netty Framework`
 - `SQLite`
> <b>Используемые технологии:</b>
 - `Java FX`
 - `Java IO, NIO`
 - `Stream API`
 - `Netty`
 - `CSS`
 - `JDBC`
 - `Мавен 3.5`
 
 > <b>Вид программы в Production.</b>
  
  ![2022-08-26_21-40-44](https://user-images.githubusercontent.com/89448563/186971668-ee48358c-436e-4c15-8dde-a004855b0fbc.png)

Передача данных между клиентом и сервером основанна на Object Message, сообщения могут содержать данные из передаваемых файлов и не превышают 
лимит в 10 Mb. При передаче файлов больших размеров файл разрезается на куски и по частям пересылается.  

## 1. Модуль Server

#### 1.1 API ядра Netty

Когда запускается сервер Netty, в нем настраивается конвеер `Pipe Line` для обработки получаемых из сети данных. Все данные получаемые приложением из сети попадают в `Byte Bufer` сервера `Netty`  и проходят через `Pipe Line` до `Main Handler'a`. Для обработки полученных байтов из `Byte Buffer'a` используются сериализатор и десериализатор. Полученные таким образом объекты приходят в конечную точку `Pipe Line` в `MainHandler`. Если необходимо отправить сообщение в сеть, происходит обратный процесс пошаговой обработки в `Pipe Line`. Сдесь нужно сказать, что элементы `PipeLine` могут обрабатывать как входящий поток так и исходящий или быть как `MainHandler` конечной точкой.

![api-netty](https://user-images.githubusercontent.com/89448563/187065711-7b930405-2e5f-456a-a80f-f6c2d6009df9.png)

- <b>`Object Decoder`</b> - Декодер, десериализует полученный байтовый буфер в объекты Java.
- <b>`Object Encoder`</b> - Кодировщик, который сериализует объект Java в байтбуфер.
- <b>`MainHandler`</b> - является точкой соприкосновения cо всеми сервисами программы.

#### 1.2 MainHandler

Нужно упоминуть, что для каждого подключаемого клиента создается свой PipeLine содержащий все элементы, и так же MainHandler. Создавая новый `PipeLine`, MainHandler получает ссылку на сервис авторизации и запускает регистратор слушателей служебных сообщений `RegistryHandler` прописывающий в себе ссылки на методы обработки входящих сообщений по их типу (патерн Registry). При получении из `PipeLine` входящего объекта сообщения в `MainHandler` вызывается `RegistryHandler` и из него достается зарегистрированный под этот тип сообщения слушатель - свой `Handler` и выполняется метод для обработки пришедшего сообщения. 
   
> Регистрация слушателей.
   
    ...
    private Map<Class<? extends AbstractMessage>, RequestHandler> mapHandlers;
    ...   
    public RegistryHandler(Controller controller) {
        this.mapHandlers = new HashMap<>();
        mapHandlers.put(AuthMessage.class, new AuthHandler(controller)::authHandle);
        mapHandlers.put(RegUserRequest.class, new RegUserHandler(controller)::regHandle);
        mapHandlers.put(FileMessage.class, new FileHandler(controller)::fileHandle);
        mapHandlers.put(FilesSizeRequest.class, new FilesSizeRequestHandler(controller)::filesSizeReqHandle);
    }

Где `RequestHandler` является функциональным интерфейсом
   
    @FunctionalInterface
    public interface RequestHandler {
       void handle(ChannelHandlerContext ctx, Object msg);
    }

   
> <b>Схема обработки входящих сообщений в `MainHandler` </b>

![MainHandler](https://user-images.githubusercontent.com/89448563/187069114-ff894097-34f2-4c83-9bc1-a90873432183.png)
   
> Используются следующие зарегистрированные слушатели:

- <b>`AuthHandler`</b> - Используя сервис авторизации пользователей, выполняет авторизацию пользователя на сервере. (для хранения данных пользователей используется локальная SQLite база данных). При успешной авторизации на сервере предоставляет доступ к рабочему функционалу облака.
- <b>`DelFileHandler`</b> - Удаляет файл из облака пользователя.
- <b>`FileHandler`</b> - Получает сообщение с данными файла и записывает на диск. 
- <b>`FilesListRequestHandler`</b> - Отвечает на запрос о состояние Облака, для пользователя, отправившего запрос.
- <b>`FileHandler`</b> - Обрабатывает запрос на регистрацию нового пользователя, проверяет, есть ли в базе данных пользователь с таким же ником, если есть, то отправляет отказ в регистрации, а при отсутствии регистрирует его и отправляет ответ клиенту, что регистрация прошла успешно.
- <b>`ReqFileHandler`</b> - Обрабатывает запрос от клиента на получение файла, если файл весит более 10 Мб,  разрезает его на части по 10 Мб. и отправляет клиенту.


## 2. Модуль клиент
- Клиент приложение на Java FX. При запуске запрашивает логин и пароль позльзователя, а при отсутствии аккаунта предоставляет возможность регистрации нового пользователя.
- Запускается клиент Netty в котором настроен конвеер с двумя декодерами для посылки объектов и получения, а так же Main Handler для обработки 
  полученных объектов сообщений.  
- После успешной аутентификации открывается окно в которое клиент подгрузит с сервера актуальную информацию: список файлов этого клиента и общую загруженность облака 
  на сервере (лимит 10 Gb).
- В клиентском окошке есть функционал: добавление файлов из ОС пользователя на сервер, уделение файлов с сервера, копирование файлов с сервера на клиент.
- Так же как и на сервере добавлены обработчики входящих сообщений зарегистрированные в Main Handler'e  (патерн разработки Registry). 
  И в зависимости от полученного сообщения выполняется код обработчика соответствующего типу сообщения.  

