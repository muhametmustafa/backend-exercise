package services;

import java.util.List;

public interface BaseRepository<T> {
    T findById(String collectionName, Class<T> classType, String id);
    List<T> findAll(String collectionName, Class<T> classType);
    T deleteById(String collectionName, Class<T> classType, String id);
    T save (String collectionName, Class<T> classType,T t);
    //List<T> saveAll(List<T> list); if we want to include
    T update(String collectionName, Class<T> classType, String id, T t);

}
