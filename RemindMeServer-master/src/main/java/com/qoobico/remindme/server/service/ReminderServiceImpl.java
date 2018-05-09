package com.qoobico.remindme.server.service;

import com.qoobico.remindme.server.entity.Remind;
import com.qoobico.remindme.server.repository.RemindRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReminderServiceImpl implements ReminderService {
    @Override
    public List<Remind> getAll() {
        return null;
    }

    @Override
    public Remind getByID(long id) {
        return null;
    }

    @Override
    public Remind save(Remind remind) {
        return null;
    }

    @Override
    public void remove(long id) {

    }
//
//    @Autowired
//    private RemindRepository repository;
//
//    public List<Remind> getAll() {
//        return repository.findAll();
//    }
//
//    public Remind getByID(long id) {
//        return repository.findOne(id);
//    }
//
//    public Remind save(Remind remind) {
//        return repository.saveAndFlush(remind);
//    }
//
//    public void remove(long id) {
//        repository.delete(id);
//    }
}
