# Simple Chat
Aplikacja jest implementacją prostego chatu składającego się z serwera i testowych klientów.
Serwer wykorzystuje java.nio.channels.Selector do multipleksowania kanałów komunikacji z klientami.

Na potrzeby niniejszego systemu został stworzony prosty **protokół komunikacji**.

## Protokół komunikacji

Protokół służy do prostej komunikacji na potrzeby aplikacji *SimpleChat*.

### Rodzaje komunikatów
Jego założenia są następujące. Klient wysyła 3 rodzaje wiadomości:
1. logowanie do systemu
2. wylogowanie z systemu
3. wysłanie wiadomości

### Schemat komunikacji
Klient wysyła do serwera jeden z powyższych komunikatów.
Serwer następnie wysyła wszystkim podłączonym klientom komunikat o przeprowadzeniu powyższego komunikatu.

### Format komunikatów
 * **Klient**
   * logowanie: `hi:<id>\n`
   * wylogowanie: `bye:<id>\n`
   * wysłanie wiadomości: `send:<message>\n`
 * **Serwer**
   * zdarzenie: `broadcast:<message>\n`
   * błąd: `error:<cause>\n`
