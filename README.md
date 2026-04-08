# Protokół komunikacji

Protokół służy do prostej komunikacji na potrzeby aplikacji *SimpleChat*.

## Rodzaje komunikatów
Jego założenia są następujące. Klient wysyła 3 rodzaje wiadomości:
1. logowanie do systemu
2. wylogowanie z systemu
3. wysłanie wiadomości

## Schemat komunikacji
Klient wysyła do serwera jeden z powyższych komunikatów.
Serwer następnie wysyła wszystkim podłączonym klientom komunikat o przeprowadzeniu powyższego komunikatu.

## Format komunikatów
 * logowanie -             `hi:<id>`
 * wylogowanie -           `bye:<id>`
 * wysłanie wiadomości -   `send:<message>`
 * zdarzenie -             `event:<message>`
