package stobiecki.tamingtheasynchronousbeast.ex02_creating_reactive_types.ex02_2_programmatically_creating;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class EventProcessor<T> {

    private final Collection<MyEventListener<T>> listeners = new CopyOnWriteArrayList<>();

    void register(MyEventListener<T> myEventListener) {
        this.listeners.add(myEventListener);
    }

    void newChunk(List<T> chunk) {
        listeners.forEach(listener -> listener.onDataChunk(chunk));
    }

    void complete() {
        listeners.forEach(MyEventListener::processComplete);
    }

    void removeListener(MyEventListener<T> myEventListener) {
        listeners.remove(myEventListener);
    }
}
