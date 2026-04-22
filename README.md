# Statystyka

Aplikacja webowa oparta na technologii Java i Spring Boot, służąca do rejestracji, logowania oraz zarządzania użytkownikami i wybranymi procesami biznesowymi. Projekt został przygotowany jako aplikacja wspierająca organizację pracy, raportowanie oraz obsługę danych w środowisku webowym.

## Główne funkcjonalności

- rejestracja i logowanie użytkowników,
- zarządzanie kontami użytkowników,
- obsługa ról i uprawnień,
- integracja z bazą danych PostgreSQL,
- wysyłka wiadomości e-mail,
- uruchamianie aplikacji w kontenerach Docker,
- automatyczny proces budowania aplikacji z wykorzystaniem GitHub Actions.

## Technologie

Projekt wykorzystuje następujące technologie:

- Java 22
- Spring Boot 3.3.3
- Spring Security
- Spring Data JPA
- Thymeleaf
- PostgreSQL
- Maven
- Docker
- Docker Compose
- GitHub Actions

## Struktura projektu

Najważniejsze elementy repozytorium:

- `register/` – katalog główny aplikacji Spring Boot,
- `register/src/` – kod źródłowy aplikacji,
- `register/Dockerfile` – definicja obrazu Docker dla aplikacji,
- `.github/workflows/ci.yml` – konfiguracja workflow GitHub Actions,
- `docker-compose.yml` – konfiguracja uruchamiania aplikacji i bazy danych w kontenerach.

## Wymagania

Przed uruchomieniem projektu lokalnie należy mieć zainstalowane:

- Java 22
- Maven lub Maven Wrapper
- Docker
- Docker Compose

## Uruchomienie aplikacji lokalnie bez Dockera

### 1. Uruchomienie bazy danych PostgreSQL

Można uruchomić samą bazę danych przez Docker Compose:

```bash
docker compose up -d db
```
### 2. Przejście do katalogu aplikacji
```bash
cd register
```
### 3. Uruchomienie aplikacji

Na Windows:
```bash
mvnw.cmd spring-boot:run
```
Na Linux / macOS:
```bash
./mvnw spring-boot:run
```
### Aplikacja będzie dostępna pod adresem:

http://localhost:8080

### Uruchomienie aplikacji przez Docker Compose

Projekt można uruchomić wraz z bazą danych i aplikacją za pomocą jednego polecenia:
```bash
docker compose up --build
```
Po poprawnym uruchomieniu:

aplikacja będzie dostępna pod adresem http://localhost:8080
baza danych PostgreSQL będzie dostępna na porcie 5432

Aby zatrzymać kontenery, należy użyć:
```bash
docker compose down
```
### Konfiguracja bazy danych

Domyślna konfiguracja bazy danych w docker-compose.yaml:

- nazwa bazy danych: Statystyka<br>
- użytkownik: Javed<br>
- port: 5432

Aplikacja korzysta ze zmiennych środowiskowych dla konfiguracji połączenia z bazą danych, dzięki czemu może działać zarówno lokalnie, jak i w środowisku kontenerowym.

### Konfiguracja poczty e-mail

Dane związane z wysyłką wiadomości e-mail powinny być przekazywane przez zmienne środowiskowe, np.:

- SPRING_MAIL_USERNAME<br>
- SPRING_MAIL_PASSWORD<br>
- SPRING_MAIL_FROM

Dzięki temu w repozytorium nie są przechowywane poufne dane konfiguracyjne.

### Konto administratora

Podczas uruchomienia aplikacji może zostać automatycznie utworzone konto administratora na podstawie właściwości konfiguracyjnych:

- APP_BOOTSTRAP_ADMIN_USERNAME<br>
- APP_BOOTSTRAP_ADMIN_EMAIL<br>
- APP_BOOTSTRAP_ADMIN_PASSWORD

Mechanizm ten ułatwia przygotowanie środowiska startowego aplikacji.

### CI/CD

Repozytorium zostało rozszerzone o podstawowy proces CI z wykorzystaniem GitHub Actions.

Workflow uruchamia się automatycznie po zmianach wypchniętych do gałęzi main i obejmuje:

- pobranie kodu źródłowego,<br>
- konfigurację środowiska Java,<br>
- budowanie aplikacji,<br>
- budowanie obrazu Docker.

Dzięki temu możliwa jest automatyczna weryfikacja procesu budowania projektu po każdej zmianie w repozytorium.

### Uwagi
- projekt wykorzystuje Docker Compose do uruchomienia kompletnego środowiska aplikacyjnego,<br>
- aplikacja została przygotowana do współpracy z bazą PostgreSQL,<br>
- w środowisku kontenerowym połączenie z bazą danych odbywa się przez nazwę serwisu db,<br>
- w przypadku pracy lokalnej aplikacja może korzystać z localhost.