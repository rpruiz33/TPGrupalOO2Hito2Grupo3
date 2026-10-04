package com.example.hito2grupo3.services;
import java.util.List;

import com.example.hito2grupo3.entities.Degree;
import com.example.hito2grupo3.dtos.DegreeDTO;


@SuppressWarnings("unused")
public interface IDegreeService {

	List<Degree> getAll();

	void insertOrUpdate(DegreeDTO degreeDTO);

	boolean remove(int id);
}

