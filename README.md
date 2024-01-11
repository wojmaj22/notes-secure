# Notes secure
Projekt prostej aplikacji pozwalającej na przechowywanie notatek. 
Głównym nastawieniem projektu jest bezpieczeństwo aplikacji oraz przechowywanych notatek.
Projekt zbudowant w oparciu o Java oraz Spring i architekturę MVC.
## Uruchomienie
Do działania aplikacji wymagane jest posiadanie Dockera.
Przed uruchomieniem aplikacji należy odpowiednio skonfigurować serwer do wysyłania emaili poprzez plik `application.yml`
oraz ustawienia `spring.mail.*`
Aby uruchomić aplikację należy uruchomić następujące polecenie w folderze z kodem:
```bash
docker-compose up --build
```
Aplikacja zostanie skompilowana a następnie uruchomiona.
Aplikacja będzie dostępna pod adresem `https://localhost:8443/`.
## Funkcjonalności
Aplikacja posiada następujące funkcjonalności:
- Tworzenie i usuwanie notatek
- Stylowanie notatek poprzez użycie `Markdown`
- Tworzenie notatek publicznych tj. dostępnych dla każdego
- Szyfrowanie notatek z użyciem hasła
- Notatki są przechowywane w oddzielnym kontenerze z bazą danych PostgreSQL
- Aplikacja obsługuje jedynie połączenie szyfrowane poprzez HTTPS
- Zmiana hasła dla użytkownika
- Zabezpieczenie 2FA przy logowaniu poprzez Google Authenticator
- Wykorzystanie tokenów CSRF
- Zabezpieczenia przez atakiem brute-force w tym blokada IP w przypadku nadmiernej liczby prób logowania oraz opóźnienia
podczas logowania
- Walidacja danych wejściowych w tym szczególnie haseł
- Po rejestracji należy aktywować konto poprzez link z wiadomości e-mail
- Odzyskiwanie hasła poprzez email
- Historia dostępu do aplikacji na danym koncie - informowanie o użytych urządeniach i adresach IP