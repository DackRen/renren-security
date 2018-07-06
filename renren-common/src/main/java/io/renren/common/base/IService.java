package io.renren.common.base;

import java.io.Serializable;
import java.util.List;

public interface IService<T extends AbstractEntity, ID extends Serializable> {
    T delete(final ID id);

    T findById(final ID id);

    List<T> findAll();

    T save(final T domain);

    void deleteById(final ID id);

}
