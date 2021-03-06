package stobiecki.tamingtheasynchronousbeast.ex11_more.repository;

public interface BlockingRepository<T> {

    void save(T value);

    T findFirst();

    Iterable<T> findAll();

    T findById(String id);
}