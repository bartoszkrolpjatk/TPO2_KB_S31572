# Asynchroniczny Serwer Czatu (Java NIO)

**Uwaga:** Projekt został zrealizowany w ramach zadania akademickiego (projekt na studia).

---

## 🎯 Cel Projektu i Podsumowanie Rozwiązania

Głównym celem projektu było zaprojektowanie i implementacja asynchronicznego serwera i klienta czatu opartego na protokole TCP, zdolnego do efektywnej obsługi wielu jednoczesnych połączeń bez konieczności tworzenia osobnego wątku dla każdego klienta.

Kluczowym założeniem technologicznym było wykorzystanie biblioteki **Java NIO (Non-blocking I/O)**. Rozwiązanie to diametralnie różni się od podejść wykorzystujących wątki blokujące (klasyczne lub wirtualne). Opiera się na architekturze zdarzeniowej z wykorzystaniem **Selektorów (`Selector`)** oraz **nieblokujących kanałów (`SocketChannel`, `ServerSocketChannel`)**. Dzięki temu pojedynczy wątek serwera może wydajnie monitorować stan wielu połączeń (np. oczekiwanie na odczyt, gotowość do zapisu) i przetwarzać je (multipleksowanie).

### Technologie:
* **Język:** Java 
* **Sieć (NIO):** `java.nio.channels.Selector`, `java.nio.channels.SocketChannel`, `java.nio.channels.ServerSocketChannel`, `java.nio.channels.SelectionKey`
* **Zarządzanie pamięcią:** Buforowanie za pomocą `java.nio.ByteBuffer`
* **Kodowanie znaków:** Niestandardowe kodowanie `Cp1250` dla transmisji bajtowej
* **Współbieżność:** Klasyczne wątki (`Thread`, `Runnable`), `ExecutorService` (do uruchamiania zadań klientów)

---

## 📜 Protokół Komunikacyjny

Aplikacja wykorzystuje tekstowy protokół warstwy aplikacji przesyłany w formie zdekodowanych ciągów bajtów. Każda paczka danych jest sprawdzana przez `MessageValidator`. Wiadomości muszą kończyć się znakiem nowej linii (`\n`) i posiadać ścisły format z dwukropkiem jako separatorem (`:`).

**Struktura wiadomości:** `<OPERACJA>:<DANE>\n`

Obsługiwane operacje (`Operation.java`):
1.  **Logowanie:** `HI:<id_klienta>` 
    * *Przykład wychodzący:* `hi:Adam\n`
    * Nawiązuje sesję użytkownika na serwerze i przypisuje ID do połączenia.
2.  **Wysłanie wiadomości:** `SEND:<treść_wiadomości>`
    * *Przykład wychodzący:* `send:Cześć wszystkim!\n`
    * Rozsyła treść na czacie do pozostałych użytkowników.
3.  **Wylogowanie:** `BYE:<id_klienta>`
    * *Przykład wychodzący:* `bye:Adam\n`
    * Kończy sesję klienta i informuje innych o opuszczeniu czatu.
4.  **Zdarzenia serwera:** `EVENT:<treść>`
    * Zastrzeżone dla serwera do rozgłaszania (broadcast) wiadomości przychodzących od innych, a także komunikatów systemowych. Wyrzuca błąd, gdy wysłane przez klienta.

---

## 🔄 Komunikacja Klient-Serwer

Komunikacja jest **w pełni asynchroniczna**, a zarządzanie ruchem opiera się na cyklach pracy selektora (`selector.select()`).

### Architektura Serwera (`ChatServer`)
1.  **Pętla Zdarzeń (Event Loop):** Serwer działa w jednym głównym wątku, który iteruje po tzw. `SelectionKey` — kluczach reprezentujących gotowość kanału do wykonania konkretnej operacji I/O (`OP_ACCEPT`, `OP_READ`, `OP_WRITE`).
2.  **Akceptacja Połączeń:** Gdy pojawia się nowy klient (`isAcceptable()`), serwer akceptuje połączenie, przełącza kanał klienta w tryb nieblokujący (`configureBlocking(false)`) i rejestruje go w selektorze z zamiarem czytania (`OP_READ`).
3.  **Zarządzanie Sesją i Załączniki (Attachments):** Dla każdego klienta tworzony jest obiekt `UserSessionDto`, który jest przypinany (attach) do jego `SelectionKey`. Obiekt ten przetrzymuje stan klienta (np. ID) oraz własną kolejkę wiadomości do wysłania (`outputQueue`).
4.  **Rozsyłanie Wiadomości (Broadcast):** Kiedy serwer otrzymuje wiadomość, nie wysyła jej natychmiast. Zamiast tego dodaje ją (w postaci `ByteBuffer`) do kolejki wyjściowej wszystkich zalogowanych klientów i aktywuje w ich kluczach flagę gotowości do zapisu (`OP_WRITE`).
5.  **Zapis:** Gdy kanał klienta jest gotowy na przyjęcie danych (`isWritable()`), serwer zdejmuje z kolejki `ByteBuffer` i wysyła go w sieć.

### Architektura Klienta (`ChatClient` i `BroadcastListener`)
1.  **Nieblokujące Inicjowanie:** Klient tworzy swój `SocketChannel`, ustala go jako nieblokujący i rejestruje we własnym selektorze (używanym wyłącznie na potrzeby odbioru).
2.  **Wysyłanie Danych:** Główny proces klienta bezpośrednio wykonuje instrukcję zapisu wybranego zlecenia do kanału, używając kodowania `Cp1250` na bufor (`channel.write()`).
3.  **Osobny Wątek Odbiorczy (`BroadcastListener`):** Równolegle z wysyłaniem wiadomości, klient uruchamia w tle specjalną klasę `BroadcastListener` (obsługiwaną przez własny `Thread`). Nasłuchuje ona w pętli na `selector.select()` gotowości do czytania i w razie dostępności danych asynchronicznie wpisuje odczytane znaki do lokalnego widoku czatu (`chatView` opartego na `StringBuilder`).
4.  **Zamykanie Połączenia:** Wysłanie komendy `BYE` modyfikuje stan na serwerze i asynchronicznie przerywa w tle wątek nasłuchujący (`listener.interrupt()`), odpinając zasoby klienta za pomocą dedykowanych funkcji czyszczących.
