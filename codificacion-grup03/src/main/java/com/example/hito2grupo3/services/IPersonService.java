package com.example.hito2grupo3.services;
import java.util.List;
import java.util.Optional;

import com.example.hito2grupo3.entities.Person;
import com.example.hito2grupo3.dtos.PersonDTO;

public interface IPersonService {

	List<Person> getAll();

	Optional<Person> findById(int id);

	Person findByName(String name);

	Person insertOrUpdate(Person person);

	boolean remove(int id);

	List<PersonDTO> findByDegreeName(String degreeName);
}

