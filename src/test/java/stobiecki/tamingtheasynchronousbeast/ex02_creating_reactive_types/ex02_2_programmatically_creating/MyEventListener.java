package stobiecki.tamingtheasynchronousbeast.ex02_creating_reactive_types.ex02_2_programmatically_creating;

import java.util.List;

/**
 * Imagine that you use a listener-based API.
 * It processes data by chunks and has two events: (1) a chunk of data is ready and (2) the processing is complete (terminal event)
 */
interface MyEventListener<T> {
    void onDataChunk(List<T> chunk);

    void processComplete();
}
