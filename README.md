# Notes secure
Projekt prostej aplikacji pozwalającej na przechowywanie notatek. 
Głównym nastawieniem projektu jest bezpieczeństwo aplikacji oraz przechowywanych notatek.
Projekt zbudowant w oparciu o Java oraz Spring MVC.
## Uruchomienie
Aby uruchomić aplikację należy uruchomić następujący kod w folderze z kodem:
```bash
docker-compose up --build
```
Aplikacja zostanie skompilowana a następnie uruchomiona.
Aby skorzystać z aplikacji należy uruchomić na przeglądarcę stronę `https://localhost:8443/`.
Następnie można korzystać z aplikacji.
## Funkcjonalności
Aplikacja posiada następujące funkcjonalności:
- Tworzenie i usuwanie notatek
- Stylowanie notatek poprzez użycie `Markdown`
- Tworzenie notatek publicznych tj. dostępnych dla każdego
- Szyfrowanie notatek z użyciem hasła
- Notatki są przechowywane w oddzielnym kontenerze z bazą danych PostgreSQL
- Aplikacja obsługuje jedynie połączenie szyfrowane poprzez HTTPS
- Zmiana hasła dla użytkownika
- Włączanie i wyłączanie zabezpieczenia 2FA
- Wykorzystanie tokenów CSRF
- Zabezpieczenie przez atakiem brute-force poprzez blokadę IP w przypadku nadmiernej liczby prób logowania
- Przechowywanie haseł w zaszyfrowanej formie
- Walidacja danych wejściowych w tym szczególnie haseł