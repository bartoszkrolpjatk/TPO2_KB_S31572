package zad1.buffer;

import zad1.exception.checked.InvalidMessageFormatException;

import java.util.Optional;

class MessageValidator {

    static ReadResultDto validateMessageFormat(String message) throws InvalidMessageFormatException {
        if (message.isBlank())
            throw new InvalidMessageFormatException("Empty message!", message);

        var split = message.split(BufferService.DELIMITER, 2);
        if (split.length != 2)
            throw new InvalidMessageFormatException("Expected ':' character. Expected format: <operation>:<message>", message);

        Optional<Operation> operation = Operation.map(split[0]);
        if (operation.isEmpty())
            throw new InvalidMessageFormatException("Unsupported operation", message);

        if (message.charAt(message.length() - 1) != '\n')
            throw new InvalidMessageFormatException("Message must end with new line character '\\n'!", message);

        return new ReadResultDto(operation.get(), split[1]);
    }
}
