package io.renren.common.base;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

/**
 * @author rjz
 */
public class ServiceImpl<T extends AbstractEntity, ID extends Serializable> implements IService<T, ID> {

    protected JpaRepository<T, ID> repository;

    public ServiceImpl(final JpaRepository<T, ID> repository) {
        this.repository = repository;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public T delete(final ID id) {
        final T deleted = this.repository.findById(id).orElseThrow(RuntimeException::new);
        this.repository.delete(deleted);
        return deleted;
    }

    @Transactional(readOnly = true)
    @Override
    public T findById(final ID id) {
//        TODO
//        return this.repository.findById(id).orElseThrow(RuntimeException::new);
        return this.repository.findById(id).orElse(null);
    }

    @Override
    public List<T> findAll() {
        return this.repository.findAll();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public T save(final T domain) {
        return this.repository.save(domain);
    }

    @Override
    public void deleteById(ID id) {
        this.repository.deleteById(id);
    }

//
//    @Transactional(rollbackFor = Exception.class)
//    public T update(final T base) throws Exception {
//        if(this.repository.exists(Example.of(base))) {
//            throw new Exception();
//        }
//    }
}
