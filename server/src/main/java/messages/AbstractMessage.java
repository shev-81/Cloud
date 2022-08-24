package messages;

import java.io.Serializable;

/**
 * Абстрактный класс сообщения, необходим в пустом виде,
 * так как каждый наследник имеет свою реализацию. Наследники созданные
 * на основе этого класса являются передаваемыми сообщениями.
 */
public abstract class AbstractMessage implements Serializable {
}