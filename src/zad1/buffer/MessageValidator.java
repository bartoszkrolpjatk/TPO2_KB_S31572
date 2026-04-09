package zad1.buffer;

import zad1.exception.checked.InvalidMessageFormatException;

import java.util.Optional;

class MessageValidator {

    static ReadResultDto validateMessageFormat(String message) throws InvalidMessageFormatException {
        var split = message.split(BufferService.DELIMITER);
        if (split.length != 2)
            throw new InvalidMessageFormatException("Message should be split by ':' character. Expected format: <operation>:<message>");

        Optional<Operation> operation = Operation.map(split[0]);
        if (operation.isEmpty())
            throw new InvalidMessageFormatException("Unsupported operation: %s".formatted(split[0]));

        if (message.charAt(message.length() - 1) != '\n')
            throw new InvalidMessageFormatException("Message must end with new line character '\\n'!");

        return new ReadResultDto(operation.get(), split[1]);
    }
}
